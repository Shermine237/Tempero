package com.shermine237.tempora.ai.backend;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyseur de performance des tâches par période de la journée
 * Cette classe implémente un algorithme d'apprentissage automatique simple
 * pour déterminer les meilleures périodes pour différents types de tâches
 */
public class TaskPerformanceAnalyzer {

    // Périodes de la journée
    public static final int MORNING = 0;   // 5h-12h
    public static final int AFTERNOON = 1; // 12h-17h
    public static final int EVENING = 2;   // 17h-22h
    public static final int NIGHT = 3;     // 22h-5h
    
    // Données d'apprentissage
    private Map<String, List<TaskPerformanceData>> taskPerformanceData;
    
    // Résultats d'analyse
    private Map<String, Map<Integer, Float>> taskPerformanceByPeriod;
    
    /**
     * Constructeur
     */
    public TaskPerformanceAnalyzer() {
        taskPerformanceData = new HashMap<>();
        taskPerformanceByPeriod = new HashMap<>();
    }
    
    /**
     * Ajoute une donnée de performance pour une tâche
     * @param taskTitle Titre de la tâche
     * @param taskCategory Catégorie de la tâche
     * @param startTime Heure de début
     * @param durationMinutes Durée en minutes
     * @param productivityScore Score de productivité (0-5)
     */
    public void addTaskPerformance(String taskTitle, String taskCategory, 
                                  java.util.Date startTime, int durationMinutes, 
                                  float productivityScore) {
        // Créer une nouvelle entrée de données
        TaskPerformanceData data = new TaskPerformanceData(
            taskTitle, taskCategory, startTime, durationMinutes, productivityScore);
        
        // Ajouter aux données existantes
        if (!taskPerformanceData.containsKey(taskCategory)) {
            taskPerformanceData.put(taskCategory, new ArrayList<>());
        }
        taskPerformanceData.get(taskCategory).add(data);
        
        // Mettre à jour l'analyse
        updateAnalysis(taskCategory);
    }
    
    /**
     * Met à jour l'analyse pour une catégorie de tâche
     * @param taskCategory Catégorie de tâche
     */
    private void updateAnalysis(String taskCategory) {
        if (!taskPerformanceData.containsKey(taskCategory)) {
            return;
        }
        
        // Initialiser les scores pour chaque période
        Map<Integer, Float> periodScores = new HashMap<>();
        Map<Integer, Integer> periodCounts = new HashMap<>();
        
        for (int i = 0; i < 4; i++) {
            periodScores.put(i, 0.0f);
            periodCounts.put(i, 0);
        }
        
        // Calculer les scores moyens pour chaque période
        for (TaskPerformanceData data : taskPerformanceData.get(taskCategory)) {
            int period = getPeriodOfDay(data.getStartTime());
            
            float currentScore = periodScores.get(period);
            int currentCount = periodCounts.get(period);
            
            // Mettre à jour le score moyen
            periodScores.put(period, 
                (currentScore * currentCount + data.getProductivityScore()) / (currentCount + 1));
            periodCounts.put(period, currentCount + 1);
        }
        
        // Enregistrer les résultats
        taskPerformanceByPeriod.put(taskCategory, periodScores);
    }
    
    /**
     * Détermine la période de la journée pour une heure donnée
     * @param time Heure
     * @return Période (MORNING, AFTERNOON, EVENING, NIGHT)
     */
    private int getPeriodOfDay(java.util.Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 5 && hour < 12) {
            return MORNING;
        } else if (hour >= 12 && hour < 18) {
            return AFTERNOON;
        } else if (hour >= 18 && hour < 22) {
            return EVENING;
        } else {
            return NIGHT;
        }
    }
    
    /**
     * Retourne la meilleure période pour une catégorie de tâche
     * @param taskCategory Catégorie de tâche
     * @return Meilleure période (MORNING, AFTERNOON, EVENING, NIGHT)
     */
    public int getBestPeriodForTaskCategory(String taskCategory) {
        if (!taskPerformanceByPeriod.containsKey(taskCategory)) {
            return MORNING; // Par défaut, le matin est souvent meilleur
        }
        
        Map<Integer, Float> periodScores = taskPerformanceByPeriod.get(taskCategory);
        int bestPeriod = MORNING;
        float bestScore = -1;
        
        for (Map.Entry<Integer, Float> entry : periodScores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestPeriod = entry.getKey();
            }
        }
        
        return bestPeriod;
    }
    
    /**
     * Retourne la durée moyenne pour une tâche spécifique
     * @param taskTitle Titre de la tâche
     * @return Durée moyenne en minutes
     */
    public int getAverageDurationForTask(String taskTitle) {
        int totalDuration = 0;
        int count = 0;
        
        for (List<TaskPerformanceData> dataList : taskPerformanceData.values()) {
            for (TaskPerformanceData data : dataList) {
                if (data.getTaskTitle().equals(taskTitle)) {
                    totalDuration += data.getDurationMinutes();
                    count++;
                }
            }
        }
        
        return count > 0 ? totalDuration / count : 60; // 60 minutes par défaut
    }
    
    /**
     * Génère une recommandation personnalisée pour une tâche
     * @param taskTitle Titre de la tâche
     * @param taskCategory Catégorie de la tâche
     * @return Recommandation personnalisée
     */
    public String generateTaskRecommendation(String taskTitle, String taskCategory) {
        int bestPeriod = getBestPeriodForTaskCategory(taskCategory);
        int avgDuration = getAverageDurationForTask(taskTitle);
        
        String periodName;
        switch (bestPeriod) {
            case MORNING:
                periodName = "le matin";
                break;
            case AFTERNOON:
                periodName = "l'après-midi";
                break;
            case EVENING:
                periodName = "en soirée";
                break;
            case NIGHT:
                periodName = "la nuit";
                break;
            default:
                periodName = "le matin";
        }
        
        return String.format(
            "Vous êtes plus efficace pour les tâches de type '%s' %s. " +
            "Vous mettez généralement %d minutes pour accomplir '%s'. " +
            "Je vous recommande de planifier cette tâche %s pour une meilleure productivité.",
            taskCategory, periodName, avgDuration, taskTitle, periodName);
    }
    
    /**
     * Classe interne pour stocker les données de performance des tâches
     */
    private class TaskPerformanceData {
        private String taskTitle;
        private String taskCategory;
        private java.util.Date startTime;
        private int durationMinutes;
        private float productivityScore;
        
        public TaskPerformanceData(String taskTitle, String taskCategory, 
                                  java.util.Date startTime, int durationMinutes, 
                                  float productivityScore) {
            this.taskTitle = taskTitle;
            this.taskCategory = taskCategory;
            this.startTime = startTime;
            this.durationMinutes = durationMinutes;
            this.productivityScore = productivityScore;
        }
        
        public String getTaskTitle() {
            return taskTitle;
        }
        
        public String getTaskCategory() {
            return taskCategory;
        }
        
        public java.util.Date getStartTime() {
            return startTime;
        }
        
        public int getDurationMinutes() {
            return durationMinutes;
        }
        
        public float getProductivityScore() {
            return productivityScore;
        }
    }
}
