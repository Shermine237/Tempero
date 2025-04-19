package com.shermine237.tempora.service;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shermine237.tempora.ai.IntelligentScheduler;
import com.shermine237.tempora.ai.UserHabitAnalyzer;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.repository.ScheduleRepository;
import com.shermine237.tempora.repository.TaskRepository;
import com.shermine237.tempora.repository.UserProfileRepository;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service d'intelligence artificielle pour l'application Tempero.
 * Cette classe coordonne l'analyse des habitudes, la génération de plannings
 * et les recommandations intelligentes.
 */
public class AIService {
    
    private static final String TAG = "AIService";
    
    // Repositories
    private final TaskRepository taskRepository;
    private final UserProfileRepository userProfileRepository;
    private final ScheduleRepository scheduleRepository;
    
    // Composants IA
    private final UserHabitAnalyzer habitAnalyzer;
    private final IntelligentScheduler scheduler;
    
    // Service de notification
    private final NotificationService notificationService;
    
    // Exécuteur pour les opérations en arrière-plan
    private final ExecutorService executorService;
    
    // Statut de l'analyse
    private final MutableLiveData<Boolean> isAnalyzing;
    
    // Constructeur
    public AIService(Application application) {
        // Initialiser les repositories
        taskRepository = new TaskRepository(application);
        userProfileRepository = new UserProfileRepository(application);
        scheduleRepository = new ScheduleRepository(application);
        
        // Initialiser les composants IA
        habitAnalyzer = new UserHabitAnalyzer(application);
        scheduler = new IntelligentScheduler(application);
        
        // Initialiser le service de notification
        notificationService = new NotificationService(application);
        
        // Initialiser l'exécuteur
        executorService = Executors.newFixedThreadPool(2);
        
        // Initialiser le statut
        isAnalyzing = new MutableLiveData<>(false);
    }
    
    /**
     * Analyse les habitudes de l'utilisateur en fonction des tâches complétées
     */
    public void analyzeUserHabits() {
        isAnalyzing.setValue(true);
        
        executorService.execute(() -> {
            try {
                // Récupérer les tâches complétées
                List<Task> completedTasks = taskRepository.getCompletedTasks().getValue();
                
                if (completedTasks != null && !completedTasks.isEmpty()) {
                    // Analyser les habitudes
                    habitAnalyzer.analyzeTasks(completedTasks);
                    
                    // Générer des conseils de productivité
                    generateProductivityTips();
                }
                
                isAnalyzing.postValue(false);
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing user habits", e);
                isAnalyzing.postValue(false);
            }
        });
    }
    
    /**
     * Génère un planning pour une journée donnée
     * @param date Date pour laquelle générer le planning
     */
    public void generateScheduleForDate(Date date) {
        executorService.execute(() -> {
            try {
                // Récupérer le profil utilisateur
                UserProfile userProfile = userProfileRepository.getUserProfile().getValue();
                
                if (userProfile == null) {
                    Log.e(TAG, "Cannot generate schedule: user profile is null");
                    return;
                }
                
                // Configurer le planificateur
                scheduler.setUserProfile(userProfile);
                
                // Récupérer les tâches incomplètes
                List<Task> incompleteTasks = taskRepository.getIncompleteTasks().getValue();
                
                if (incompleteTasks == null || incompleteTasks.isEmpty()) {
                    Log.i(TAG, "No incomplete tasks to schedule");
                    return;
                }
                
                // Générer le planning
                Schedule schedule = scheduler.generateSchedule(date, incompleteTasks);
                
                // Enregistrer le planning
                scheduleRepository.insert(schedule);
                
                // Envoyer une notification
                notificationService.notifyDailySchedule(schedule);
                
                // Vérifier la surcharge potentielle
                checkForOverload(schedule);
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating schedule", e);
            }
        });
    }
    
    /**
     * Génère des conseils de productivité basés sur l'analyse des habitudes
     */
    private void generateProductivityTips() {
        try {
            // Obtenir les heures les plus productives
            int[] productiveHours = habitAnalyzer.getMostProductiveHours();
            
            if (productiveHours.length > 0) {
                int bestHour = productiveHours[0];
                String tip = String.format(
                        "Vous êtes plus productif(ve) vers %dh. Essayez de planifier vos tâches importantes à ce moment.",
                        bestHour);
                notificationService.notifyProductivityTip(tip);
            }
            
            // Obtenir les jours les plus productifs
            int[] productiveDays = habitAnalyzer.getMostProductiveDays();
            
            if (productiveDays.length > 0) {
                String[] dayNames = {"dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"};
                int bestDay = productiveDays[0];
                String tip = String.format(
                        "Vous êtes plus productif(ve) le %s. Planifiez vos tâches importantes ce jour-là.",
                        dayNames[bestDay]);
                notificationService.notifyProductivityTip(tip);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating productivity tips", e);
        }
    }
    
    /**
     * Vérifie si un planning contient une surcharge potentielle
     */
    private void checkForOverload(Schedule schedule) {
        if (schedule == null || schedule.getItems().isEmpty()) {
            return;
        }
        
        // Compter le nombre total d'heures de travail
        int totalWorkMinutes = 0;
        for (int i = 0; i < schedule.getItems().size(); i++) {
            if (schedule.getItems().get(i).getType().equals("task")) {
                totalWorkMinutes += schedule.getItems().get(i).getDurationMinutes();
            }
        }
        
        // Si le planning contient plus de 8 heures de travail, envoyer une alerte
        if (totalWorkMinutes > 480) { // 8 heures = 480 minutes
            String message = String.format(
                    "Attention : votre planning contient %d heures et %d minutes de travail. Pensez à répartir certaines tâches sur d'autres jours.",
                    totalWorkMinutes / 60, totalWorkMinutes % 60);
            notificationService.notifyOverloadAlert(message);
        }
    }
    
    /**
     * Planifie des rappels pour les tâches à venir
     */
    public void scheduleTaskReminders() {
        executorService.execute(() -> {
            try {
                // Récupérer les tâches incomplètes
                List<Task> incompleteTasks = taskRepository.getIncompleteTasks().getValue();
                
                if (incompleteTasks == null || incompleteTasks.isEmpty()) {
                    return;
                }
                
                // Planifier des rappels pour chaque tâche
                for (Task task : incompleteTasks) {
                    if (task.getDueDate() != null) {
                        // Déterminer le délai de rappel en fonction de la priorité
                        int reminderMinutes = 30; // Par défaut: 30 minutes avant
                        
                        switch (task.getPriority()) {
                            case 5: // Très haute priorité
                                reminderMinutes = 60; // 1 heure avant
                                break;
                            case 4: // Haute priorité
                                reminderMinutes = 45; // 45 minutes avant
                                break;
                            case 3: // Priorité moyenne
                                reminderMinutes = 30; // 30 minutes avant
                                break;
                            case 2: // Faible priorité
                                reminderMinutes = 15; // 15 minutes avant
                                break;
                            case 1: // Très faible priorité
                                reminderMinutes = 10; // 10 minutes avant
                                break;
                        }
                        
                        // Planifier le rappel
                        notificationService.scheduleTaskReminder(task, reminderMinutes);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error scheduling task reminders", e);
            }
        });
    }
    
    /**
     * Vérifie les tâches en retard et envoie des notifications
     */
    public void checkOverdueTasks() {
        executorService.execute(() -> {
            try {
                // Récupérer les tâches en retard
                Date currentDate = new Date();
                List<Task> incompleteTasks = taskRepository.getIncompleteTasks().getValue();
                
                if (incompleteTasks == null || incompleteTasks.isEmpty()) {
                    return;
                }
                
                // Vérifier chaque tâche
                for (Task task : incompleteTasks) {
                    if (task.getDueDate() != null && task.getDueDate().before(currentDate)) {
                        // La tâche est en retard, envoyer une notification
                        notificationService.notifyOverdueTask(task);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking overdue tasks", e);
            }
        });
    }
    
    /**
     * Génère un planning pour aujourd'hui
     */
    public void generateTodaySchedule() {
        generateScheduleForDate(new Date());
    }
    
    /**
     * Génère un planning pour demain
     */
    public void generateTomorrowSchedule() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        generateScheduleForDate(calendar.getTime());
    }
    
    /**
     * Obtient le statut de l'analyse
     */
    public LiveData<Boolean> getIsAnalyzing() {
        return isAnalyzing;
    }
    
    /**
     * Analyse les habitudes de travail de l'utilisateur
     * @return Recommandations basées sur les habitudes
     */
    public List<String> analyzeWorkHabits() {
        return habitAnalyzer.generateInsights();
    }
    
    /**
     * Prédit la durée réelle d'une tâche en fonction des données historiques
     * @param task Tâche à analyser
     * @return Durée prédite en minutes
     */
    public int predictTaskDuration(Task task) {
        // Utiliser l'estimateur de durée basé sur les données historiques
        int predictedDuration = task.getEstimatedDuration();
        
        // Facteurs d'ajustement basés sur la difficulté et la priorité
        float difficultyFactor = 1.0f + (task.getDifficulty() * 0.1f);
        float priorityFactor = 1.0f - (task.getPriority() * 0.05f);
        
        // Ajuster la durée estimée
        predictedDuration = Math.round(predictedDuration * difficultyFactor * priorityFactor);
        
        // Ajouter une marge de sécurité de 10%
        predictedDuration = Math.round(predictedDuration * 1.1f);
        
        return predictedDuration;
    }
    
    /**
     * Suggère les meilleurs moments pour travailler sur une tâche spécifique
     * @param task Tâche à planifier
     * @return Liste des créneaux horaires recommandés
     */
    public List<String> suggestBestTimeSlots(Task task) {
        return habitAnalyzer.getBestTimeSlotsForTask(task);
    }
}
