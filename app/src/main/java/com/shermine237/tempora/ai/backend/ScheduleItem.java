package com.shermine237.tempora.ai.backend;

/**
 * Classe représentant un élément de planning
 * (tâche, pause, repas, etc.)
 */
public class ScheduleItem {
    private String title;
    private String description;
    private String type; // "task", "break", "meal", etc.
    private String startTime; // Format "HH:MM"
    private String endTime; // Format "HH:MM"
    private int durationMinutes;
    private int taskId; // ID de la tâche associée (si type="task")
    private boolean completed;
    
    /**
     * Constructeur par défaut
     */
    public ScheduleItem() {
        this.completed = false;
    }
    
    /**
     * Constructeur avec paramètres
     * @param title Titre de l'élément
     * @param description Description de l'élément
     * @param type Type d'élément
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @param durationMinutes Durée en minutes
     */
    public ScheduleItem(String title, String description, String type, 
                        String startTime, String endTime, int durationMinutes) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.completed = false;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes > 0) {
            this.durationMinutes = durationMinutes;
        }
    }
    
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
