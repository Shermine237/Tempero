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
    
    /**
     * Constructeur
     */
    public UserHabitAnalyzer() {
        userActivities = new ArrayList<>();
        productivityByDayOfWeek = new HashMap<>();
        productivityByHourOfDay = new HashMap<>();
        productivityByTaskCategory = new HashMap<>();
        averageTaskDurations = new HashMap<>();
        
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
}
