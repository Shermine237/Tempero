package com.shermine237.tempora.ai.backend;

import java.util.Date;

/**
 * Classe représentant une activité utilisateur
 * Utilisée pour l'analyse des habitudes et la prédiction des durées
 */
public class UserActivity {
    private String title;
    private String description;
    private String category;
    private Date startTime;
    private Date endTime;
    private float productivityScore; // Score de 0 à 5
    private boolean completed;
    
    /**
     * Constructeur
     * @param title Titre de l'activité
     * @param description Description de l'activité
     * @param category Catégorie de l'activité
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @param productivityScore Score de productivité (0-5)
     * @param completed Si l'activité a été complétée
     */
    public UserActivity(String title, String description, String category, 
                        Date startTime, Date endTime, float productivityScore, boolean completed) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.productivityScore = productivityScore;
        this.completed = completed;
    }
    
    // Getters et setters
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public float getProductivityScore() {
        return productivityScore;
    }
    
    public void setProductivityScore(float productivityScore) {
        this.productivityScore = productivityScore;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
