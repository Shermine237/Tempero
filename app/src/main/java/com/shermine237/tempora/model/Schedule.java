package com.shermine237.tempora.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.shermine237.tempora.utils.DateConverter;
import com.shermine237.tempora.utils.ScheduleItemListConverter;

import java.util.Date;
import java.util.List;

/**
 * Entité représentant un planning généré par l'IA dans l'application Tempero.
 * Cette classe est utilisée pour stocker les plannings optimisés proposés à l'utilisateur.
 */
@Entity(tableName = "schedules")
public class Schedule {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @TypeConverters(DateConverter.class)
    private Date date; // Date du planning
    
    @TypeConverters(ScheduleItemListConverter.class)
    private List<ScheduleItem> items; // Liste des éléments du planning
    
    private boolean approved; // Si le planning a été approuvé par l'utilisateur
    private boolean completed; // Si toutes les tâches du planning ont été complétées
    private int productivityScore; // Score de productivité calculé par l'IA (0-100)
    
    @TypeConverters(DateConverter.class)
    private Date generatedAt; // Date de génération du planning
    
    @TypeConverters(DateConverter.class)
    private Date lastModifiedAt; // Date de dernière modification du planning
    
    // Constructeur
    public Schedule(Date date, List<ScheduleItem> items) {
        this.date = date;
        this.items = items;
        this.approved = false;
        this.completed = false;
        this.productivityScore = 0;
        this.generatedAt = new Date();
        this.lastModifiedAt = new Date();
    }
    
    // Getters et Setters
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
        this.lastModifiedAt = new Date();
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
        this.lastModifiedAt = new Date();
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.lastModifiedAt = new Date();
    }
    
    public int getProductivityScore() {
        return productivityScore;
    }
    
    public void setProductivityScore(int productivityScore) {
        this.productivityScore = productivityScore;
    }
    
    public Date getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }
    
    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }
}
