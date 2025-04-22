package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.FragmentScheduleDetailBinding;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.ui.adapter.ScheduleDetailAdapter;
import com.shermine237.tempora.viewmodel.ScheduleViewModel;
import com.shermine237.tempora.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleDetailFragment extends Fragment implements ScheduleDetailAdapter.OnScheduleItemClickListener {

    private FragmentScheduleDetailBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private TaskViewModel taskViewModel;
    private ScheduleDetailAdapter scheduleAdapter;
    private Schedule currentSchedule;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        
        // Configurer le RecyclerView
        setupRecyclerView();
        
        // Récupérer la date du planning depuis les arguments
        if (getArguments() != null) {
            if (getArguments().containsKey("scheduleId")) {
                // Si on a un ID de planning
                int scheduleId = getArguments().getInt("scheduleId");
                loadScheduleDetailsById(scheduleId);
            } else if (getArguments().containsKey("date")) {
                // Si on a une date
                long dateMillis = getArguments().getLong("date");
                Date scheduleDate = new Date(dateMillis);
                loadScheduleDetailsByDate(scheduleDate);
            }
        }
        
        // Configurer le bouton d'approbation
        binding.buttonApproveSchedule.setOnClickListener(v -> {
            approveSchedule();
        });
    }

    private void setupRecyclerView() {
        scheduleAdapter = new ScheduleDetailAdapter(new ArrayList<>(), this);
        binding.recyclerScheduleItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerScheduleItems.setAdapter(scheduleAdapter);
    }

    private void loadScheduleDetailsById(int scheduleId) {
        scheduleViewModel.getScheduleById(scheduleId).observe(getViewLifecycleOwner(), schedule -> {
            if (schedule != null) {
                currentSchedule = schedule;
                updateUI(schedule);
            }
        });
    }

    private void loadScheduleDetailsByDate(Date scheduleDate) {
        scheduleViewModel.getScheduleByDate(scheduleDate).observe(getViewLifecycleOwner(), schedule -> {
            if (schedule != null) {
                currentSchedule = schedule;
                updateUI(schedule);
            }
        });
    }

    private void updateUI(Schedule schedule) {
        // Date du planning
        if (schedule.getDate() != null) {
            binding.textScheduleDate.setText(dateFormat.format(schedule.getDate()));
        }
        
        // Éléments du planning
        List<ScheduleItem> items = schedule.getItems();
        if (items != null && !items.isEmpty()) {
            scheduleAdapter.updateScheduleItems(items);
            binding.textEmptySchedule.setVisibility(View.GONE);
            binding.recyclerScheduleItems.setVisibility(View.VISIBLE);
        } else {
            binding.textEmptySchedule.setVisibility(View.VISIBLE);
            binding.recyclerScheduleItems.setVisibility(View.GONE);
        }
        
        // Mettre à jour le bouton d'approbation
        if (schedule.isApproved()) {
            binding.buttonApproveSchedule.setText("Planning approuvé");
            binding.buttonApproveSchedule.setEnabled(false);
        } else {
            binding.buttonApproveSchedule.setText("Approuver ce planning");
            binding.buttonApproveSchedule.setEnabled(true);
        }
    }

    private void approveSchedule() {
        if (currentSchedule == null) {
            Toast.makeText(requireContext(), "Aucun planning à approuver", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Récupérer les tâches du planning
        List<ScheduleItem> items = currentSchedule.getItems();
        
        if (items == null || items.isEmpty()) {
            Toast.makeText(requireContext(), "Le planning est vide", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Log pour le débogage
        Log.d("ScheduleDetailFragment", "Nombre total d'éléments dans le planning: " + items.size());
        
        // Récupérer toutes les tâches non approuvées
        taskViewModel.getAllTasksIncludingUnapproved().observe(getViewLifecycleOwner(), tasks -> {
            List<Task> unapprovedTasks = new ArrayList<>();
            
            // Filtrer pour ne garder que les tâches non approuvées
            for (Task task : tasks) {
                if (!task.isApproved()) {
                    unapprovedTasks.add(task);
                    Log.d("ScheduleDetailFragment", "Tâche non approuvée trouvée: " + task.getTitle() + 
                          ", ID: " + task.getId() + 
                          ", Date planifiée: " + (task.getScheduledDate() != null ? task.getScheduledDate() : "null") + 
                          ", Date d'échéance: " + (task.getDueDate() != null ? task.getDueDate() : "null"));
                }
            }
            
            if (unapprovedTasks.isEmpty()) {
                Toast.makeText(requireContext(), "Aucune tâche à approuver", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Créer la liste des tâches à afficher dans la boîte de dialogue
            String[] taskNames = new String[unapprovedTasks.size()];
            boolean[] checkedItems = new boolean[unapprovedTasks.size()];
            final List<Integer> selectedItems = new ArrayList<>();
            
            // Remplir les tableaux
            for (int i = 0; i < unapprovedTasks.size(); i++) {
                Task task = unapprovedTasks.get(i);
                taskNames[i] = task.getTitle();
                checkedItems[i] = true; // Toutes les tâches sont sélectionnées par défaut
                selectedItems.add(i);
            }
            
            // Créer la boîte de dialogue
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Sélectionnez les tâches à approuver");
            
            // Ajouter les cases à cocher
            builder.setMultiChoiceItems(taskNames, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(which);
                } else {
                    selectedItems.remove(Integer.valueOf(which));
                }
            });
            
            // Ajouter les boutons
            builder.setPositiveButton("Approuver", (dialog, which) -> {
                // Approuver les tâches sélectionnées
                for (int i = 0; i < selectedItems.size(); i++) {
                    int index = selectedItems.get(i);
                    Task task = unapprovedTasks.get(index);
                    
                    // Log avant la modification
                    Log.d("ScheduleDetailFragment", "AVANT approbation - Tâche: " + task.getTitle() + 
                          ", ID: " + task.getId() + 
                          ", Générée par IA: " + task.isAiGenerated() + 
                          ", Approuvée: " + task.isApproved() + 
                          ", Date planifiée: " + (task.getScheduledDate() != null ? task.getScheduledDate() : "null") + 
                          ", Date d'échéance: " + (task.getDueDate() != null ? task.getDueDate() : "null"));
                    
                    task.setApproved(true);
                    // Nous ne modifions pas l'attribut aiGenerated pour préserver l'origine de la tâche
                    
                    // Log après la modification
                    Log.d("ScheduleDetailFragment", "APRÈS approbation - Tâche: " + task.getTitle() + 
                          ", ID: " + task.getId() + 
                          ", Générée par IA: " + task.isAiGenerated() + 
                          ", Approuvée: " + task.isApproved() + 
                          ", Date planifiée: " + (task.getScheduledDate() != null ? task.getScheduledDate() : "null") + 
                          ", Date d'échéance: " + (task.getDueDate() != null ? task.getDueDate() : "null"));
                    
                    taskViewModel.update(task);
                }
                
                Toast.makeText(requireContext(), "Tâches approuvées", Toast.LENGTH_SHORT).show();
            });
            
            builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
            
            // Afficher la boîte de dialogue
            builder.create().show();
        });
    }

    @Override
    public void onScheduleItemClick(ScheduleItem item) {
        // Si c'est une tâche, naviguer vers les détails de la tâche
        if (item.getTaskId() > 0) {
            Bundle bundle = new Bundle();
            bundle.putInt("taskId", item.getTaskId());
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_schedule_detail_to_navigation_task_detail, bundle);
        }
    }

    @Override
    public void onScheduleItemCompleteClick(ScheduleItem item, boolean isCompleted) {
        // Mettre à jour l'état de l'élément du planning
        if (currentSchedule != null) {
            List<ScheduleItem> items = currentSchedule.getItems();
            for (ScheduleItem scheduleItem : items) {
                if (scheduleItem.getTitle().equals(item.getTitle()) && 
                    scheduleItem.getStartTime().equals(item.getStartTime())) {
                    scheduleItem.setCompleted(isCompleted);
                    break;
                }
            }
            currentSchedule.setItems(items);
            scheduleViewModel.update(currentSchedule);
            
            // Vérifier si tous les éléments sont complétés
            boolean allCompleted = true;
            for (ScheduleItem scheduleItem : items) {
                if (!scheduleItem.isCompleted() && scheduleItem.getType().equals("task")) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                currentSchedule.setCompleted(true);
                scheduleViewModel.update(currentSchedule);
                Toast.makeText(requireContext(), "Toutes les tâches sont terminées !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
