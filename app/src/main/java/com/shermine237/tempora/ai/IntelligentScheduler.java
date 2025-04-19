package com.shermine237.tempora.ai;

import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.model.UserProfile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Planificateur intelligent pour l'application Tempero.
 * Cette classe utilise les habitudes de l'utilisateur et des algorithmes d'optimisation
 * pour générer des plannings optimisés.
 */
public class IntelligentScheduler {
    
    // Analyseur d'habitudes
    private final UserHabitAnalyzer habitAnalyzer;
    
    // Profil utilisateur
    private UserProfile userProfile;
    
    // Constructeur
    public IntelligentScheduler(UserHabitAnalyzer habitAnalyzer) {
        this.habitAnalyzer = habitAnalyzer;
    }
    
    /**
     * Définit le profil utilisateur pour la planification
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
    
    /**
     * Génère un planning optimisé pour une journée donnée
     * @param date Date pour laquelle générer le planning
     * @param tasks Liste des tâches à planifier
     * @return Planning optimisé
     */
    public Schedule generateSchedule(Date date, List<Task> tasks) {
        if (userProfile == null) {
            throw new IllegalStateException("User profile must be set before generating a schedule");
        }
        
        // Filtrer les tâches pertinentes pour cette journée
        List<Task> relevantTasks = filterRelevantTasks(tasks, date);
        
        // Trier les tâches par priorité et date d'échéance
        sortTasksByPriorityAndDueDate(relevantTasks);
        
        // Créer les plages horaires disponibles pour la journée
        List<TimeSlot> availableSlots = createAvailableTimeSlots(date);
        
        // Allouer les tâches aux plages horaires
        List<ScheduleItem> scheduleItems = allocateTasksToTimeSlots(relevantTasks, availableSlots);
        
        // Ajouter des pauses intelligentes
        scheduleItems = addIntelligentBreaks(scheduleItems);
        
        // Créer et retourner le planning
        return new Schedule(date, scheduleItems);
    }
    
    /**
     * Filtre les tâches pertinentes pour une journée donnée
     */
    private List<Task> filterRelevantTasks(List<Task> tasks, Date date) {
        List<Task> relevantTasks = new ArrayList<>();
        
        // Obtenir le jour de la semaine (0 = Dimanche, 1 = Lundi, etc.)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        
        // Vérifier si c'est un jour de travail pour l'utilisateur
        if (!userProfile.getWorkDays().contains(dayOfWeek)) {
            // Si ce n'est pas un jour de travail, ne retourner que les tâches urgentes
            for (Task task : tasks) {
                if (!task.isCompleted() && task.getPriority() >= 4) {
                    relevantTasks.add(task);
                }
            }
            return relevantTasks;
        }
        
        // Pour un jour de travail, filtrer les tâches pertinentes
        for (Task task : tasks) {
            if (task.isCompleted()) {
                continue; // Ignorer les tâches déjà complétées
            }
            
            // Ajouter les tâches dont la date d'échéance est aujourd'hui ou avant
            if (task.getDueDate() != null && !task.getDueDate().after(date)) {
                relevantTasks.add(task);
                continue;
            }
            
            // Ajouter les tâches de haute priorité même si leur échéance est plus tard
            if (task.getPriority() >= 4) {
                relevantTasks.add(task);
                continue;
            }
            
            // Ajouter quelques tâches de priorité moyenne si l'échéance est proche (dans les 3 jours)
            if (task.getPriority() >= 3 && task.getDueDate() != null) {
                Calendar dueDateCal = Calendar.getInstance();
                dueDateCal.setTime(task.getDueDate());
                
                Calendar datePlusThreeDays = Calendar.getInstance();
                datePlusThreeDays.setTime(date);
                datePlusThreeDays.add(Calendar.DAY_OF_MONTH, 3);
                
                if (!dueDateCal.after(datePlusThreeDays)) {
                    relevantTasks.add(task);
                }
            }
        }
        
        return relevantTasks;
    }
    
    /**
     * Trie les tâches par priorité et date d'échéance
     */
    private void sortTasksByPriorityAndDueDate(List<Task> tasks) {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                // D'abord comparer par priorité (plus haute priorité en premier)
                int priorityComparison = Integer.compare(t2.getPriority(), t1.getPriority());
                if (priorityComparison != 0) {
                    return priorityComparison;
                }
                
                // Ensuite par date d'échéance (plus tôt en premier)
                if (t1.getDueDate() == null && t2.getDueDate() == null) {
                    return 0;
                } else if (t1.getDueDate() == null) {
                    return 1;
                } else if (t2.getDueDate() == null) {
                    return -1;
                }
                return t1.getDueDate().compareTo(t2.getDueDate());
            }
        });
    }
    
    /**
     * Crée les plages horaires disponibles pour une journée
     */
    private List<TimeSlot> createAvailableTimeSlots(Date date) {
        List<TimeSlot> slots = new ArrayList<>();
        
        // Obtenir les heures de travail préférées de l'utilisateur
        int startHour = userProfile.getPreferredWorkStartHour();
        int endHour = userProfile.getPreferredWorkEndHour();
        
        // Créer une plage horaire pour la journée de travail
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(date);
        startCal.set(Calendar.HOUR_OF_DAY, startHour);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(date);
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        
        // Ajouter la plage horaire principale
        slots.add(new TimeSlot(startCal.getTime(), endCal.getTime()));
        
        return slots;
    }
    
    /**
     * Alloue les tâches aux plages horaires disponibles
     */
    private List<ScheduleItem> allocateTasksToTimeSlots(List<Task> tasks, List<TimeSlot> availableSlots) {
        List<ScheduleItem> scheduleItems = new ArrayList<>();
        
        // Si aucune plage horaire n'est disponible, retourner une liste vide
        if (availableSlots.isEmpty()) {
            return scheduleItems;
        }
        
        // Utiliser la première plage horaire disponible
        TimeSlot workSlot = availableSlots.get(0);
        Date currentTime = workSlot.getStartTime();
        
        // Allouer chaque tâche
        for (Task task : tasks) {
            // Vérifier si la plage horaire est épuisée
            if (currentTime.after(workSlot.getEndTime())) {
                break;
            }
            
            // Estimer la durée de la tâche
            int estimatedDuration = task.getEstimatedDuration();
            if (estimatedDuration <= 0) {
                // Si aucune durée n'est spécifiée, estimer en fonction des données historiques
                estimatedDuration = habitAnalyzer.estimateTaskDuration(task.getCategory(), 30);
                task.setEstimatedDuration(estimatedDuration);
            }
            
            // Calculer l'heure de fin
            Calendar endTimeCal = Calendar.getInstance();
            endTimeCal.setTime(currentTime);
            endTimeCal.add(Calendar.MINUTE, estimatedDuration);
            Date endTime = endTimeCal.getTime();
            
            // Vérifier si la tâche dépasse la fin de la plage horaire
            if (endTime.after(workSlot.getEndTime())) {
                endTime = workSlot.getEndTime();
            }
            
            // Créer l'élément de planning
            ScheduleItem item = new ScheduleItem(task.getId(), task.getTitle(), currentTime, endTime);
            scheduleItems.add(item);
            
            // Mettre à jour l'heure courante
            currentTime = endTime;
        }
        
        return scheduleItems;
    }
    
    /**
     * Ajoute des pauses intelligentes au planning
     */
    private List<ScheduleItem> addIntelligentBreaks(List<ScheduleItem> scheduleItems) {
        if (scheduleItems.isEmpty()) {
            return scheduleItems;
        }
        
        List<ScheduleItem> itemsWithBreaks = new ArrayList<>();
        int itemCount = 0;
        
        // Obtenir les préférences de pause de l'utilisateur
        int shortBreakDuration = userProfile.getShortBreakDuration();
        int longBreakDuration = userProfile.getLongBreakDuration();
        int sessionsBeforeLongBreak = userProfile.getWorkSessionsBeforeLongBreak();
        
        for (int i = 0; i < scheduleItems.size(); i++) {
            ScheduleItem currentItem = scheduleItems.get(i);
            itemsWithBreaks.add(currentItem);
            itemCount++;
            
            // Si ce n'est pas le dernier élément, ajouter une pause
            if (i < scheduleItems.size() - 1) {
                // Déterminer le type de pause (courte ou longue)
                boolean needsLongBreak = (itemCount % sessionsBeforeLongBreak == 0);
                int breakDuration = needsLongBreak ? longBreakDuration : shortBreakDuration;
                
                // Calculer les heures de début et de fin de la pause
                Date breakStart = currentItem.getEndTime();
                
                Calendar breakEndCal = Calendar.getInstance();
                breakEndCal.setTime(breakStart);
                breakEndCal.add(Calendar.MINUTE, breakDuration);
                Date breakEnd = breakEndCal.getTime();
                
                // Créer l'élément de pause
                String breakTitle = needsLongBreak ? "Pause longue" : "Pause courte";
                ScheduleItem breakItem = new ScheduleItem(breakTitle, breakStart, breakEnd, "break");
                itemsWithBreaks.add(breakItem);
                
                // Ajuster l'heure de début de l'élément suivant
                ScheduleItem nextItem = scheduleItems.get(i + 1);
                nextItem.setStartTime(breakEnd);
            }
        }
        
        return itemsWithBreaks;
    }
    
    /**
     * Classe interne représentant une plage horaire disponible
     */
    private static class TimeSlot {
        private final Date startTime;
        private final Date endTime;
        
        public TimeSlot(Date startTime, Date endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public Date getStartTime() {
            return startTime;
        }
        
        public Date getEndTime() {
            return endTime;
        }
        
        public int getDurationMinutes() {
            return (int) ((endTime.getTime() - startTime.getTime()) / (60 * 1000));
        }
    }
}
