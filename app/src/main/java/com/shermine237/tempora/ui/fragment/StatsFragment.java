package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
        
        // Initialiser le service IA
        aiService = new AIService(requireActivity().getApplication());
        
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
        // Utiliser le backend d'IA pour générer des statistiques et des recommandations
        
        // Récupérer les jours et heures productifs à partir du backend d'IA
        try {
            // Générer un conseil de productivité basé sur l'analyse des habitudes
            String productivityTip = aiService.generateProductivityTip();
            if (productivityTip != null && !productivityTip.isEmpty()) {
                // Extraire les informations du conseil
                if (productivityTip.contains("plus productif")) {
                    // Exemple: "Vous êtes plus productif(ve) le mardi vers 10h."
                    String[] parts = productivityTip.split(" ");
                    
                    // Extraire le jour
                    int dayIndex = -1;
                    for (int i = 0; i < parts.length; i++) {
                        String part = parts[i].toLowerCase();
                        if (part.equals("dimanche") || part.equals("lundi") || 
                            part.equals("mardi") || part.equals("mercredi") || 
                            part.equals("jeudi") || part.equals("vendredi") || 
                            part.equals("samedi")) {
                            dayIndex = i;
                            break;
                        }
                    }
                    
                    if (dayIndex >= 0) {
                        binding.textProductiveDays.setText("Jour le plus productif: " + parts[dayIndex]);
                    }
                    
                    // Extraire l'heure
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].contains("h") && parts[i].length() <= 3) {
                            String hourPart = parts[i].replace("h", "");
                            try {
                                int hour = Integer.parseInt(hourPart);
                                binding.textProductiveHours.setText("Heure la plus productive: " + hour + "h");
                                break;
                            } catch (NumberFormatException e) {
                                // Ignorer
                            }
                        }
                    }
                }
            }
            
            // Générer des recommandations pour les tâches fréquentes
            String[] commonTasks = {"Réunion d'équipe", "Préparation de présentation", "Révisions"};
            String[] categories = {"Travail", "Études", "Personnel"};
            
            StringBuilder efficientCategories = new StringBuilder("Catégories optimales: ");
            for (int i = 0; i < Math.min(categories.length, 2); i++) {
                String recommendation = aiService.generateTaskRecommendation(commonTasks[i], categories[i]);
                if (recommendation != null && !recommendation.isEmpty()) {
                    efficientCategories.append(categories[i]);
                    if (i < Math.min(categories.length, 2) - 1) {
                        efficientCategories.append(", ");
                    }
                }
            }
            binding.textEfficientCategories.setText(efficientCategories.toString());
            
            // Générer des conseils personnalisés
            generateAITips();
            
        } catch (Exception e) {
            // Fallback en cas d'erreur
            binding.textProductiveDays.setText("Jour le plus productif: données insuffisantes");
            binding.textProductiveHours.setText("Heure la plus productive: données insuffisantes");
            binding.textEfficientCategories.setText("Catégories optimales: données insuffisantes");
        }
    }

    private void generateAITips() {
        StringBuilder tips = new StringBuilder();
        
        // Utiliser le backend d'IA pour générer des conseils personnalisés
        String productivityTip = aiService.generateProductivityTip();
        if (productivityTip != null && !productivityTip.isEmpty()) {
            tips.append("• ").append(productivityTip).append("\n\n");
        }
        
        // Ajouter des recommandations pour des tâches spécifiques
        String[] commonTasks = {"Réunion d'équipe", "Préparation de présentation"};
        String[] categories = {"Travail", "Études"};
        
        for (int i = 0; i < Math.min(commonTasks.length, 1); i++) {
            String recommendation = aiService.generateTaskRecommendation(commonTasks[i], categories[i]);
            if (recommendation != null && !recommendation.isEmpty()) {
                tips.append("• ").append(recommendation).append("\n\n");
            }
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
