package com.shermine237.tempora.model;

import androidx.room.Entity;

/**
 * Classe représentant les heures de travail pour un jour spécifique.
 * Cette classe est utilisée pour stocker les heures de début et de fin de travail pour chaque jour.
 */
public class WorkHours {
    private int dayOfWeek; // 0 = Dimanche, 1 = Lundi, ..., 6 = Samedi
    private int startHour; // Heure de début (0-23)
    private int endHour;   // Heure de fin (0-23)
    private boolean isWorkDay; // Indique si c'est un jour de travail
    
    /**
     * Constructeur par défaut
     */
    public WorkHours() {
        this.dayOfWeek = 0;
        this.startHour = 9;
        this.endHour = 17;
        this.isWorkDay = false;
    }
    
    /**
     * Constructeur avec paramètres
     * @param dayOfWeek Jour de la semaine (0-6)
     * @param startHour Heure de début (0-23)
     * @param endHour Heure de fin (0-23)
     * @param isWorkDay Indique si c'est un jour de travail
     */
    public WorkHours(int dayOfWeek, int startHour, int endHour, boolean isWorkDay) {
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
        this.endHour = endHour;
        this.isWorkDay = isWorkDay;
    }
    
    // Getters et Setters
    public int getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public int getStartHour() {
        return startHour;
    }
    
    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }
    
    public int getEndHour() {
        return endHour;
    }
    
    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }
    
    public boolean isWorkDay() {
        return isWorkDay;
    }
    
    public void setWorkDay(boolean workDay) {
        isWorkDay = workDay;
    }
    
    /**
     * Retourne le nom du jour en français
     * @return Nom du jour
     */
    public String getDayName() {
        switch (dayOfWeek) {
            case 0: return "Dimanche";
            case 1: return "Lundi";
            case 2: return "Mardi";
            case 3: return "Mercredi";
            case 4: return "Jeudi";
            case 5: return "Vendredi";
            case 6: return "Samedi";
            default: return "Inconnu";
        }
    }
    
    /**
     * Retourne l'heure de début formatée (ex: "09:00")
     * @return Heure de début formatée
     */
    public String getFormattedStartHour() {
        return String.format("%02d:00", startHour);
    }
    
    /**
     * Retourne l'heure de fin formatée (ex: "17:00")
     * @return Heure de fin formatée
     */
    public String getFormattedEndHour() {
        return String.format("%02d:00", endHour);
    }
}
