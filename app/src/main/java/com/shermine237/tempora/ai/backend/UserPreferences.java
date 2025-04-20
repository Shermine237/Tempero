package com.shermine237.tempora.ai.backend;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant les préférences utilisateur
 * Utilisée pour personnaliser la génération de planning
 */
public class UserPreferences {
    
    // Heures de travail par jour de la semaine (0=Dimanche, 1=Lundi, etc.)
    private Map<Integer, Integer> workStartHours;
    private Map<Integer, Integer> workEndHours;
    
    // Préférences pour les repas
    private boolean includeBreakfast;
    private boolean includeLunch;
    private boolean includeDinner;
    
    // Préférences pour les pauses
    private boolean includeBreaks;
    private int breakDurationMinutes;
    private int workDurationBeforeBreak;
    
    // Préférences pour la difficulté des tâches
    private boolean scheduleDifficultTasksInMorning;
    
    /**
     * Constructeur
     */
    public UserPreferences() {
        workStartHours = new HashMap<>();
        workEndHours = new HashMap<>();
        
        // Valeurs par défaut
        for (int i = 0; i < 7; i++) {
            workStartHours.put(i, 9); // 9h par défaut
            workEndHours.put(i, 18); // 18h par défaut
        }
        
        includeBreakfast = true;
        includeLunch = true;
        includeDinner = true;
        
        includeBreaks = true;
        breakDurationMinutes = 15;
        workDurationBeforeBreak = 120; // 2 heures
        
        scheduleDifficultTasksInMorning = true;
    }
    
    /**
     * Définit l'heure de début de travail pour un jour spécifique
     * @param dayOfWeek Jour de la semaine (0=Dimanche, 1=Lundi, etc.)
     * @param hour Heure de début (0-23)
     */
    public void setWorkStartHour(int dayOfWeek, int hour) {
        if (dayOfWeek >= 0 && dayOfWeek <= 6 && hour >= 0 && hour <= 23) {
            workStartHours.put(dayOfWeek, hour);
        }
    }
    
    /**
     * Définit l'heure de fin de travail pour un jour spécifique
     * @param dayOfWeek Jour de la semaine (0=Dimanche, 1=Lundi, etc.)
     * @param hour Heure de fin (0-23)
     */
    public void setWorkEndHour(int dayOfWeek, int hour) {
        if (dayOfWeek >= 0 && dayOfWeek <= 6 && hour >= 0 && hour <= 23) {
            workEndHours.put(dayOfWeek, hour);
        }
    }
    
    /**
     * Retourne l'heure de début de travail pour un jour spécifique
     * @param dayOfWeek Jour de la semaine (0=Dimanche, 1=Lundi, etc.)
     * @return Heure de début (0-23)
     */
    public int getWorkStartHour(int dayOfWeek) {
        return workStartHours.getOrDefault(dayOfWeek, 9);
    }
    
    /**
     * Retourne l'heure de fin de travail pour un jour spécifique
     * @param dayOfWeek Jour de la semaine (0=Dimanche, 1=Lundi, etc.)
     * @return Heure de fin (0-23)
     */
    public int getWorkEndHour(int dayOfWeek) {
        return workEndHours.getOrDefault(dayOfWeek, 18);
    }
    
    // Getters et setters pour les préférences de repas
    
    public boolean includeBreakfast() {
        return includeBreakfast;
    }
    
    public void setIncludeBreakfast(boolean includeBreakfast) {
        this.includeBreakfast = includeBreakfast;
    }
    
    public boolean includeLunch() {
        return includeLunch;
    }
    
    public void setIncludeLunch(boolean includeLunch) {
        this.includeLunch = includeLunch;
    }
    
    public boolean includeDinner() {
        return includeDinner;
    }
    
    public void setIncludeDinner(boolean includeDinner) {
        this.includeDinner = includeDinner;
    }
    
    // Getters et setters pour les préférences de pauses
    
    public boolean includeBreaks() {
        return includeBreaks;
    }
    
    public void setIncludeBreaks(boolean includeBreaks) {
        this.includeBreaks = includeBreaks;
    }
    
    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }
    
    public void setBreakDurationMinutes(int breakDurationMinutes) {
        if (breakDurationMinutes > 0) {
            this.breakDurationMinutes = breakDurationMinutes;
        }
    }
    
    public int getWorkDurationBeforeBreak() {
        return workDurationBeforeBreak;
    }
    
    public void setWorkDurationBeforeBreak(int workDurationBeforeBreak) {
        if (workDurationBeforeBreak > 0) {
            this.workDurationBeforeBreak = workDurationBeforeBreak;
        }
    }
    
    // Getters et setters pour les préférences de difficulté des tâches
    
    public boolean scheduleDifficultTasksInMorning() {
        return scheduleDifficultTasksInMorning;
    }
    
    public void setScheduleDifficultTasksInMorning(boolean scheduleDifficultTasksInMorning) {
        this.scheduleDifficultTasksInMorning = scheduleDifficultTasksInMorning;
    }
}
