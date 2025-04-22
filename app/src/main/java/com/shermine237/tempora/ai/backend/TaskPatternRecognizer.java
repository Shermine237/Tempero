package com.shermine237.tempora.ai.backend;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reconnaissance de modèles pour les tâches
 * Ce service identifie des schémas récurrents dans les tâches de l'utilisateur
 */
public class TaskPatternRecognizer {
    
    private static final String TAG = "TaskPatternRecognizer";
    
    // Types de modèles
    public static final int PATTERN_DAILY = 0;
    public static final int PATTERN_WEEKLY = 1;
    public static final int PATTERN_MONTHLY = 2;
    public static final int PATTERN_SEQUENTIAL = 3;
    
    // Stockage des modèles identifiés
    private Map<String, List<TaskPattern>> taskPatterns;
    
    /**
     * Constructeur
     */
    public TaskPatternRecognizer() {
        taskPatterns = new HashMap<>();
        Log.i(TAG, "Service de reconnaissance de modèles initialisé");
    }
    
    /**
     * Analyse un historique de tâches pour identifier des modèles
     * @param taskHistory Historique des tâches
     */
    public void analyzeTaskHistory(List<UserActivity> taskHistory) {
        if (taskHistory == null || taskHistory.isEmpty()) {
            Log.d(TAG, "Aucun historique de tâches à analyser");
            return;
        }
        
        Log.d(TAG, "Analyse de " + taskHistory.size() + " tâches pour identifier des modèles");
        
        // Regrouper les tâches par titre
        Map<String, List<UserActivity>> tasksByTitle = new HashMap<>();
        
        for (UserActivity activity : taskHistory) {
            String title = activity.getTitle();
            if (!tasksByTitle.containsKey(title)) {
                tasksByTitle.put(title, new ArrayList<>());
            }
            tasksByTitle.get(title).add(activity);
        }
        
        // Analyser chaque groupe de tâches
        for (Map.Entry<String, List<UserActivity>> entry : tasksByTitle.entrySet()) {
            String taskTitle = entry.getKey();
            List<UserActivity> activities = entry.getValue();
            
            // Ignorer les tâches avec trop peu d'occurrences
            if (activities.size() < 3) {
                continue;
            }
            
            // Identifier les modèles pour cette tâche
            List<TaskPattern> patterns = identifyPatterns(taskTitle, activities);
            
            if (!patterns.isEmpty()) {
                taskPatterns.put(taskTitle, patterns);
                Log.d(TAG, "Modèles identifiés pour la tâche '" + taskTitle + "': " + patterns.size());
            }
        }
    }
    
    /**
     * Identifie des modèles pour une tâche spécifique
     * @param taskTitle Titre de la tâche
     * @param activities Activités liées à cette tâche
     * @return Liste des modèles identifiés
     */
    private List<TaskPattern> identifyPatterns(String taskTitle, List<UserActivity> activities) {
        List<TaskPattern> patterns = new ArrayList<>();
        
        // Vérifier les modèles quotidiens
        TaskPattern dailyPattern = identifyDailyPattern(taskTitle, activities);
        if (dailyPattern != null) {
            patterns.add(dailyPattern);
        }
        
        // Vérifier les modèles hebdomadaires
        TaskPattern weeklyPattern = identifyWeeklyPattern(taskTitle, activities);
        if (weeklyPattern != null) {
            patterns.add(weeklyPattern);
        }
        
        // Vérifier les modèles mensuels
        TaskPattern monthlyPattern = identifyMonthlyPattern(taskTitle, activities);
        if (monthlyPattern != null) {
            patterns.add(monthlyPattern);
        }
        
        // Vérifier les modèles séquentiels
        TaskPattern sequentialPattern = identifySequentialPattern(taskTitle, activities);
        if (sequentialPattern != null) {
            patterns.add(sequentialPattern);
        }
        
        return patterns;
    }
    
    /**
     * Identifie un modèle quotidien
     * @param taskTitle Titre de la tâche
     * @param activities Activités liées à cette tâche
     * @return Modèle identifié ou null si aucun modèle n'est trouvé
     */
    private TaskPattern identifyDailyPattern(String taskTitle, List<UserActivity> activities) {
        // Compter les occurrences par heure de la journée
        Map<Integer, Integer> hourCounts = new HashMap<>();
        
        for (UserActivity activity : activities) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(activity.getStartTime());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            
            int count = hourCounts.getOrDefault(hour, 0);
            hourCounts.put(hour, count + 1);
        }
        
        // Trouver l'heure la plus fréquente
        int bestHour = -1;
        int bestCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : hourCounts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestHour = entry.getKey();
            }
        }
        
        // Si au moins 50% des occurrences sont à la même heure
        if (bestCount >= activities.size() * 0.5) {
            TaskPattern pattern = new TaskPattern(taskTitle, PATTERN_DAILY);
            pattern.setHourOfDay(bestHour);
            pattern.setConfidence((float) bestCount / activities.size());
            
            Log.d(TAG, "Modèle quotidien identifié pour '" + taskTitle + "' à " + bestHour + "h avec une confiance de " + pattern.getConfidence());
            
            return pattern;
        }
        
        return null;
    }
    
    /**
     * Identifie un modèle hebdomadaire
     * @param taskTitle Titre de la tâche
     * @param activities Activités liées à cette tâche
     * @return Modèle identifié ou null si aucun modèle n'est trouvé
     */
    private TaskPattern identifyWeeklyPattern(String taskTitle, List<UserActivity> activities) {
        // Compter les occurrences par jour de la semaine
        Map<Integer, Integer> dayOfWeekCounts = new HashMap<>();
        
        for (UserActivity activity : activities) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(activity.getStartTime());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            
            int count = dayOfWeekCounts.getOrDefault(dayOfWeek, 0);
            dayOfWeekCounts.put(dayOfWeek, count + 1);
        }
        
        // Trouver le jour le plus fréquent
        int bestDay = -1;
        int bestCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : dayOfWeekCounts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestDay = entry.getKey();
            }
        }
        
        // Si au moins 50% des occurrences sont le même jour
        if (bestCount >= activities.size() * 0.5) {
            TaskPattern pattern = new TaskPattern(taskTitle, PATTERN_WEEKLY);
            pattern.setDayOfWeek(bestDay);
            pattern.setConfidence((float) bestCount / activities.size());
            
            Log.d(TAG, "Modèle hebdomadaire identifié pour '" + taskTitle + "' le jour " + bestDay + " avec une confiance de " + pattern.getConfidence());
            
            return pattern;
        }
        
        return null;
    }
    
    /**
     * Identifie un modèle mensuel
     * @param taskTitle Titre de la tâche
     * @param activities Activités liées à cette tâche
     * @return Modèle identifié ou null si aucun modèle n'est trouvé
     */
    private TaskPattern identifyMonthlyPattern(String taskTitle, List<UserActivity> activities) {
        // Compter les occurrences par jour du mois
        Map<Integer, Integer> dayOfMonthCounts = new HashMap<>();
        
        for (UserActivity activity : activities) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(activity.getStartTime());
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            
            int count = dayOfMonthCounts.getOrDefault(dayOfMonth, 0);
            dayOfMonthCounts.put(dayOfMonth, count + 1);
        }
        
        // Trouver le jour le plus fréquent
        int bestDay = -1;
        int bestCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : dayOfMonthCounts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestDay = entry.getKey();
            }
        }
        
        // Si au moins 50% des occurrences sont le même jour du mois
        if (bestCount >= activities.size() * 0.5) {
            TaskPattern pattern = new TaskPattern(taskTitle, PATTERN_MONTHLY);
            pattern.setDayOfMonth(bestDay);
            pattern.setConfidence((float) bestCount / activities.size());
            
            Log.d(TAG, "Modèle mensuel identifié pour '" + taskTitle + "' le " + bestDay + " du mois avec une confiance de " + pattern.getConfidence());
            
            return pattern;
        }
        
        return null;
    }
    
    /**
     * Identifie un modèle séquentiel (tâches qui se suivent régulièrement)
     * @param taskTitle Titre de la tâche
     * @param activities Activités liées à cette tâche
     * @return Modèle identifié ou null si aucun modèle n'est trouvé
     */
    private TaskPattern identifySequentialPattern(String taskTitle, List<UserActivity> activities) {
        // Ce type de modèle nécessiterait une analyse plus complexe
        // Pour cette démo, nous retournons null
        return null;
    }
    
    /**
     * Prédit la prochaine occurrence d'une tâche en fonction des modèles identifiés
     * @param taskTitle Titre de la tâche
     * @param referenceDate Date de référence
     * @return Date prédite pour la prochaine occurrence, ou null si aucune prédiction n'est possible
     */
    public Date predictNextOccurrence(String taskTitle, Date referenceDate) {
        // Vérifier si nous avons des modèles pour cette tâche
        List<TaskPattern> patterns = taskPatterns.get(taskTitle);
        
        if (patterns == null || patterns.isEmpty()) {
            Log.d(TAG, "Aucun modèle trouvé pour la tâche '" + taskTitle + "'");
            return null;
        }
        
        // Trouver le modèle avec la plus grande confiance
        TaskPattern bestPattern = null;
        float bestConfidence = 0;
        
        for (TaskPattern pattern : patterns) {
            if (pattern.getConfidence() > bestConfidence) {
                bestConfidence = pattern.getConfidence();
                bestPattern = pattern;
            }
        }
        
        if (bestPattern == null) {
            return null;
        }
        
        // Prédire la prochaine occurrence en fonction du type de modèle
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(referenceDate);
        
        switch (bestPattern.getPatternType()) {
            case PATTERN_DAILY:
                // Définir l'heure prédite
                calendar.set(Calendar.HOUR_OF_DAY, bestPattern.getHourOfDay());
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                
                // Si l'heure est déjà passée, passer au jour suivant
                if (calendar.getTime().before(referenceDate)) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;
                
            case PATTERN_WEEKLY:
                // Définir le jour de la semaine prédit
                int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int targetDayOfWeek = bestPattern.getDayOfWeek();
                
                // Calculer le nombre de jours à ajouter
                int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;
                if (daysToAdd == 0) {
                    // Si c'est le même jour, passer à la semaine suivante
                    daysToAdd = 7;
                }
                
                calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
                
                // Définir l'heure (par défaut 9h)
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                break;
                
            case PATTERN_MONTHLY:
                // Définir le jour du mois prédit
                int targetDayOfMonth = bestPattern.getDayOfMonth();
                int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                
                // Ajuster si le jour cible est supérieur au nombre de jours dans le mois
                targetDayOfMonth = Math.min(targetDayOfMonth, maxDayOfMonth);
                
                if (targetDayOfMonth < currentDayOfMonth) {
                    // Passer au mois suivant
                    calendar.add(Calendar.MONTH, 1);
                }
                
                calendar.set(Calendar.DAY_OF_MONTH, targetDayOfMonth);
                
                // Définir l'heure (par défaut 9h)
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                break;
                
            default:
                return null;
        }
        
        Log.d(TAG, "Prochaine occurrence prédite pour '" + taskTitle + "': " + calendar.getTime());
        
        return calendar.getTime();
    }
    
    /**
     * Vérifie si une tâche suit un modèle récurrent
     * @param taskTitle Titre de la tâche
     * @return true si la tâche suit un modèle, false sinon
     */
    public boolean hasRecurringPattern(String taskTitle) {
        List<TaskPattern> patterns = taskPatterns.get(taskTitle);
        return patterns != null && !patterns.isEmpty();
    }
    
    /**
     * Obtient la description textuelle du modèle principal d'une tâche
     * @param taskTitle Titre de la tâche
     * @return Description du modèle ou null si aucun modèle n'est trouvé
     */
    public String getPatternDescription(String taskTitle) {
        // Vérifier si nous avons des modèles pour cette tâche
        List<TaskPattern> patterns = taskPatterns.get(taskTitle);
        
        if (patterns == null || patterns.isEmpty()) {
            return null;
        }
        
        // Trouver le modèle avec la plus grande confiance
        TaskPattern bestPattern = null;
        float bestConfidence = 0;
        
        for (TaskPattern pattern : patterns) {
            if (pattern.getConfidence() > bestConfidence) {
                bestConfidence = pattern.getConfidence();
                bestPattern = pattern;
            }
        }
        
        if (bestPattern == null) {
            return null;
        }
        
        // Générer une description en fonction du type de modèle
        switch (bestPattern.getPatternType()) {
            case PATTERN_DAILY:
                return String.format("Tous les jours à %dh", bestPattern.getHourOfDay());
                
            case PATTERN_WEEKLY:
                String[] dayNames = {"", "dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"};
                return String.format("Tous les %s", dayNames[bestPattern.getDayOfWeek()]);
                
            case PATTERN_MONTHLY:
                return String.format("Le %d de chaque mois", bestPattern.getDayOfMonth());
                
            default:
                return null;
        }
    }
    
    /**
     * Classe représentant un modèle de tâche
     */
    public class TaskPattern {
        private String taskTitle;
        private int patternType;
        private int hourOfDay;
        private int dayOfWeek;
        private int dayOfMonth;
        private float confidence;
        
        public TaskPattern(String taskTitle, int patternType) {
            this.taskTitle = taskTitle;
            this.patternType = patternType;
            this.confidence = 0.0f;
        }
        
        public String getTaskTitle() {
            return taskTitle;
        }
        
        public int getPatternType() {
            return patternType;
        }
        
        public int getHourOfDay() {
            return hourOfDay;
        }
        
        public void setHourOfDay(int hourOfDay) {
            this.hourOfDay = hourOfDay;
        }
        
        public int getDayOfWeek() {
            return dayOfWeek;
        }
        
        public void setDayOfWeek(int dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }
        
        public int getDayOfMonth() {
            return dayOfMonth;
        }
        
        public void setDayOfMonth(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }
        
        public float getConfidence() {
            return confidence;
        }
        
        public void setConfidence(float confidence) {
            this.confidence = confidence;
        }
    }
}
