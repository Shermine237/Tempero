package com.shermine237.tempora.ai.backend;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Service principal du backend d'IA
 * Cette classe coordonne les différents composants d'IA
 * et expose les fonctionnalités principales
 */
public class AIBackendService {
    
    private static final String TAG = "AIBackendService";
    
    private UserHabitAnalyzer habitAnalyzer;
    private IntelligentScheduler scheduler;
    private UserPreferences userPreferences;
    private TaskPerformanceAnalyzer performanceAnalyzer;
    private Application application;
    
    /**
     * Constructeur
     */
    public AIBackendService(Application application) {
        this.application = application;
        this.habitAnalyzer = new UserHabitAnalyzer();
        this.userPreferences = new UserPreferences();
        this.scheduler = new IntelligentScheduler(habitAnalyzer, userPreferences);
        this.performanceAnalyzer = new TaskPerformanceAnalyzer();
        
        Log.i(TAG, "Backend d'IA initialisé avec succès");
    }
    
    /**
     * Initialise le service avec les préférences utilisateur
     * @param userPreferences Préférences utilisateur
     */
    public void initialize(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
        this.scheduler = new IntelligentScheduler(habitAnalyzer, userPreferences);
    }
    
    /**
     * Ajoute une activité utilisateur pour analyse
     * @param activity Activité utilisateur
     */
    public void addUserActivity(UserActivity activity) {
        habitAnalyzer.addUserActivity(activity);
        
        // Ajouter également à l'analyseur de performance
        if (activity.isCompleted()) {
            long durationMinutes = (activity.getEndTime().getTime() - activity.getStartTime().getTime()) / (60 * 1000);
            performanceAnalyzer.addTaskPerformance(
                activity.getTitle(),
                activity.getCategory(),
                activity.getStartTime(),
                (int) durationMinutes,
                activity.getProductivityScore()
            );
        }
    }
    
    /**
     * Génère un planning optimisé pour une journée donnée
     * @param date Date pour laquelle générer le planning
     * @param tasks Liste des tâches à planifier
     * @return Planning optimisé
     */
    public Schedule generateSchedule(Date date, List<Task> tasks) {
        // Optimiser les tâches en fonction des performances analysées
        optimizeTasksBasedOnPerformance(tasks);
        
        return scheduler.generateSchedule(date, tasks);
    }
    
    /**
     * Optimise les tâches en fonction des performances analysées
     * @param tasks Liste des tâches à optimiser
     */
    private void optimizeTasksBasedOnPerformance(List<Task> tasks) {
        for (Task task : tasks) {
            // Ajuster la priorité des tâches en fonction de la période optimale
            int bestPeriod = performanceAnalyzer.getBestPeriodForTaskCategory(task.getCategory());
            
            // Obtenir l'heure actuelle
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            
            // Vérifier si nous sommes dans la période optimale pour cette tâche
            boolean isOptimalPeriod = false;
            
            switch (bestPeriod) {
                case TaskPerformanceAnalyzer.MORNING:
                    isOptimalPeriod = (currentHour >= 5 && currentHour < 12);
                    break;
                case TaskPerformanceAnalyzer.AFTERNOON:
                    isOptimalPeriod = (currentHour >= 12 && currentHour < 18);
                    break;
                case TaskPerformanceAnalyzer.EVENING:
                    isOptimalPeriod = (currentHour >= 18 && currentHour < 22);
                    break;
                case TaskPerformanceAnalyzer.NIGHT:
                    isOptimalPeriod = (currentHour >= 22 || currentHour < 5);
                    break;
            }
            
            // Augmenter la priorité si nous sommes dans la période optimale
            if (isOptimalPeriod && task.getPriority() < 5) {
                task.setPriority(task.getPriority() + 1);
            }
            
            // Mettre à jour la durée estimée si nous avons des données
            int avgDuration = performanceAnalyzer.getAverageDurationForTask(task.getTitle());
            if (avgDuration > 0) {
                task.setEstimatedDuration(avgDuration);
            }
        }
    }
    
    /**
     * Prédit la durée d'une tâche en fonction de son titre et de sa catégorie
     * @param taskTitle Titre de la tâche
     * @param category Catégorie de la tâche
     * @return Durée prédite en minutes
     */
    public int predictTaskDuration(String taskTitle, String category) {
        // Vérifier d'abord si l'analyseur de performance a des données
        int avgDuration = performanceAnalyzer.getAverageDurationForTask(taskTitle);
        if (avgDuration > 0) {
            return avgDuration;
        }
        
        // Sinon, utiliser l'analyseur d'habitudes
        return habitAnalyzer.predictTaskDuration(taskTitle, category);
    }
    
    /**
     * Génère un conseil de productivité basé sur l'analyse des habitudes
     * @return Conseil de productivité
     */
    public String generateProductivityTip() {
        return habitAnalyzer.generateProductivityTip();
    }
    
    /**
     * Génère une recommandation personnalisée pour une tâche
     * @param taskTitle Titre de la tâche
     * @param category Catégorie de la tâche
     * @return Recommandation personnalisée
     */
    public String generateTaskRecommendation(String taskTitle, String category) {
        return performanceAnalyzer.generateTaskRecommendation(taskTitle, category);
    }
    
    /**
     * Vérifie si un planning est surchargé et génère une alerte si nécessaire
     * @param schedule Planning à vérifier
     * @return Message d'alerte ou null si pas de surcharge
     */
    public String checkForOverload(Schedule schedule) {
        if (schedule == null || schedule.getItems() == null || schedule.getItems().isEmpty()) {
            return null;
        }
        
        if (schedule.isOverloaded()) {
            int totalWorkMinutes = 0;
            for (ScheduleItem item : schedule.getItems()) {
                if (item.getType().equals("task")) {
                    totalWorkMinutes += item.getDurationMinutes();
                }
            }
            
            return String.format(
                    "Attention : votre planning contient %d heures et %d minutes de travail. " +
                    "Pensez à répartir certaines tâches sur d'autres jours.",
                    totalWorkMinutes / 60, totalWorkMinutes % 60);
        }
        
        return null;
    }
    
    /**
     * Convertit une tâche Android en tâche backend
     * @param androidTask Tâche Android
     * @return Tâche backend
     */
    public Task convertAndroidTaskToBackendTask(com.shermine237.tempora.model.Task androidTask) {
        Task backendTask = new Task();
        backendTask.setId(androidTask.getId());
        backendTask.setTitle(androidTask.getTitle());
        backendTask.setDescription(androidTask.getDescription());
        backendTask.setCategory(androidTask.getCategory());
        backendTask.setPriority(androidTask.getPriority());
        backendTask.setEstimatedDuration(androidTask.getEstimatedDuration());
        backendTask.setCompleted(androidTask.isCompleted());
        
        // Définir directement la date d'échéance
        backendTask.setDueDate(androidTask.getDueDate());
        
        // Définir directement la date planifiée si elle existe
        if (androidTask.getScheduledDate() != null) {
            backendTask.setScheduledDate(androidTask.getScheduledDate());
        }
        
        return backendTask;
    }
    
    /**
     * Convertit un planning backend en planning Android
     * @param backendSchedule Planning backend
     * @return Planning Android
     */
    public com.shermine237.tempora.model.Schedule convertBackendScheduleToAndroidSchedule(Schedule backendSchedule) {
        // Créer un nouveau planning Android avec la date et une liste vide d'éléments
        com.shermine237.tempora.model.Schedule androidSchedule = new com.shermine237.tempora.model.Schedule(
            backendSchedule.getDate(), 
            new ArrayList<>()
        );
        
        // Convertir chaque élément du planning
        for (ScheduleItem backendItem : backendSchedule.getItems()) {
            // Déterminer le type d'élément
            String type = backendItem.getType();
            
            // Convertir les chaînes d'heure en objets Date
            Date startTime = parseTimeString(backendSchedule.getDate(), backendItem.getStartTime());
            Date endTime = parseTimeString(backendSchedule.getDate(), backendItem.getEndTime());
            
            // Créer l'élément Android en fonction du type
            com.shermine237.tempora.model.ScheduleItem androidItem;
            
            if ("task".equals(type)) {
                // Pour une tâche
                androidItem = new com.shermine237.tempora.model.ScheduleItem(
                    backendItem.getTaskId(),
                    backendItem.getTitle(),
                    startTime,
                    endTime
                );
                
                // Log pour le débogage
                Log.d("AIBackendService", "Création d'un élément de planning de type tâche: " + 
                      backendItem.getTitle() + ", TaskId: " + backendItem.getTaskId());
                
                // Mettre à jour la tâche avec les dates appropriées
                if (backendItem.getTaskId() > 0) {
                    updateTaskDates(backendItem.getTaskId(), backendSchedule.getDate());
                } else {
                    Log.w("AIBackendService", "TaskId invalide pour la tâche: " + backendItem.getTitle());
                }
            } else {
                // Pour les autres types (pause, repas, etc.)
                androidItem = new com.shermine237.tempora.model.ScheduleItem(
                    backendItem.getTitle(),
                    startTime,
                    endTime,
                    backendItem.getType()
                );
            }
            
            // Définir si l'élément est complété
            androidItem.setCompleted(backendItem.isCompleted());
            
            // Ajouter l'élément au planning
            androidSchedule.getItems().add(androidItem);
        }
        
        return androidSchedule;
    }
    
    /**
     * Met à jour les dates d'une tâche générée par l'IA
     * @param taskId ID de la tâche
     * @param scheduleDate Date du planning
     */
    private void updateTaskDates(int taskId, Date scheduleDate) {
        // Log pour le débogage
        Log.d("AIBackendService", "Mise à jour des dates de la tâche " + taskId + " avec la date de planning: " + scheduleDate);
        
        // Créer un calendrier pour la date du planning (sans l'heure)
        Calendar scheduleCal = Calendar.getInstance();
        scheduleCal.setTime(scheduleDate);
        scheduleCal.set(Calendar.HOUR_OF_DAY, 0);
        scheduleCal.set(Calendar.MINUTE, 0);
        scheduleCal.set(Calendar.SECOND, 0);
        scheduleCal.set(Calendar.MILLISECOND, 0);
        
        // Créer un calendrier pour la date d'échéance (même jour à minuit)
        Calendar dueCal = Calendar.getInstance();
        dueCal.setTime(scheduleDate);
        dueCal.set(Calendar.HOUR_OF_DAY, 23);
        dueCal.set(Calendar.MINUTE, 59);
        dueCal.set(Calendar.SECOND, 59);
        dueCal.set(Calendar.MILLISECOND, 999);
        
        // Log pour le débogage
        Log.d("AIBackendService", "Date planifiée: " + scheduleCal.getTime() + ", Date d'échéance: " + dueCal.getTime());
        
        // Mettre à jour la tâche dans un thread séparé
        new Thread(() -> {
            try {
                // Récupérer le repository de tâches
                com.shermine237.tempora.repository.TaskRepository taskRepository = 
                    new com.shermine237.tempora.repository.TaskRepository(application);
                
                // Utiliser le handler pour exécuter le code sur le thread principal
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.post(() -> {
                    // Observer la tâche
                    taskRepository.getTaskById(taskId).observeForever(new androidx.lifecycle.Observer<com.shermine237.tempora.model.Task>() {
                        @Override
                        public void onChanged(com.shermine237.tempora.model.Task task) {
                            if (task != null) {
                                // Mettre à jour la tâche
                                task.setScheduledDate(scheduleCal.getTime());
                                task.setDueDate(dueCal.getTime());
                                task.setApproved(false); // Par défaut, les tâches générées par l'IA ne sont pas approuvées
                                task.setAiGenerated(true); // Marquer la tâche comme générée par l'IA
                                
                                // Enregistrer la tâche mise à jour
                                taskRepository.update(task);
                                
                                Log.i(TAG, "Task " + taskId + " updated with scheduled date " + 
                                      scheduleCal.getTime() + " and due date " + dueCal.getTime());
                                
                                // Supprimer l'observer pour éviter les fuites de mémoire
                                taskRepository.getTaskById(taskId).removeObserver(this);
                            }
                        }
                    });
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating task dates", e);
            }
        }).start();
    }
    
    /**
     * Formate une date en chaîne de caractères (yyyy-MM-dd)
     * @param date Date à formater
     * @return Chaîne de caractères formatée
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Les mois commencent à 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    /**
     * Formate une heure en chaîne de caractères (HH:mm)
     * @param date Date à formater
     * @return Chaîne de caractères formatée
     */
    private String formatTime(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        
        return String.format("%02d:%02d", hours, minutes);
    }
    
    /**
     * Convertit une chaîne d'heure (HH:MM) en objet Date
     * @param baseDate Date de base
     * @param timeString Chaîne d'heure (HH:MM)
     * @return Objet Date
     */
    private Date parseTimeString(Date baseDate, String timeString) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(baseDate);
            
            String[] parts = timeString.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            
            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            return calendar.getTime();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time string: " + timeString, e);
            return baseDate;
        }
    }
}
