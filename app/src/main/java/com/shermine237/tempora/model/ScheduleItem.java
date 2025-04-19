package com.shermine237.tempora.model;

import androidx.room.TypeConverters;

import com.shermine237.tempora.utils.DateConverter;

import java.util.Date;

/**
 * Classe représentant un élément du planning dans l'application Tempero.
 * Cette classe est utilisée pour stocker les informations sur une activité planifiée.
 */
public class ScheduleItem {
    private int taskId; // ID de la tâche associée, -1 si c'est une pause ou un autre type d'élément
    private String title; // Titre de l'élément (nom de la tâche ou "Pause" ou "Déjeuner" etc.)
    
    @TypeConverters(DateConverter.class)
    private Date startTime; // Heure de début
    
    @TypeConverters(DateConverter.class)
    private Date endTime; // Heure de fin
    
    private String type; // Type d'élément: "task", "break", "meal", etc.
    private boolean completed; // Si l'élément a été complété
    
    // Constructeur pour une tâche
    public ScheduleItem(int taskId, String title, Date startTime, Date endTime) {
        this.taskId = taskId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = "task";
        this.completed = false;
    }
    
    // Constructeur pour un autre type d'élément (pause, repas, etc.)
    public ScheduleItem(String title, Date startTime, Date endTime, String type) {
        this.taskId = -1;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.completed = false;
    }
    
    // Getters et Setters
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    // Méthodes utilitaires
    
    /**
     * Calcule la durée de l'élément en minutes
     * @return Durée en minutes
     */
    public int getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long diffMillis = endTime.getTime() - startTime.getTime();
        return (int) (diffMillis / (60 * 1000));
    }
}
