package com.shermine237.tempora.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.shermine237.tempora.utils.DateConverter;
import com.shermine237.tempora.utils.StringListConverter;
import com.shermine237.tempora.utils.WorkHoursListConverter;

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
    
    // Préférences de travail (conservées pour la compatibilité avec les versions antérieures)
    private int preferredWorkStartHour; // Heure préférée pour commencer à travailler (0-23)
    private int preferredWorkEndHour; // Heure préférée pour terminer de travailler (0-23)
    
    // Heures de travail par jour
    @TypeConverters(WorkHoursListConverter.class)
    private List<WorkHours> workHoursByDay;
    
    // Préférences de repas
    private boolean includeBreakfast;
    private boolean includeLunch;
    private boolean includeDinner;
    
    // Préférences de pauses
    private int shortBreakDuration; // Durée des pauses courtes en minutes
    private int longBreakDuration; // Durée des pauses longues en minutes
    private int workSessionsBeforeLongBreak; // Nombre de sessions de travail avant une pause longue
    private boolean includeBreaks;
    
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
        this.includeBreakfast = true;
        this.includeLunch = true;
        this.includeDinner = true;
        this.includeBreaks = true;
        
        // Par défaut: du lundi au vendredi
        this.workDays = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            this.workDays.add(i);
        }
        
        // Initialiser les heures de travail par jour
        this.workHoursByDay = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            boolean isWorkDay = i >= 1 && i <= 5; // Du lundi au vendredi
            WorkHours workHours = new WorkHours(i, 9, 17, isWorkDay);
            this.workHoursByDay.add(workHours);
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
    
    public List<WorkHours> getWorkHoursByDay() {
        return workHoursByDay;
    }
    
    public void setWorkHoursByDay(List<WorkHours> workHoursByDay) {
        this.workHoursByDay = workHoursByDay;
    }
    
    public List<WorkHours> getWorkHours() {
        return workHoursByDay;
    }
    
    public void setWorkHours(List<WorkHours> workHoursByDay) {
        this.workHoursByDay = workHoursByDay;
    }
    
    public boolean isIncludeBreakfast() {
        return includeBreakfast;
    }
    
    public void setIncludeBreakfast(boolean includeBreakfast) {
        this.includeBreakfast = includeBreakfast;
    }
    
    public boolean isIncludeLunch() {
        return includeLunch;
    }
    
    public void setIncludeLunch(boolean includeLunch) {
        this.includeLunch = includeLunch;
    }
    
    public boolean isIncludeDinner() {
        return includeDinner;
    }
    
    public void setIncludeDinner(boolean includeDinner) {
        this.includeDinner = includeDinner;
    }
    
    public boolean isIncludeBreaks() {
        return includeBreaks;
    }
    
    public void setIncludeBreaks(boolean includeBreaks) {
        this.includeBreaks = includeBreaks;
    }
    
    /**
     * Obtient les heures de travail pour un jour spécifique
     * @param dayOfWeek Jour de la semaine (0-6)
     * @return Heures de travail pour ce jour
     */
    public WorkHours getWorkHoursForDay(int dayOfWeek) {
        if (workHoursByDay == null) {
            workHoursByDay = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                boolean isWorkDay = workDays.contains(i);
                WorkHours workHours = new WorkHours(i, preferredWorkStartHour, preferredWorkEndHour, isWorkDay);
                workHoursByDay.add(workHours);
            }
        }
        
        for (WorkHours workHours : workHoursByDay) {
            if (workHours.getDayOfWeek() == dayOfWeek) {
                return workHours;
            }
        }
        
        // Si aucune heure de travail n'est trouvée pour ce jour, en créer une nouvelle
        WorkHours workHours = new WorkHours(dayOfWeek, preferredWorkStartHour, preferredWorkEndHour, workDays.contains(dayOfWeek));
        workHoursByDay.add(workHours);
        return workHours;
    }
    
    /**
     * Met à jour les heures de travail pour un jour spécifique
     * @param workHours Heures de travail à mettre à jour
     */
    public void updateWorkHoursForDay(WorkHours workHours) {
        if (workHoursByDay == null) {
            workHoursByDay = new ArrayList<>();
        }
        
        // Rechercher si les heures de travail pour ce jour existent déjà
        for (int i = 0; i < workHoursByDay.size(); i++) {
            if (workHoursByDay.get(i).getDayOfWeek() == workHours.getDayOfWeek()) {
                workHoursByDay.set(i, workHours);
                
                // Mettre à jour la liste des jours de travail
                if (workHours.isWorkDay()) {
                    if (!workDays.contains(workHours.getDayOfWeek())) {
                        workDays.add(workHours.getDayOfWeek());
                    }
                } else {
                    workDays.remove(Integer.valueOf(workHours.getDayOfWeek()));
                }
                
                return;
            }
        }
        
        // Si aucune heure de travail n'est trouvée pour ce jour, en ajouter une nouvelle
        workHoursByDay.add(workHours);
        
        // Mettre à jour la liste des jours de travail
        if (workHours.isWorkDay()) {
            if (!workDays.contains(workHours.getDayOfWeek())) {
                workDays.add(workHours.getDayOfWeek());
            }
        } else {
            workDays.remove(Integer.valueOf(workHours.getDayOfWeek()));
        }
    }
}
