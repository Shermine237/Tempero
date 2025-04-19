package com.shermine237.tempora.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.shermine237.tempora.utils.DateConverter;
import com.shermine237.tempora.utils.StringListConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entité représentant le profil de l'utilisateur dans l'application Tempero.
 * Cette classe est utilisée pour stocker les préférences et les informations de l'utilisateur.
 */
@Entity(tableName = "user_profile")
public class UserProfile {
    @PrimaryKey
    private int id = 1; // Un seul profil utilisateur par installation
    
    private String name;
    private String email;
    
    // Préférences de travail
    private int preferredWorkStartHour; // Heure préférée pour commencer à travailler (0-23)
    private int preferredWorkEndHour; // Heure préférée pour terminer de travailler (0-23)
    
    // Préférences de pause
    private int shortBreakDuration; // Durée des pauses courtes en minutes
    private int longBreakDuration; // Durée des pauses longues en minutes
    private int workSessionsBeforeLongBreak; // Nombre de sessions de travail avant une pause longue
    
    // Jours de travail préférés (0 = Dimanche, 1 = Lundi, ..., 6 = Samedi)
    @TypeConverters(StringListConverter.class)
    private List<Integer> workDays;
    
    // Catégories personnalisées pour les tâches
    @TypeConverters(StringListConverter.class)
    private List<String> customCategories;
    
    // Date de création du profil
    @TypeConverters(DateConverter.class)
    private Date creationDate;
    
    // Constructeur
    public UserProfile(String name, String email) {
        this.name = name;
        this.email = email;
        this.preferredWorkStartHour = 9; // Par défaut: 9h du matin
        this.preferredWorkEndHour = 17; // Par défaut: 17h (5h du soir)
        this.shortBreakDuration = 5; // Par défaut: 5 minutes
        this.longBreakDuration = 15; // Par défaut: 15 minutes
        this.workSessionsBeforeLongBreak = 4; // Par défaut: 4 sessions
        
        // Par défaut: du lundi au vendredi
        this.workDays = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            this.workDays.add(i);
        }
        
        // Catégories par défaut
        this.customCategories = new ArrayList<>();
        this.customCategories.add("Travail");
        this.customCategories.add("Personnel");
        this.customCategories.add("Études");
        this.customCategories.add("Santé");
        
        this.creationDate = new Date();
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getPreferredWorkStartHour() {
        return preferredWorkStartHour;
    }
    
    public void setPreferredWorkStartHour(int preferredWorkStartHour) {
        this.preferredWorkStartHour = preferredWorkStartHour;
    }
    
    public int getPreferredWorkEndHour() {
        return preferredWorkEndHour;
    }
    
    public void setPreferredWorkEndHour(int preferredWorkEndHour) {
        this.preferredWorkEndHour = preferredWorkEndHour;
    }
    
    public int getShortBreakDuration() {
        return shortBreakDuration;
    }
    
    public void setShortBreakDuration(int shortBreakDuration) {
        this.shortBreakDuration = shortBreakDuration;
    }
    
    public int getLongBreakDuration() {
        return longBreakDuration;
    }
    
    public void setLongBreakDuration(int longBreakDuration) {
        this.longBreakDuration = longBreakDuration;
    }
    
    public int getWorkSessionsBeforeLongBreak() {
        return workSessionsBeforeLongBreak;
    }
    
    public void setWorkSessionsBeforeLongBreak(int workSessionsBeforeLongBreak) {
        this.workSessionsBeforeLongBreak = workSessionsBeforeLongBreak;
    }
    
    public List<Integer> getWorkDays() {
        return workDays;
    }
    
    public void setWorkDays(List<Integer> workDays) {
        this.workDays = workDays;
    }
    
    public List<String> getCustomCategories() {
        return customCategories;
    }
    
    public void setCustomCategories(List<String> customCategories) {
        this.customCategories = customCategories;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
