package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.FragmentProfileBinding;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.ui.adapter.CategoryAdapter;
import com.shermine237.tempora.viewmodel.UserProfileViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment implements CategoryAdapter.CategoryListener {

    private FragmentProfileBinding binding;
    private UserProfileViewModel userProfileViewModel;
    private UserProfile currentProfile;
    private CategoryAdapter categoryAdapter;

    // Tableaux pour les heures
    private final String[] hours = new String[24];
    private final CheckBox[] dayCheckboxes = new CheckBox[7];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        
        // Initialiser les tableaux d'heures
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
        }
        
        // Configurer les spinners d'heures
        setupHourSpinners();
        
        // Configurer les checkboxes des jours
        setupDayCheckboxes();
        
        // Configurer le RecyclerView des catégories
        setupCategoriesRecyclerView();
        
        // Observer les données du profil
        observeProfileData();
        
        // Configurer le bouton de sauvegarde
        binding.buttonSaveProfile.setOnClickListener(v -> {
            saveProfile();
        });
        
        // Configurer le bouton d'ajout de catégorie
        binding.buttonAddCategory.setOnClickListener(v -> {
            addCategory();
        });
    }

    private void setupHourSpinners() {
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        binding.spinnerStartHour.setAdapter(hourAdapter);
        binding.spinnerEndHour.setAdapter(hourAdapter);
    }

    private void setupDayCheckboxes() {
        dayCheckboxes[0] = binding.checkboxSunday;
        dayCheckboxes[1] = binding.checkboxMonday;
        dayCheckboxes[2] = binding.checkboxTuesday;
        dayCheckboxes[3] = binding.checkboxWednesday;
        dayCheckboxes[4] = binding.checkboxThursday;
        dayCheckboxes[5] = binding.checkboxFriday;
        dayCheckboxes[6] = binding.checkboxSaturday;
    }

    private void setupCategoriesRecyclerView() {
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this, requireContext());
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCategories.setAdapter(categoryAdapter);
    }

    private void observeProfileData() {
        userProfileViewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                currentProfile = profile;
                updateUI(profile);
            }
        });
    }

    private void updateUI(UserProfile profile) {
        // Informations personnelles
        binding.editName.setText(profile.getName());
        binding.editEmail.setText(profile.getEmail());
        
        // Heures de travail
        binding.spinnerStartHour.setSelection(profile.getPreferredWorkStartHour());
        binding.spinnerEndHour.setSelection(profile.getPreferredWorkEndHour());
        
        // Jours de travail
        List<Integer> workDays = profile.getWorkDays();
        for (int i = 0; i < dayCheckboxes.length; i++) {
            dayCheckboxes[i].setChecked(workDays.contains(i));
        }
        
        // Préférences de pause
        binding.editShortBreak.setText(String.valueOf(profile.getShortBreakDuration()));
        binding.editLongBreak.setText(String.valueOf(profile.getLongBreakDuration()));
        binding.editSessionsBeforeLongBreak.setText(String.valueOf(profile.getWorkSessionsBeforeLongBreak()));
        
        // Catégories
        categoryAdapter.updateCategories(profile.getCustomCategories());
    }

    private void addCategory() {
        String newCategory = binding.editNewCategory.getText().toString().trim();
        
        if (newCategory.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer un nom de catégorie", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentProfile != null) {
            List<String> categories = new ArrayList<>(currentProfile.getCustomCategories());
            
            // Vérifier si la catégorie existe déjà
            if (categories.contains(newCategory)) {
                Toast.makeText(requireContext(), "Cette catégorie existe déjà", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Ajouter la nouvelle catégorie
            categories.add(newCategory);
            currentProfile.setCustomCategories(categories);
            
            // Mettre à jour l'interface
            categoryAdapter.updateCategories(categories);
            
            // Effacer le champ de texte
            binding.editNewCategory.setText("");
        }
    }
    
    @Override
    public void onCategoryEdit(int position, String newName) {
        if (currentProfile != null) {
            List<String> categories = new ArrayList<>(currentProfile.getCustomCategories());
            
            // Vérifier si la nouvelle catégorie existe déjà
            if (categories.contains(newName) && !categories.get(position).equals(newName)) {
                Toast.makeText(requireContext(), "Cette catégorie existe déjà", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Modifier la catégorie
            categories.set(position, newName);
            currentProfile.setCustomCategories(categories);
            
            // Mettre à jour l'interface
            categoryAdapter.updateCategories(categories);
        }
    }
    
    @Override
    public void onCategoryDelete(int position) {
        if (currentProfile != null) {
            List<String> categories = new ArrayList<>(currentProfile.getCustomCategories());
            
            // Supprimer la catégorie
            categories.remove(position);
            currentProfile.setCustomCategories(categories);
            
            // Mettre à jour l'interface
            categoryAdapter.updateCategories(categories);
        }
    }

    private void saveProfile() {
        if (currentProfile == null) {
            return;
        }
        
        // Récupérer les informations personnelles
        String name = binding.editName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mettre à jour les informations personnelles
        currentProfile.setName(name);
        currentProfile.setEmail(email);
        
        // Récupérer les heures de travail
        int startHour = binding.spinnerStartHour.getSelectedItemPosition();
        int endHour = binding.spinnerEndHour.getSelectedItemPosition();
        
        // Récupérer les jours de travail
        List<Integer> workDays = new ArrayList<>();
        for (int i = 0; i < dayCheckboxes.length; i++) {
            if (dayCheckboxes[i].isChecked()) {
                workDays.add(i);
            }
        }
        
        // Mettre à jour les préférences de travail
        userProfileViewModel.updateWorkPreferences(currentProfile, startHour, endHour, workDays);
        
        // Récupérer les préférences de pause
        String shortBreakStr = binding.editShortBreak.getText().toString().trim();
        String longBreakStr = binding.editLongBreak.getText().toString().trim();
        String sessionsStr = binding.editSessionsBeforeLongBreak.getText().toString().trim();
        
        if (!shortBreakStr.isEmpty() && !longBreakStr.isEmpty() && !sessionsStr.isEmpty()) {
            int shortBreak = Integer.parseInt(shortBreakStr);
            int longBreak = Integer.parseInt(longBreakStr);
            int sessions = Integer.parseInt(sessionsStr);
            
            // Mettre à jour les préférences de pause
            userProfileViewModel.updateBreakPreferences(currentProfile, shortBreak, longBreak, sessions);
        }
        
        // Mettre à jour les catégories personnalisées
        userProfileViewModel.updateCustomCategories(currentProfile, currentProfile.getCustomCategories());
        
        Toast.makeText(requireContext(), "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
