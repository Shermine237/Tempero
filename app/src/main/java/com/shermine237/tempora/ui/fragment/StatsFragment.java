package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shermine237.tempora.ai.UserHabitAnalyzer;
import com.shermine237.tempora.databinding.FragmentStatsBinding;
import com.shermine237.tempora.service.AIService;
import com.shermine237.tempora.viewmodel.ScheduleViewModel;
import com.shermine237.tempora.viewmodel.TaskViewModel;

import java.util.Arrays;
import java.util.Date;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private TaskViewModel taskViewModel;
    private ScheduleViewModel scheduleViewModel;
    private AIService aiService;
    private UserHabitAnalyzer habitAnalyzer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser les ViewModels
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        
        // Initialiser le service IA et l'analyseur d'habitudes
        aiService = new AIService(requireActivity().getApplication());
        habitAnalyzer = new UserHabitAnalyzer();
        
        // Observer les données
        observeTaskData();
        observeScheduleData();
        
        // Configurer le bouton de rafraîchissement
        binding.buttonRefreshStats.setOnClickListener(v -> {
            refreshStats();
        });
    }

    private void observeTaskData() {
        // Observer le nombre de tâches complétées et incomplètes
        taskViewModel.getCompletedTasks().observe(getViewLifecycleOwner(), completedTasks -> {
            taskViewModel.getIncompleteTasks().observe(getViewLifecycleOwner(), incompleteTasks -> {
                int completed = completedTasks != null ? completedTasks.size() : 0;
                int total = completed + (incompleteTasks != null ? incompleteTasks.size() : 0);
                binding.textTasksCompleted.setText("Tâches complétées: " + completed + "/" + total);
            });
        });
        
        // Observer les tâches en retard
        taskViewModel.getOverdueTaskCount().observe(getViewLifecycleOwner(), overdueCount -> {
            // Cette information pourrait être utilisée pour afficher des alertes ou des conseils
        });
    }

    private void observeScheduleData() {
        // Observer le score de productivité moyen
        scheduleViewModel.getAverageProductivityScore().observe(getViewLifecycleOwner(), averageScore -> {
            if (averageScore != null) {
                int score = Math.round(averageScore);
                binding.textProductivityScore.setText("Score de productivité: " + score + "/100");
                binding.progressProductivity.setProgress(score);
            } else {
                binding.textProductivityScore.setText("Score de productivité: 0/100");
                binding.progressProductivity.setProgress(0);
            }
        });
    }

    private void refreshStats() {
        // Analyser les habitudes de l'utilisateur
        aiService.analyzeUserHabits();
        
        // Observer l'état de l'analyse
        aiService.getIsAnalyzing().observe(getViewLifecycleOwner(), isAnalyzing -> {
            if (isAnalyzing) {
                binding.buttonRefreshStats.setEnabled(false);
                binding.buttonRefreshStats.setText("Analyse en cours...");
            } else {
                binding.buttonRefreshStats.setEnabled(true);
                binding.buttonRefreshStats.setText("Rafraîchir les statistiques");
                updateHabitStats();
            }
        });
    }

    private void updateHabitStats() {
        // Récupérer les heures les plus productives
        int[] productiveHours = habitAnalyzer.getMostProductiveHours();
        if (productiveHours.length > 0) {
            StringBuilder hoursText = new StringBuilder("Heures les plus productives: ");
            for (int i = 0; i < Math.min(productiveHours.length, 3); i++) {
                hoursText.append(productiveHours[i]).append("h");
                if (i < Math.min(productiveHours.length, 3) - 1) {
                    hoursText.append(", ");
                }
            }
            binding.textProductiveHours.setText(hoursText.toString());
        }
        
        // Récupérer les jours les plus productifs
        int[] productiveDays = habitAnalyzer.getMostProductiveDays();
        if (productiveDays.length > 0) {
            String[] dayNames = {"dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"};
            StringBuilder daysText = new StringBuilder("Jours les plus productifs: ");
            for (int i = 0; i < Math.min(productiveDays.length, 3); i++) {
                daysText.append(dayNames[productiveDays[i]]);
                if (i < Math.min(productiveDays.length, 3) - 1) {
                    daysText.append(", ");
                }
            }
            binding.textProductiveDays.setText(daysText.toString());
        }
        
        // Récupérer les catégories les plus efficaces
        String[] efficientCategories = habitAnalyzer.getMostEfficientCategories();
        if (efficientCategories.length > 0) {
            StringBuilder categoriesText = new StringBuilder("Catégories les plus efficaces: ");
            for (int i = 0; i < Math.min(efficientCategories.length, 3); i++) {
                categoriesText.append(efficientCategories[i]);
                if (i < Math.min(efficientCategories.length, 3) - 1) {
                    categoriesText.append(", ");
                }
            }
            binding.textEfficientCategories.setText(categoriesText.toString());
        }
        
        // Générer des conseils personnalisés
        generateAITips();
    }

    private void generateAITips() {
        StringBuilder tips = new StringBuilder();
        
        // Conseil basé sur les heures productives
        int[] productiveHours = habitAnalyzer.getMostProductiveHours();
        if (productiveHours.length > 0) {
            tips.append("• Vous êtes plus productif(ve) vers ").append(productiveHours[0])
                .append("h. Planifiez vos tâches importantes à ce moment.\n\n");
        }
        
        // Conseil basé sur les jours productifs
        int[] productiveDays = habitAnalyzer.getMostProductiveDays();
        if (productiveDays.length > 0) {
            String[] dayNames = {"dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"};
            tips.append("• Le ").append(dayNames[productiveDays[0]])
                .append(" est votre jour le plus productif. Réservez ce jour pour les tâches complexes.\n\n");
        }
        
        // Conseil général sur les pauses
        tips.append("• N'oubliez pas de faire des pauses régulières pour maintenir votre concentration.");
        
        binding.textAiTips.setText(tips.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
