package com.shermine237.tempora.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.shermine237.tempora.databinding.FragmentTaskDetailBinding;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    private FragmentTaskDetailBinding binding;
    private TaskViewModel taskViewModel;
    private Task currentTask;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        
        // Récupérer l'ID de la tâche depuis les arguments
        if (getArguments() != null) {
            int taskId = getArguments().getInt("taskId");
            loadTaskDetails(taskId);
        }
        
        // Configurer les boutons
        binding.buttonCompleteTask.setOnClickListener(v -> {
            completeTask();
        });
        
        binding.buttonEditTask.setOnClickListener(v -> {
            // Navigation vers l'écran d'édition (à implémenter)
            Toast.makeText(requireContext(), "Fonctionnalité d'édition à venir", Toast.LENGTH_SHORT).show();
        });
        
        binding.buttonDeleteTask.setOnClickListener(v -> {
            confirmDeleteTask();
        });
    }

    private void loadTaskDetails(int taskId) {
        taskViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                currentTask = task;
                updateUI(task);
            }
        });
    }

    private void updateUI(Task task) {
        // Titre et description
        binding.textTaskTitle.setText(task.getTitle());
        binding.textTaskDescription.setText(task.getDescription());
        
        // Statut
        String status = task.isCompleted() ? "Terminée" : "En cours";
        binding.textTaskStatus.setText(status);
        
        // Statut d'approbation
        String approvalStatus = task.isApproved() ? "Approuvée" : "En attente d'approbation";
        binding.textTaskApprovalStatus.setText(approvalStatus);
        binding.textTaskApprovalStatus.setTextColor(androidx.core.content.ContextCompat.getColor(
            requireContext(), 
            task.isApproved() ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark));
        
        // Source de la tâche (manuelle ou IA)
        String sourceText;
        int sourceColor;
        
        // Utiliser l'attribut aiGenerated pour déterminer la source de la tâche
        if (task.isAiGenerated()) {
            sourceText = "Générée par IA";
            sourceColor = android.R.color.holo_blue_dark;
        } else {
            sourceText = "Créée manuellement";
            sourceColor = android.R.color.holo_green_dark;
        }
        
        binding.textTaskDetailSource.setText(sourceText);
        binding.textTaskDetailSource.setTextColor(androidx.core.content.ContextCompat.getColor(
            requireContext(), sourceColor));
        
        // Date planifiée
        if (task.getScheduledDate() != null) {
            binding.textTaskScheduledDate.setText("Date planifiée: " + dateFormat.format(task.getScheduledDate()));
            binding.textTaskScheduledDate.setVisibility(View.VISIBLE);
        } else {
            binding.textTaskScheduledDate.setVisibility(View.GONE);
        }
        
        // Date d'échéance
        if (task.getDueDate() != null) {
            binding.textTaskDueDate.setText("Échéance: " + dateFormat.format(task.getDueDate()));
        } else {
            binding.textTaskDueDate.setText("Échéance: Non définie");
        }
        
        // Priorité
        String priorityText;
        switch (task.getPriority()) {
            case 1:
                priorityText = "1 (Très basse)";
                break;
            case 2:
                priorityText = "2 (Basse)";
                break;
            case 3:
                priorityText = "3 (Moyenne)";
                break;
            case 4:
                priorityText = "4 (Haute)";
                break;
            case 5:
                priorityText = "5 (Très haute)";
                break;
            default:
                priorityText = "Non définie";
        }
        binding.textTaskPriority.setText(priorityText);
        
        // Difficulté
        String difficultyText;
        switch (task.getDifficulty()) {
            case 1:
                difficultyText = "1 (Très facile)";
                break;
            case 2:
                difficultyText = "2 (Facile)";
                break;
            case 3:
                difficultyText = "3 (Moyenne)";
                break;
            case 4:
                difficultyText = "4 (Difficile)";
                break;
            case 5:
                difficultyText = "5 (Très difficile)";
                break;
            default:
                difficultyText = "Non définie";
        }
        binding.textTaskDifficulty.setText(difficultyText);
        
        // Catégorie
        binding.textTaskCategory.setText(task.getCategory());
        
        // Durée estimée
        binding.textTaskEstimatedDuration.setText(task.getEstimatedDuration() + " minutes");
        
        // Durée réelle (si la tâche est complétée)
        if (task.isCompleted() && task.getActualDuration() > 0) {
            binding.layoutActualDuration.setVisibility(View.VISIBLE);
            binding.textTaskActualDuration.setText(task.getActualDuration() + " minutes");
        } else {
            binding.layoutActualDuration.setVisibility(View.GONE);
        }
        
        // Mettre à jour le texte du bouton en fonction du statut
        if (task.isCompleted()) {
            binding.buttonCompleteTask.setText("Marquer comme non terminée");
        } else {
            binding.buttonCompleteTask.setText("Marquer comme terminée");
        }
    }

    private void completeTask() {
        if (currentTask == null) {
            return;
        }
        
        if (currentTask.isCompleted()) {
            // Si la tâche est déjà complétée, la marquer comme non complétée
            currentTask.setCompleted(false);
            currentTask.setCompletionDate(null);
            taskViewModel.update(currentTask);
            Toast.makeText(requireContext(), "Tâche marquée comme non terminée", Toast.LENGTH_SHORT).show();
        } else {
            // Sinon, la marquer comme complétée
            currentTask.setCompleted(true);
            currentTask.setCompletionDate(new Date());
            
            // Pour la durée réelle, on pourrait demander à l'utilisateur ou utiliser une estimation
            // Ici, on utilise simplement la durée estimée comme valeur par défaut
            currentTask.setActualDuration(currentTask.getEstimatedDuration());
            
            taskViewModel.update(currentTask);
            Toast.makeText(requireContext(), "Tâche marquée comme terminée", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteTask() {
        if (currentTask == null) {
            return;
        }
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la tâche")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette tâche ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    taskViewModel.delete(currentTask);
                    Toast.makeText(requireContext(), "Tâche supprimée", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
