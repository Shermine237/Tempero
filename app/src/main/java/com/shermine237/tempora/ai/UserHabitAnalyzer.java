package com.shermine237.tempora.ai;

import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.model.UserProfile;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe d'analyse des habitudes de l'utilisateur pour l'application Tempero.
 * Cette classe utilise des algorithmes d'apprentissage automatique simples pour
 * analyser les habitudes de travail de l'utilisateur et fournir des recommandations.
 */
public class UserHabitAnalyzer {
    
    // Constantes
    private static final int HOURS_IN_DAY = 24;
    private static final int DAYS_IN_WEEK = 7;
    
    // Données d'analyse
    private final Map<Integer, Double> hourlyProductivityScore = new HashMap<>();
    private final Map<Integer, Double> dailyProductivityScore = new HashMap<>();
    private final Map<String, Double> categoryEfficiencyScore = new HashMap<>();
    private final Map<String, Integer> averageTaskDuration = new HashMap<>();
    
    // Constructeur
    public UserHabitAnalyzer() {
        // Initialiser les scores par défaut
        for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
            hourlyProductivityScore.put(hour, 0.5); // Score neutre par défaut
        }
        
        for (int day = 0; day < DAYS_IN_WEEK; day++) {
            dailyProductivityScore.put(day, 0.5); // Score neutre par défaut
        }
    }
    
    /**
     * Analyse les tâches complétées pour identifier les habitudes de l'utilisateur
     * @param completedTasks Liste des tâches complétées
     */
    public void analyzeTasks(List<Task> completedTasks) {
        if (completedTasks == null || completedTasks.isEmpty()) {
            return;
        }
        
        // Réinitialiser les scores
        resetScores();
        
        // Analyser chaque tâche complétée
        for (Task task : completedTasks) {
            if (task.getStartDate() != null && task.getCompletionDate() != null) {
                analyzeTaskTiming(task);
                analyzeTaskEfficiency(task);
                analyzeTaskCategory(task);
            }
        }
        
        // Normaliser les scores
        normalizeScores();
    }
    
    /**
     * Analyse le timing d'une tâche pour déterminer les heures et jours productifs
     */
    private void analyzeTaskTiming(Task task) {
        Calendar calendar = Calendar.getInstance();
        
        // Analyser l'heure de début
        calendar.setTime(task.getStartDate());
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Dimanche, 1 = Lundi, etc.
        
        // Calculer l'efficacité (rapport entre durée estimée et durée réelle)
        double efficiency = calculateEfficiency(task);
        
        // Mettre à jour les scores
        hourlyProductivityScore.put(startHour, 
                (hourlyProductivityScore.get(startHour) + efficiency) / 2);
        
        dailyProductivityScore.put(dayOfWeek, 
                (dailyProductivityScore.get(dayOfWeek) + efficiency) / 2);
    }
    
    /**
     * Analyse l'efficacité d'une tâche (rapport entre durée estimée et durée réelle)
     */
    private void analyzeTaskEfficiency(Task task) {
        String category = task.getCategory();
        double efficiency = calculateEfficiency(task);
        
        // Mettre à jour le score d'efficacité pour cette catégorie
        if (categoryEfficiencyScore.containsKey(category)) {
            categoryEfficiencyScore.put(category, 
                    (categoryEfficiencyScore.get(category) + efficiency) / 2);
        } else {
            categoryEfficiencyScore.put(category, efficiency);
        }
    }
    
    /**
     * Analyse la durée moyenne des tâches par catégorie
     */
    private void analyzeTaskCategory(Task task) {
        String category = task.getCategory();
        int actualDuration = task.getActualDuration();
        
        // Mettre à jour la durée moyenne pour cette catégorie
        if (averageTaskDuration.containsKey(category)) {
            int currentAvg = averageTaskDuration.get(category);
            averageTaskDuration.put(category, (currentAvg + actualDuration) / 2);
        } else {
            averageTaskDuration.put(category, actualDuration);
        }
    }
    
    /**
     * Calcule l'efficacité d'une tâche (rapport entre durée estimée et durée réelle)
     * @return Score d'efficacité entre 0 et 1 (1 = parfaitement efficace)
     */
    private double calculateEfficiency(Task task) {
        int estimatedDuration = task.getEstimatedDuration();
        int actualDuration = task.getActualDuration();
        
        if (estimatedDuration <= 0 || actualDuration <= 0) {
            return 0.5; // Valeur neutre par défaut
        }
        
        // Si la tâche a pris moins de temps que prévu, c'est très efficace
        if (actualDuration <= estimatedDuration) {
            return 1.0;
        }
        
        // Sinon, calculer l'efficacité relative
        return Math.max(0.1, estimatedDuration / (double) actualDuration);
    }
    
    /**
     * Réinitialise les scores pour une nouvelle analyse
     */
    private void resetScores() {
        for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
            hourlyProductivityScore.put(hour, 0.5);
        }
        
        for (int day = 0; day < DAYS_IN_WEEK; day++) {
            dailyProductivityScore.put(day, 0.5);
        }
        
        categoryEfficiencyScore.clear();
        averageTaskDuration.clear();
    }
    
    /**
     * Normalise les scores pour qu'ils soient entre 0 et 1
     */
    private void normalizeScores() {
        // Trouver les valeurs min et max pour chaque type de score
        double minHourly = Double.MAX_VALUE;
        double maxHourly = Double.MIN_VALUE;
        
        for (double score : hourlyProductivityScore.values()) {
            minHourly = Math.min(minHourly, score);
            maxHourly = Math.max(maxHourly, score);
        }
        
        // Normaliser les scores horaires
        if (maxHourly > minHourly) {
            for (int hour : hourlyProductivityScore.keySet()) {
                double normalizedScore = (hourlyProductivityScore.get(hour) - minHourly) / (maxHourly - minHourly);
                hourlyProductivityScore.put(hour, normalizedScore);
            }
        }
        
        // Même chose pour les scores journaliers
        double minDaily = Double.MAX_VALUE;
        double maxDaily = Double.MIN_VALUE;
        
        for (double score : dailyProductivityScore.values()) {
            minDaily = Math.min(minDaily, score);
            maxDaily = Math.max(maxDaily, score);
        }
        
        if (maxDaily > minDaily) {
            for (int day : dailyProductivityScore.keySet()) {
                double normalizedScore = (dailyProductivityScore.get(day) - minDaily) / (maxDaily - minDaily);
                dailyProductivityScore.put(day, normalizedScore);
            }
        }
    }
    
    // Getters pour les résultats d'analyse
    
    /**
     * Obtient les heures les plus productives de la journée
     * @return Tableau des 3 heures les plus productives (0-23)
     */
    public int[] getMostProductiveHours() {
        return getTopNKeys(hourlyProductivityScore, 3);
    }
    
    /**
     * Obtient les jours les plus productifs de la semaine
     * @return Tableau des jours les plus productifs (0 = Dimanche, 1 = Lundi, etc.)
     */
    public int[] getMostProductiveDays() {
        return getTopNKeys(dailyProductivityScore, 3);
    }
    
    /**
     * Obtient les catégories dans lesquelles l'utilisateur est le plus efficace
     * @return Tableau des catégories les plus efficaces
     */
    public String[] getMostEfficientCategories() {
        return getTopNStringKeys(categoryEfficiencyScore, 3);
    }
    
    /**
     * Estime la durée d'une tâche en fonction de sa catégorie et des données historiques
     * @param category Catégorie de la tâche
     * @param defaultDuration Durée par défaut si aucune donnée n'est disponible
     * @return Durée estimée en minutes
     */
    public int estimateTaskDuration(String category, int defaultDuration) {
        if (averageTaskDuration.containsKey(category)) {
            return averageTaskDuration.get(category);
        }
        return defaultDuration;
    }
    
    /**
     * Détermine si une heure donnée est productive pour l'utilisateur
     * @param hour Heure à vérifier (0-23)
     * @return true si l'heure est productive (score > 0.6)
     */
    public boolean isProductiveHour(int hour) {
        if (hour < 0 || hour >= HOURS_IN_DAY) {
            return false;
        }
        return hourlyProductivityScore.get(hour) > 0.6;
    }
    
    /**
     * Détermine si un jour donné est productif pour l'utilisateur
     * @param day Jour à vérifier (0 = Dimanche, 1 = Lundi, etc.)
     * @return true si le jour est productif (score > 0.6)
     */
    public boolean isProductiveDay(int day) {
        if (day < 0 || day >= DAYS_IN_WEEK) {
            return false;
        }
        return dailyProductivityScore.get(day) > 0.6;
    }
    
    // Méthodes utilitaires
    
    /**
     * Obtient les N clés avec les valeurs les plus élevées dans une map
     */
    private int[] getTopNKeys(Map<Integer, Double> map, int n) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(n)
                .mapToInt(Map.Entry::getKey)
                .toArray();
    }
    
    /**
     * Obtient les N clés de type String avec les valeurs les plus élevées dans une map
     */
    private String[] getTopNStringKeys(Map<String, Double> map, int n) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }
}
