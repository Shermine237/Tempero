package com.shermine237.tempora.ai.backend;

import java.util.Date;

/**
 * Classe représentant une tâche à planifier
 */
public class Task {
    private int id;
    private String title;
    private String description;
    private Date dueDate;
    private Date scheduledDate; // Date planifiée
    private int priority; // 1-5, 5 étant la plus haute priorité
    private int difficulty; // 1-5, 5 étant la plus difficile
    private int estimatedDuration; // en minutes
    private String category;
    private boolean completed;
    
    /**
     * Constructeur par défaut
     */
    public Task() {
        this.title = "";
        this.description = "";
        this.dueDate = null;
        this.scheduledDate = null;
        this.priority = 3;
        this.difficulty = 3;
        this.estimatedDuration = 30;
        this.category = "Autre";
        this.completed = false;
    }
    
    /**
     * Constructeur
     * @param title Titre de la tâche
     * @param description Description de la tâche
     * @param dueDate Date d'échéance
     * @param priority Priorité (1-5)
     * @param difficulty Difficulté (1-5)
     * @param estimatedDuration Durée estimée en minutes
     * @param category Catégorie de la tâche
     */
    public Task(String title, String description, Date dueDate, int priority, 
                int difficulty, int estimatedDuration, String category) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.scheduledDate = null;
        this.priority = priority;
        this.difficulty = difficulty;
        this.estimatedDuration = estimatedDuration;
        this.category = category;
        this.completed = false;
    }
    
    // Getters et setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
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
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public Date getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        if (priority >= 1 && priority <= 5) {
            this.priority = priority;
        }
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        if (difficulty >= 1 && difficulty <= 5) {
            this.difficulty = difficulty;
        }
    }
    
    public int getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(int estimatedDuration) {
        if (estimatedDuration > 0) {
            this.estimatedDuration = estimatedDuration;
        }
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
