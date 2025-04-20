package com.shermine237.tempora.ai.backend;

import java.util.Date;
import java.util.List;

/**
 * Classe représentant un planning généré par l'IA
 */
public class Schedule {
    private int id;
    private Date date;
    private List<ScheduleItem> items;
    private boolean approved;
    private boolean completed;
    private float productivityScore; // Score de productivité (0-100)
    
    /**
     * Constructeur par défaut
     */
    public Schedule() {
        this.approved = false;
        this.completed = false;
        this.productivityScore = 0;
    }
    
    /**
     * Constructeur avec date et éléments
     * @param date Date du planning
     * @param items Éléments du planning
     */
    public Schedule(Date date, List<ScheduleItem> items) {
        this.date = date;
        this.items = items;
        this.approved = false;
        this.completed = false;
        this.productivityScore = 0;
    }
    
    // Getters et setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public List<ScheduleItem> getItems() {
        return items;
    }
    
    public void setItems(List<ScheduleItem> items) {
        this.items = items;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public float getProductivityScore() {
        return productivityScore;
    }
    
    public void setProductivityScore(float productivityScore) {
        if (productivityScore >= 0 && productivityScore <= 100) {
            this.productivityScore = productivityScore;
        }
    }
    
    /**
     * Calcule la durée totale du planning en minutes
     * @return Durée totale en minutes
     */
    public int getTotalDurationMinutes() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        int totalMinutes = 0;
        for (ScheduleItem item : items) {
            totalMinutes += item.getDurationMinutes();
        }
        
        return totalMinutes;
    }
    
    /**
     * Vérifie si le planning est surchargé (plus de 8 heures de travail)
     * @return true si le planning est surchargé, false sinon
     */
    public boolean isOverloaded() {
        if (items == null || items.isEmpty()) {
            return false;
        }
        
        int workMinutes = 0;
        for (ScheduleItem item : items) {
            if (item.getType().equals("task")) {
                workMinutes += item.getDurationMinutes();
            }
        }
        
        return workMinutes > 480; // 8 heures = 480 minutes
    }
}
