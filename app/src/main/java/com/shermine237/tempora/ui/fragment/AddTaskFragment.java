package com.shermine237.tempora.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.shermine237.tempora.databinding.FragmentAddTaskBinding;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.viewmodel.TaskViewModel;
import com.shermine237.tempora.viewmodel.UserProfileViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskFragment extends Fragment {

    private FragmentAddTaskBinding binding;
    private TaskViewModel taskViewModel;
    private UserProfileViewModel userProfileViewModel;
    private Date selectedDueDate;
    private Date selectedScheduledDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser les ViewModels
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        
        // Configurer le sélecteur de date d'échéance
        binding.buttonSelectDate.setOnClickListener(v -> {
            showDatePicker(true);
        });
        
        // Configurer le sélecteur de date planifiée
        binding.buttonSelectScheduledDate.setOnClickListener(v -> {
            showDatePicker(false);
        });
        
        // Configurer le spinner de catégories
        setupCategorySpinner();
        
        // Configurer le bouton de sauvegarde
        binding.buttonSaveTask.setOnClickListener(v -> {
            saveTask();
        });
    }

    private void setupCategorySpinner() {
        userProfileViewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null && profile.getCustomCategories() != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        profile.getCustomCategories());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCategory.setAdapter(adapter);
            }
        });
    }

    private void showDatePicker(boolean isDueDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    if (isDueDate) {
                        selectedDueDate = calendar.getTime();
                        binding.textSelectedDate.setText(dateFormat.format(selectedDueDate));
                    } else {
                        selectedScheduledDate = calendar.getTime();
                        binding.textSelectedScheduledDate.setText(dateFormat.format(selectedScheduledDate));
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void saveTask() {
        // Récupérer le titre et la description
        String title = binding.editTaskTitle.getText().toString().trim();
        String description = binding.editTaskDescription.getText().toString().trim();
        
        // Vérifier que le titre n'est pas vide
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer un titre pour la tâche", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Vérifier que la date d'échéance est sélectionnée
        if (selectedDueDate == null) {
            Toast.makeText(requireContext(), "Veuillez sélectionner une date d'échéance", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Récupérer la priorité
        int priority = getPriorityFromRadioGroup();
        
        // Récupérer la difficulté
        int difficulty = getDifficultyFromRadioGroup();
        
        // Récupérer la durée estimée
        String durationStr = binding.editEstimatedDuration.getText().toString().trim();
        if (durationStr.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer une durée estimée", Toast.LENGTH_SHORT).show();
            return;
        }
        int estimatedDuration = Integer.parseInt(durationStr);
        
        // Récupérer la catégorie
        String category = binding.spinnerCategory.getSelectedItem().toString();
        
        // Créer la tâche
        taskViewModel.createTaskWithScheduledDate(title, description, selectedDueDate, 
                selectedScheduledDate, priority, difficulty, estimatedDuration, category);
        
        // Retourner à la liste des tâches
        Toast.makeText(requireContext(), "Tâche créée avec succès", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(binding.getRoot()).navigateUp();
    }

    private int getPriorityFromRadioGroup() {
        int radioButtonId = binding.radioGroupPriority.getCheckedRadioButtonId();
        RadioButton radioButton = binding.getRoot().findViewById(radioButtonId);
        return Integer.parseInt(radioButton.getText().toString());
    }

    private int getDifficultyFromRadioGroup() {
        int radioButtonId = binding.radioGroupDifficulty.getCheckedRadioButtonId();
        RadioButton radioButton = binding.getRoot().findViewById(radioButtonId);
        return Integer.parseInt(radioButton.getText().toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
