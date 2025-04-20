package com.shermine237.tempora.ai.backend;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Planificateur intelligent basé sur l'IA
 * Cette classe est responsable de la génération de plannings optimisés
 * en tenant compte des habitudes de l'utilisateur, des priorités et des contraintes
 */
public class IntelligentScheduler {

    private UserHabitAnalyzer habitAnalyzer;
    private UserPreferences userPreferences;
    
    // Constantes pour les types d'éléments de planning
    private static final String TYPE_TASK = "task";
    private static final String TYPE_BREAK = "break";
    private static final String TYPE_MEAL = "meal";
    
    /**
     * Constructeur
     * @param habitAnalyzer Analyseur d'habitudes utilisateur
     * @param userPreferences Préférences utilisateur
     */
    public IntelligentScheduler(UserHabitAnalyzer habitAnalyzer, UserPreferences userPreferences) {
        this.habitAnalyzer = habitAnalyzer;
        this.userPreferences = userPreferences;
    }
    
    /**
     * Génère un planning optimisé pour une journée donnée
     * @param date Date pour laquelle générer le planning
     * @param tasks Liste des tâches à planifier
     * @return Planning optimisé
     */
    public Schedule generateSchedule(Date date, List<Task> tasks) {
        // Créer un nouveau planning
        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setItems(new ArrayList<>());
        
        // Vérifier s'il y a des tâches à planifier
        if (tasks == null || tasks.isEmpty()) {
            return schedule;
        }
        
        // Trier les tâches par priorité (décroissante)
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Integer.compare(t2.getPriority(), t1.getPriority());
            }
        });
        
        // Obtenir les heures de travail de l'utilisateur
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0=Dimanche, 1=Lundi, etc.
        
        int startHour = userPreferences.getWorkStartHour(dayOfWeek);
        int endHour = userPreferences.getWorkEndHour(dayOfWeek);
        
        // Ajuster en fonction du jour le plus productif
        int mostProductiveDay = habitAnalyzer.getMostProductiveDay();
        int mostProductiveHour = habitAnalyzer.getMostProductiveHour();
        
        // Si c'est le jour le plus productif, placer les tâches importantes pendant l'heure la plus productive
        boolean isProductiveDay = (dayOfWeek == mostProductiveDay);
        
        // Initialiser l'heure actuelle au début de la journée de travail
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date currentTime = calendar.getTime();
        
        // Planifier le petit-déjeuner si nécessaire
        if (startHour <= 9 && userPreferences.includeBreakfast()) {
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            Date breakfastTime = calendar.getTime();
            
            if (breakfastTime.after(currentTime)) {
                // Ajouter le petit-déjeuner
                ScheduleItem breakfast = new ScheduleItem();
                breakfast.setTitle("Petit-déjeuner");
                breakfast.setType(TYPE_MEAL);
                breakfast.setStartTime(formatTime(breakfastTime));
                
                calendar.add(Calendar.MINUTE, 30); // 30 minutes pour le petit-déjeuner
                breakfast.setEndTime(formatTime(calendar.getTime()));
                breakfast.setDurationMinutes(30);
                
                schedule.getItems().add(breakfast);
                currentTime = calendar.getTime();
            }
        }
        
        // Planifier le déjeuner
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 30);
        Date lunchTime = calendar.getTime();
        
        // Planifier les tâches avant le déjeuner
        currentTime = planTasks(schedule, tasks, currentTime, lunchTime, isProductiveDay, mostProductiveHour);
        
        // Ajouter le déjeuner
        if (userPreferences.includeLunch()) {
            ScheduleItem lunch = new ScheduleItem();
            lunch.setTitle("Déjeuner");
            lunch.setType(TYPE_MEAL);
            lunch.setStartTime(formatTime(lunchTime));
            
            calendar.add(Calendar.MINUTE, 60); // 1 heure pour le déjeuner
            lunch.setEndTime(formatTime(calendar.getTime()));
            lunch.setDurationMinutes(60);
            
            schedule.getItems().add(lunch);
            currentTime = calendar.getTime();
        }
        
        // Planifier le dîner
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);
        Date dinnerTime = calendar.getTime();
        
        // Planifier les tâches entre le déjeuner et le dîner
        currentTime = planTasks(schedule, tasks, currentTime, dinnerTime, isProductiveDay, mostProductiveHour);
        
        // Ajouter le dîner
        if (userPreferences.includeDinner() && endHour >= 19) {
            ScheduleItem dinner = new ScheduleItem();
            dinner.setTitle("Dîner");
            dinner.setType(TYPE_MEAL);
            dinner.setStartTime(formatTime(dinnerTime));
            
            calendar.add(Calendar.MINUTE, 60); // 1 heure pour le dîner
            dinner.setEndTime(formatTime(calendar.getTime()));
            dinner.setDurationMinutes(60);
            
            schedule.getItems().add(dinner);
            currentTime = calendar.getTime();
        }
        
        // Planifier les tâches restantes après le dîner
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        calendar.set(Calendar.MINUTE, 0);
        Date endOfDay = calendar.getTime();
        
        planTasks(schedule, tasks, currentTime, endOfDay, isProductiveDay, mostProductiveHour);
        
        // Trier les éléments du planning par heure de début
        Collections.sort(schedule.getItems(), new Comparator<ScheduleItem>() {
            @Override
            public int compare(ScheduleItem i1, ScheduleItem i2) {
                return i1.getStartTime().compareTo(i2.getStartTime());
            }
        });
        
        return schedule;
    }
    
    /**
     * Planifie les tâches dans un intervalle de temps donné
     * @param schedule Planning à remplir
     * @param tasks Liste des tâches à planifier
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @param isProductiveDay Si c'est le jour le plus productif
     * @param mostProductiveHour Heure la plus productive
     * @return Nouvelle heure courante après planification
     */
    private Date planTasks(Schedule schedule, List<Task> tasks, Date startTime, Date endTime, 
                          boolean isProductiveDay, int mostProductiveHour) {
        // Copier la liste des tâches pour ne pas modifier l'originale
        List<Task> remainingTasks = new ArrayList<>(tasks);
        
        // Heure courante
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        Date currentTime = calendar.getTime();
        
        // Planifier les tâches jusqu'à l'heure de fin ou jusqu'à ce qu'il n'y ait plus de tâches
        while (currentTime.before(endTime) && !remainingTasks.isEmpty()) {
            // Trouver la meilleure tâche à planifier maintenant
            Task bestTask = findBestTask(remainingTasks, currentTime, isProductiveDay, mostProductiveHour);
            
            if (bestTask != null) {
                // Prédire la durée de la tâche
                int durationMinutes = bestTask.getEstimatedDuration();
                if (durationMinutes <= 0) {
                    durationMinutes = habitAnalyzer.predictTaskDuration(bestTask.getTitle(), bestTask.getCategory());
                }
                
                // Créer un élément de planning pour cette tâche
                ScheduleItem item = new ScheduleItem();
                item.setTitle(bestTask.getTitle());
                item.setDescription(bestTask.getDescription());
                item.setType(TYPE_TASK);
                item.setTaskId(bestTask.getId());
                item.setStartTime(formatTime(currentTime));
                
                // Calculer l'heure de fin
                calendar.add(Calendar.MINUTE, durationMinutes);
                
                // Vérifier si la tâche dépasse l'heure de fin
                if (calendar.getTime().after(endTime)) {
                    // Ajuster la durée pour ne pas dépasser l'heure de fin
                    long availableMinutes = (endTime.getTime() - currentTime.getTime()) / (60 * 1000);
                    if (availableMinutes <= 0) {
                        break; // Plus de temps disponible
                    }
                    
                    durationMinutes = (int) availableMinutes;
                    calendar.setTime(currentTime);
                    calendar.add(Calendar.MINUTE, durationMinutes);
                }
                
                item.setEndTime(formatTime(calendar.getTime()));
                item.setDurationMinutes(durationMinutes);
                
                // Ajouter l'élément au planning
                schedule.getItems().add(item);
                
                // Mettre à jour l'heure courante
                currentTime = calendar.getTime();
                
                // Supprimer la tâche de la liste des tâches restantes
                remainingTasks.remove(bestTask);
                
                // Ajouter une pause si nécessaire
                if (userPreferences.includeBreaks() && !remainingTasks.isEmpty()) {
                    // Ajouter une pause de 15 minutes toutes les 2 heures
                    if (schedule.getItems().size() % 3 == 0) {
                        ScheduleItem breakItem = new ScheduleItem();
                        breakItem.setTitle("Pause");
                        breakItem.setType(TYPE_BREAK);
                        breakItem.setStartTime(formatTime(currentTime));
                        
                        calendar.add(Calendar.MINUTE, 15);
                        breakItem.setEndTime(formatTime(calendar.getTime()));
                        breakItem.setDurationMinutes(15);
                        
                        schedule.getItems().add(breakItem);
                        currentTime = calendar.getTime();
                    }
                }
            } else {
                // Aucune tâche ne peut être planifiée, sortir de la boucle
                break;
            }
        }
        
        return currentTime;
    }
    
    /**
     * Trouve la meilleure tâche à planifier à un moment donné
     * @param tasks Liste des tâches disponibles
     * @param currentTime Heure actuelle
     * @param isProductiveDay Si c'est le jour le plus productif
     * @param mostProductiveHour Heure la plus productive
     * @return Meilleure tâche à planifier
     */
    private Task findBestTask(List<Task> tasks, Date currentTime, boolean isProductiveDay, int mostProductiveHour) {
        if (tasks.isEmpty()) {
            return null;
        }
        
        // Obtenir l'heure actuelle
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Si c'est le jour le plus productif et l'heure la plus productive, choisir la tâche la plus importante
        if (isProductiveDay && currentHour == mostProductiveHour) {
            return tasks.get(0); // La liste est déjà triée par priorité
        }
        
        // Sinon, choisir la tâche en fonction de la priorité et de la difficulté
        Task bestTask = null;
        int bestScore = -1;
        
        for (Task task : tasks) {
            int score = task.getPriority() * 2 - task.getDifficulty();
            
            // Bonus pour les tâches avec date d'échéance proche
            if (task.getDueDate() != null) {
                long daysUntilDue = (task.getDueDate().getTime() - currentTime.getTime()) / (24 * 60 * 60 * 1000);
                if (daysUntilDue <= 1) {
                    score += 5; // Bonus important pour les tâches dues aujourd'hui ou demain
                } else if (daysUntilDue <= 3) {
                    score += 3; // Bonus moyen pour les tâches dues dans 2-3 jours
                } else if (daysUntilDue <= 7) {
                    score += 1; // Petit bonus pour les tâches dues dans la semaine
                }
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestTask = task;
            }
        }
        
        return bestTask;
    }
    
    /**
     * Formate une date en chaîne de caractères pour l'heure (HH:MM)
     * @param date Date à formater
     * @return Heure formatée
     */
    private String formatTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }
}
