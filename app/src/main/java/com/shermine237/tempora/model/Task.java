package com.shermine237.tempora.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.shermine237.tempora.utils.DateConverter;

import java.util.Date;

/**
 * Entité représentant une tâche dans l'application Tempero.
 * Cette classe est utilisée pour stocker les informations relatives aux tâches de l'utilisateur.
 */
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String description;
    
    @TypeConverters(DateConverter.class)
    private Date dueDate;
    
    @TypeConverters(DateConverter.class)
    private Date startDate;
    
    @TypeConverters(DateConverter.class)
    private Date scheduledDate;
    
    @TypeConverters(DateConverter.class)
    private Date completionDate;
    
    private int priority; // 1-5, 5 étant la plus haute priorité
    private int difficulty; // 1-5, 5 étant la plus difficile
    private int estimatedDuration; // en minutes
    private int actualDuration; // en minutes
    private boolean completed;
    private boolean recurring;
    private String recurrencePattern; // daily, weekly, monthly, etc.
    private String category; // travail, personnel, études, etc.
    private boolean approved; // Indique si la tâche a été approuvée
    private boolean aiGenerated; // Attribut pour suivre l'origine de la tâche (IA ou manuelle)
    
    // Constructeur par défaut
    public Task() {
        this.title = "";
        this.description = "";
        this.scheduledDate = null;
        this.dueDate = null;
        this.priority = 3;
        this.estimatedDuration = 30;
        this.category = "Autre";
        this.completed = false;
        this.approved = true; // Par défaut, les tâches sont approuvées
        this.aiGenerated = false; // Par défaut, les tâches ne sont pas générées par l'IA
    }
    
    // Constructeur
    @Ignore
    public Task(String title, String description, Date dueDate, int priority, int difficulty, 
               int estimatedDuration, String category) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.difficulty = difficulty;
        this.estimatedDuration = estimatedDuration;
        this.category = category;
        this.completed = false;
        this.recurring = false;
        this.approved = false; // Par défaut, les tâches ne sont pas approuvées
        this.aiGenerated = false; // Par défaut, les tâches ne sont pas générées par l'IA
    }
    
    // Getters et Setters
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
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public Date getCompletionDate() {
        return completionDate;
    }
    
    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    public int getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
    
    public int getActualDuration() {
        return actualDuration;
    }
    
    public void setActualDuration(int actualDuration) {
        this.actualDuration = actualDuration;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public boolean isRecurring() {
        return recurring;
    }
    
    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
    
    public String getRecurrencePattern() {
        return recurrencePattern;
    }
    
    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public boolean isAiGenerated() {
        return aiGenerated;
    }
    
    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
}
