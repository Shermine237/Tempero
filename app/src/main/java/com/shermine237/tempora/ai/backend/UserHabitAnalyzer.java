package com.shermine237.tempora.ai.backend;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyseur d'habitudes utilisateur
 * Cette classe est responsable de l'analyse des habitudes de l'utilisateur
 * pour déterminer ses périodes de productivité, ses préférences, etc.
 */
public class UserHabitAnalyzer {

    // Stockage des données d'activité de l'utilisateur
    private List<UserActivity> userActivities;
    
    // Résultats d'analyse
    private Map<Integer, Float> productivityByDayOfWeek; // 0=Dimanche, 1=Lundi, etc.
    private Map<Integer, Float> productivityByHourOfDay; // 0-23 heures
    private Map<String, Float> productivityByTaskCategory;
    private Map<String, Float> averageTaskDurations;
    
    // Nouvelles structures pour l'apprentissage avancé
    private Map<String, Integer> taskCompletionCounts; // Nombre de fois qu'une tâche a été complétée
    private Map<String, Integer> taskPostponementCounts; // Nombre de fois qu'une tâche a été reportée
    private Map<String, List<Integer>> preferredDaysForTasks; // Jours préférés pour chaque type de tâche
    private Map<String, List<Integer>> preferredHoursForTasks; // Heures préférées pour chaque type de tâche
    private Map<String, Float> taskSuccessRates; // Taux de réussite pour chaque type de tâche
    
    /**
     * Constructeur
     */
    public UserHabitAnalyzer() {
        userActivities = new ArrayList<>();
        productivityByDayOfWeek = new HashMap<>();
        productivityByHourOfDay = new HashMap<>();
        productivityByTaskCategory = new HashMap<>();
        averageTaskDurations = new HashMap<>();
        
        // Initialiser les nouvelles structures
        taskCompletionCounts = new HashMap<>();
        taskPostponementCounts = new HashMap<>();
        preferredDaysForTasks = new HashMap<>();
        preferredHoursForTasks = new HashMap<>();
        taskSuccessRates = new HashMap<>();
        
        // Initialiser les maps
        for (int i = 0; i < 7; i++) {
            productivityByDayOfWeek.put(i, 0.0f);
        }
        
        for (int i = 0; i < 24; i++) {
            productivityByHourOfDay.put(i, 0.0f);
        }
    }
    
    /**
     * Ajoute une activité utilisateur à analyser
     * @param activity Activité utilisateur
     */
    public void addUserActivity(UserActivity activity) {
        userActivities.add(activity);
        analyzeActivity(activity);
    }
    
    /**
     * Analyse une activité utilisateur
     * @param activity Activité à analyser
     */
    private void analyzeActivity(UserActivity activity) {
        // Extraire le jour de la semaine
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(activity.getStartTime());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0=Dimanche, 1=Lundi, etc.
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Mettre à jour les scores de productivité
        if (activity.getProductivityScore() > 0) {
            // Jour de la semaine
            float currentDayScore = productivityByDayOfWeek.get(dayOfWeek);
            int dayCount = 1;
            for (UserActivity a : userActivities) {
                Calendar c = Calendar.getInstance();
                c.setTime(a.getStartTime());
                if (c.get(Calendar.DAY_OF_WEEK) - 1 == dayOfWeek) {
                    dayCount++;
                }
            }
            productivityByDayOfWeek.put(dayOfWeek, 
                    (currentDayScore * (dayCount - 1) + activity.getProductivityScore()) / dayCount);
            
            // Heure de la journée
            float currentHourScore = productivityByHourOfDay.get(hourOfDay);
            int hourCount = 1;
            for (UserActivity a : userActivities) {
                Calendar c = Calendar.getInstance();
                c.setTime(a.getStartTime());
                if (c.get(Calendar.HOUR_OF_DAY) == hourOfDay) {
                    hourCount++;
                }
            }
            productivityByHourOfDay.put(hourOfDay, 
                    (currentHourScore * (hourCount - 1) + activity.getProductivityScore()) / hourCount);
            
            // Catégorie de tâche
            String category = activity.getCategory();
            float currentCategoryScore = productivityByTaskCategory.getOrDefault(category, 0.0f);
            int categoryCount = 1;
            for (UserActivity a : userActivities) {
                if (a.getCategory().equals(category)) {
                    categoryCount++;
                }
            }
            productivityByTaskCategory.put(category, 
                    (currentCategoryScore * (categoryCount - 1) + activity.getProductivityScore()) / categoryCount);
        }
        
        // Mettre à jour les durées moyennes des tâches
        String taskTitle = activity.getTitle();
        long durationMinutes = (activity.getEndTime().getTime() - activity.getStartTime().getTime()) / (60 * 1000);
        float currentAvgDuration = averageTaskDurations.getOrDefault(taskTitle, 0.0f);
        int taskCount = 1;
        for (UserActivity a : userActivities) {
            if (a.getTitle().equals(taskTitle)) {
                taskCount++;
            }
        }
        averageTaskDurations.put(taskTitle, 
                (currentAvgDuration * (taskCount - 1) + durationMinutes) / taskCount);
    }
    
    /**
     * Retourne le jour de la semaine le plus productif
     * @return Jour de la semaine (0=Dimanche, 1=Lundi, etc.)
     */
    public int getMostProductiveDay() {
        int bestDay = 0;
        float bestScore = -1;
        
        for (Map.Entry<Integer, Float> entry : productivityByDayOfWeek.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestDay = entry.getKey();
            }
        }
        
        return bestDay;
    }
    
    /**
     * Retourne l'heure de la journée la plus productive
     * @return Heure de la journée (0-23)
     */
    public int getMostProductiveHour() {
        int bestHour = 0;
        float bestScore = -1;
        
        for (Map.Entry<Integer, Float> entry : productivityByHourOfDay.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestHour = entry.getKey();
            }
        }
        
        return bestHour;
    }
    
    /**
     * Prédit la durée d'une tâche en fonction de son titre et de sa catégorie
     * @param taskTitle Titre de la tâche
     * @param category Catégorie de la tâche
     * @return Durée prédite en minutes
     */
    public int predictTaskDuration(String taskTitle, String category) {
        // Si nous avons déjà des données pour cette tâche spécifique
        if (averageTaskDurations.containsKey(taskTitle)) {
            return Math.round(averageTaskDurations.get(taskTitle));
        }
        
        // Sinon, calculer la moyenne des durées pour cette catégorie
        float totalDuration = 0;
        int count = 0;
        
        for (UserActivity activity : userActivities) {
            if (activity.getCategory().equals(category)) {
                long durationMinutes = (activity.getEndTime().getTime() - activity.getStartTime().getTime()) / (60 * 1000);
                totalDuration += durationMinutes;
                count++;
            }
        }
        
        if (count > 0) {
            return Math.round(totalDuration / count);
        }
        
        // Valeur par défaut si aucune donnée n'est disponible
        return 60; // 1 heure par défaut
    }
    
    /**
     * Génère un conseil de productivité basé sur l'analyse des habitudes
     * @return Conseil de productivité
     */
    public String generateProductivityTip() {
        int bestDay = getMostProductiveDay();
        int bestHour = getMostProductiveHour();
        
        String[] dayNames = {"dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"};
        
        return String.format(
                "Vous êtes plus productif(ve) le %s vers %dh. Planifiez vos tâches importantes à ce moment.",
                dayNames[bestDay], bestHour);
    }
    
    /**
     * Enregistre qu'une tâche a été complétée
     * @param taskTitle Titre de la tâche
     * @param category Catégorie de la tâche
     * @param completionDate Date de complétion
     */
    public void recordTaskCompletion(String taskTitle, String category, java.util.Date completionDate) {
        // Incrémenter le compteur de complétion
        int count = taskCompletionCounts.getOrDefault(taskTitle, 0);
        taskCompletionCounts.put(taskTitle, count + 1);
        
        // Mettre à jour le taux de réussite
        updateTaskSuccessRate(taskTitle);
        
        // Enregistrer le jour et l'heure de complétion comme préférés
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(completionDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0=Dimanche, 1=Lundi, etc.
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Jours préférés
        List<Integer> preferredDays = preferredDaysForTasks.getOrDefault(taskTitle, new ArrayList<>());
        preferredDays.add(dayOfWeek);
        preferredDaysForTasks.put(taskTitle, preferredDays);
        
        // Heures préférées
        List<Integer> preferredHours = preferredHoursForTasks.getOrDefault(taskTitle, new ArrayList<>());
        preferredHours.add(hourOfDay);
        preferredHoursForTasks.put(taskTitle, preferredHours);
    }
    
    /**
     * Enregistre qu'une tâche a été reportée
     * @param taskTitle Titre de la tâche
     */
    public void recordTaskPostponement(String taskTitle) {
        // Incrémenter le compteur de report
        int count = taskPostponementCounts.getOrDefault(taskTitle, 0);
        taskPostponementCounts.put(taskTitle, count + 1);
        
        // Mettre à jour le taux de réussite
        updateTaskSuccessRate(taskTitle);
    }
    
    /**
     * Met à jour le taux de réussite d'une tâche
     * @param taskTitle Titre de la tâche
     */
    private void updateTaskSuccessRate(String taskTitle) {
        int completions = taskCompletionCounts.getOrDefault(taskTitle, 0);
        int postponements = taskPostponementCounts.getOrDefault(taskTitle, 0);
        
        if (completions + postponements > 0) {
            float successRate = (float) completions / (completions + postponements);
            taskSuccessRates.put(taskTitle, successRate);
        }
    }
    
    /**
     * Obtient le jour préféré pour une tâche spécifique
     * @param taskTitle Titre de la tâche
     * @return Jour préféré (0=Dimanche, 1=Lundi, etc.) ou -1 si aucune donnée
     */
    public int getPreferredDayForTask(String taskTitle) {
        List<Integer> preferredDays = preferredDaysForTasks.get(taskTitle);
        
        if (preferredDays == null || preferredDays.isEmpty()) {
            return -1;
        }
        
        // Calculer le jour le plus fréquent
        Map<Integer, Integer> dayCounts = new HashMap<>();
        for (int day : preferredDays) {
            int count = dayCounts.getOrDefault(day, 0);
            dayCounts.put(day, count + 1);
        }
        
        int bestDay = -1;
        int bestCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : dayCounts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestDay = entry.getKey();
            }
        }
        
        return bestDay;
    }
    
    /**
     * Obtient l'heure préférée pour une tâche spécifique
     * @param taskTitle Titre de la tâche
     * @return Heure préférée (0-23) ou -1 si aucune donnée
     */
    public int getPreferredHourForTask(String taskTitle) {
        List<Integer> preferredHours = preferredHoursForTasks.get(taskTitle);
        
        if (preferredHours == null || preferredHours.isEmpty()) {
            return -1;
        }
        
        // Calculer l'heure la plus fréquente
        Map<Integer, Integer> hourCounts = new HashMap<>();
        for (int hour : preferredHours) {
            int count = hourCounts.getOrDefault(hour, 0);
            hourCounts.put(hour, count + 1);
        }
        
        int bestHour = -1;
        int bestCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : hourCounts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestHour = entry.getKey();
            }
        }
        
        return bestHour;
    }
    
    /**
     * Obtient le taux de réussite d'une tâche
     * @param taskTitle Titre de la tâche
     * @return Taux de réussite (0.0-1.0) ou -1.0 si aucune donnée
     */
    public float getTaskSuccessRate(String taskTitle) {
        return taskSuccessRates.getOrDefault(taskTitle, -1.0f);
    }
}
