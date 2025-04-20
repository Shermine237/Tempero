package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shermine237.tempora.adapter.WorkDayAdapter;
import com.shermine237.tempora.databinding.FragmentWorkHoursBinding;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.model.WorkHours;
import com.shermine237.tempora.viewmodel.UserProfileViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pour gérer les heures de travail par jour.
 */
public class WorkHoursFragment extends Fragment implements WorkDayAdapter.OnWorkDayChangedListener {
    
    private FragmentWorkHoursBinding binding;
    private UserProfileViewModel userProfileViewModel;
    private UserProfile userProfile;
    private WorkDayAdapter adapter;
    private List<WorkHours> workHoursList;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkHoursBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        
        // Configurer le RecyclerView
        binding.recyclerWorkDays.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Charger les données du profil utilisateur
        userProfileViewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                userProfile = profile;
                initWorkHoursList();
                setupRecyclerView();
            }
        });
        
        // Configurer le bouton de sauvegarde
        binding.buttonSave.setOnClickListener(v -> saveWorkHours());
    }
    
    /**
     * Initialise la liste des heures de travail
     */
    private void initWorkHoursList() {
        workHoursList = new ArrayList<>();
        
        // Si le profil a déjà des heures de travail définies, les utiliser
        if (userProfile.getWorkHoursByDay() != null && !userProfile.getWorkHoursByDay().isEmpty()) {
            workHoursList.addAll(userProfile.getWorkHoursByDay());
        } else {
            // Sinon, créer des heures de travail par défaut pour chaque jour
            for (int i = 0; i < 7; i++) {
                boolean isWorkDay = userProfile.getWorkDays().contains(i);
                WorkHours workHours = new WorkHours(
                        i,
                        userProfile.getPreferredWorkStartHour(),
                        userProfile.getPreferredWorkEndHour(),
                        isWorkDay
                );
                workHoursList.add(workHours);
            }
            
            // Mettre à jour le profil utilisateur avec ces heures par défaut
            userProfile.setWorkHoursByDay(new ArrayList<>(workHoursList));
            userProfileViewModel.update(userProfile);
        }
        
        // Trier la liste par jour de la semaine
        workHoursList.sort((wh1, wh2) -> Integer.compare(wh1.getDayOfWeek(), wh2.getDayOfWeek()));
    }
    
    /**
     * Configure le RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new WorkDayAdapter(requireContext(), workHoursList, this);
        binding.recyclerWorkDays.setAdapter(adapter);
    }
    
    /**
     * Enregistre les heures de travail
     */
    private void saveWorkHours() {
        if (userProfile != null) {
            // Mettre à jour les heures de travail dans le profil utilisateur
            userProfile.setWorkHoursByDay(workHoursList);
            
            // Mettre à jour la liste des jours de travail
            List<Integer> workDays = new ArrayList<>();
            for (WorkHours workHours : workHoursList) {
                if (workHours.isWorkDay()) {
                    workDays.add(workHours.getDayOfWeek());
                }
            }
            userProfile.setWorkDays(workDays);
            
            // Mettre à jour également les heures de travail générales
            // en utilisant les heures du premier jour de travail trouvé
            for (WorkHours workHours : workHoursList) {
                if (workHours.isWorkDay()) {
                    userProfile.setPreferredWorkStartHour(workHours.getStartHour());
                    userProfile.setPreferredWorkEndHour(workHours.getEndHour());
                    break;
                }
            }
            
            // Enregistrer le profil
            userProfileViewModel.update(userProfile);
            
            // Afficher un message de confirmation
            Toast.makeText(requireContext(), "Heures de travail enregistrées", Toast.LENGTH_SHORT).show();
            
            // Revenir au fragment précédent
            requireActivity().onBackPressed();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    @Override
    public void onWorkDayChanged(WorkHours workHours) {
        // Cette méthode est appelée lorsqu'un jour de travail est modifié
        // Nous n'avons pas besoin de faire quoi que ce soit ici car les modifications
        // sont déjà appliquées à l'objet WorkHours dans l'adaptateur
    }
}
