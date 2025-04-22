package com.shermine237.tempora.service;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shermine237.tempora.ai.backend.AIBackendService;
import com.shermine237.tempora.ai.backend.UserPreferences;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.model.WorkHours;
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
    
    // Application
    private final Application application;
    
    // Repositories
    private final TaskRepository taskRepository;
    private final UserProfileRepository userProfileRepository;
    private final ScheduleRepository scheduleRepository;
    
    // Services
    private final NotificationService notificationService;
    
    // Backend d'IA
    private final AIBackendService aiBackendService;
    
    // Exécuteur pour les tâches en arrière-plan
    private final ExecutorService executor;
    private final Handler handler;
    
    // Statut de l'analyse
    private final MutableLiveData<Boolean> isAnalyzing;
    
    // Statut de la génération de planning
    private final MutableLiveData<Boolean> isGenerating;
    
    /**
     * Constructeur
     * @param application Application
     */
    public AIService(Application application) {
        this.application = application;
        
        // Initialiser les repositories
        taskRepository = new TaskRepository(application);
        userProfileRepository = new UserProfileRepository(application);
        scheduleRepository = new ScheduleRepository(application);
        
        // Initialiser le service de notification
        notificationService = new NotificationService(application);
        
        // Initialiser le backend d'IA
        aiBackendService = new AIBackendService(application);
        
        // Initialiser les exécuteurs
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        
        // Initialiser les statuts
        isAnalyzing = new MutableLiveData<>(false);
        isGenerating = new MutableLiveData<>(false);
    }
    
    /**
     * Analyse les habitudes de l'utilisateur en fonction des tâches complétées
     */
    public void analyzeUserHabits() {
        isAnalyzing.setValue(true);
        
        executor.execute(() -> {
            try {
                // Récupérer les tâches complétées
                List<Task> completedTasks = taskRepository.getCompletedTasks().getValue();
                
                if (completedTasks != null && !completedTasks.isEmpty()) {
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
     * Génère un planning optimisé pour une journée donnée
     */
    public void generateScheduleForDate(Date date) {
        // Indiquer que la génération commence
        isGenerating.postValue(true);
        
        // Log pour le débogage
        Log.d("AIService", "Génération du planning pour la date: " + date);
        
        executor.execute(() -> {
            try {
                Log.i(TAG, "Starting schedule generation for date: " + date);
                
                // Récupérer le profil utilisateur
                UserProfile userProfile = userProfileRepository.getUserProfile().getValue();
                
                if (userProfile == null) {
                    Log.i(TAG, "Creating default user profile");
                    // Créer un profil utilisateur par défaut
                    userProfile = createDefaultUserProfile();
                    // Enregistrer le profil utilisateur
                    userProfileRepository.insert(userProfile);
                }
                
                // Configurer les préférences utilisateur pour le backend d'IA
                configureUserPreferences();
                
                // Récupérer les tâches incomplètes
                List<Task> incompleteTasks = taskRepository.getIncompleteTasks().getValue();
                
                if (incompleteTasks == null || incompleteTasks.isEmpty()) {
                    Log.i(TAG, "No incomplete tasks to schedule, asking AI to generate tasks");
                    // Utiliser l'IA pour générer des tâches avec des données de démonstration
                    incompleteTasks = aiBackendService.generateTasksWithDemoData(date);
                    
                    // Enregistrer les tâches générées dans la base de données
                    for (Task task : incompleteTasks) {
                        taskRepository.insert(task);
                    }
                }
                
                Log.i(TAG, "Found " + incompleteTasks.size() + " tasks to schedule");
                
                // Convertir les tâches Android en tâches backend
                List<com.shermine237.tempora.ai.backend.Task> backendTasks = new ArrayList<>();
                for (Task task : incompleteTasks) {
                    backendTasks.add(aiBackendService.convertAndroidTaskToBackendTask(task));
                }
                
                // Générer le planning avec le backend d'IA
                com.shermine237.tempora.ai.backend.Schedule backendSchedule = 
                    aiBackendService.generateSchedule(date, backendTasks);
                
                // Log pour le débogage
                Log.d("AIService", "Planning backend généré avec " + backendSchedule.getItems().size() + " éléments");
                for (com.shermine237.tempora.ai.backend.ScheduleItem item : backendSchedule.getItems()) {
                    Log.d("AIService", "Élément backend: " + item.getTitle() + ", Type: " + item.getType() + ", TaskId: " + item.getTaskId());
                }
                
                // Convertir le planning backend en planning Android
                Schedule schedule = aiBackendService.convertBackendScheduleToAndroidSchedule(backendSchedule);
                
                Log.i(TAG, "Schedule generated with " + schedule.getItems().size() + " items");
                
                // Normaliser la date pour ignorer l'heure
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                schedule.setDate(calendar.getTime());
                
                // Vérifier si un planning existe déjà pour cette date
                Schedule existingSchedule = null;
                try {
                    existingSchedule = scheduleRepository.getScheduleForDate(calendar.getTime()).getValue();
                } catch (Exception e) {
                    Log.e(TAG, "Error getting existing schedule", e);
                }
                
                if (existingSchedule != null) {
                    Log.i(TAG, "Updating existing schedule for date: " + date);
                    // Mettre à jour le planning existant
                    existingSchedule.setItems(schedule.getItems());
                    scheduleRepository.update(existingSchedule);
                } else {
                    Log.i(TAG, "Inserting new schedule for date: " + date);
                    // Enregistrer le nouveau planning
                    scheduleRepository.insert(schedule);
                }
                
                // Envoyer une notification
                handler.post(() -> {
                    notificationService.notifyDailySchedule(schedule);
                });
                
                // Indiquer que la génération est terminée
                isGenerating.postValue(false);
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating schedule", e);
                // En cas d'erreur, indiquer que la génération est terminée
                isGenerating.postValue(false);
            }
        });
    }
    
    /**
     * Configure les préférences utilisateur pour le backend d'IA
     */
    private void configureUserPreferences() {
        // Récupérer le profil utilisateur
        UserProfile userProfile = userProfileRepository.getUserProfile().getValue();
        if (userProfile == null) {
            return;
        }
        
        // Créer les préférences utilisateur pour le backend d'IA
        com.shermine237.tempora.ai.backend.UserPreferences preferences = new com.shermine237.tempora.ai.backend.UserPreferences();
        
        // Configurer les heures de travail
        if (userProfile.getWorkHours() != null) {
            for (int i = 0; i < Math.min(7, userProfile.getWorkHours().size()); i++) {
                WorkHours workHours = userProfile.getWorkHours().get(i);
                int startHour = workHours.getStartHour();
                int endHour = workHours.getEndHour();
                preferences.setWorkStartHour(i, startHour);
                preferences.setWorkEndHour(i, endHour);
            }
        }
        
        // Configurer les préférences de repas
        preferences.setIncludeBreakfast(userProfile.isIncludeBreakfast());
        preferences.setIncludeLunch(userProfile.isIncludeLunch());
        preferences.setIncludeDinner(userProfile.isIncludeDinner());
        
        // Configurer les préférences de pauses
        preferences.setIncludeBreaks(userProfile.isIncludeBreaks());
        
        // Initialiser le backend d'IA avec les préférences
        aiBackendService.initialize(preferences);
    }
    
    /**
     * Crée un profil utilisateur par défaut
     * @return Profil utilisateur par défaut
     */
    private UserProfile createDefaultUserProfile() {
        UserProfile profile = new UserProfile("Utilisateur", "utilisateur@exemple.com");
        
        // Heures de travail par défaut (9h à 17h)
        profile.setPreferredWorkStartHour(9);
        profile.setPreferredWorkEndHour(17);
        
        // Jours de travail par défaut (lundi à vendredi)
        List<Integer> workDays = new ArrayList<>();
        workDays.add(Calendar.MONDAY);
        workDays.add(Calendar.TUESDAY);
        workDays.add(Calendar.WEDNESDAY);
        workDays.add(Calendar.THURSDAY);
        workDays.add(Calendar.FRIDAY);
        profile.setWorkDays(workDays);
        
        // Durées de pause par défaut
        profile.setShortBreakDuration(15); // 15 minutes
        profile.setLongBreakDuration(60);  // 1 heure
        
        return profile;
    }
    
    /**
     * Génère des conseils de productivité basés sur l'analyse des habitudes
     */
    private void generateProductivityTips() {
        try {
            // Utiliser le backend d'IA pour générer des conseils de productivité
            String productivityTip = generateProductivityTip();
            
            if (productivityTip != null && !productivityTip.isEmpty()) {
                // Envoyer une notification avec le conseil
                notificationService.sendProductivityTipNotification(
                    "Conseil de productivité", 
                    productivityTip
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la génération des conseils de productivité", e);
        }
    }
    
    /**
     * Génère un conseil de productivité personnalisé
     * @return Conseil de productivité
     */
    public String generateProductivityTip() {
        try {
            return aiBackendService.generateProductivityTip();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la génération d'un conseil de productivité", e);
            return "Essayez de planifier vos tâches importantes le matin pour une meilleure productivité.";
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
        executor.execute(() -> {
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
        executor.execute(() -> {
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
     * Retourne l'état d'analyse des habitudes utilisateur
     * @return LiveData indiquant si l'analyse est en cours
     */
    public LiveData<Boolean> getIsAnalyzing() {
        return isAnalyzing;
    }
    
    /**
     * Retourne l'état de génération de planning
     * @return LiveData indiquant si la génération est en cours
     */
    public LiveData<Boolean> getIsGenerating() {
        return isGenerating;
    }
    
    /**
     * Analyse les habitudes de travail de l'utilisateur
     * @return Recommandations basées sur les habitudes
     */
    public List<String> analyzeWorkHabits() {
        // Récupérer les données d'analyse des habitudes
        List<String> insights = new ArrayList<>();
        
        // Ajouter quelques recommandations génériques en attendant l'implémentation complète
        insights.add("Vous êtes plus productif le matin entre 9h et 11h");
        insights.add("Vos tâches de catégorie 'Travail' prennent généralement plus de temps que prévu");
        insights.add("Vous complétez plus de tâches le lundi et le mercredi");
        insights.add("Essayez de planifier les tâches difficiles pendant vos heures de haute productivité");
        
        return insights;
    }
    
    /**
     * Suggère les meilleurs moments pour travailler sur une tâche spécifique
     * @param task Tâche à planifier
     * @return Liste des créneaux horaires recommandés
     */
    public List<String> suggestBestTimeSlots(Task task) {
        // Créer une liste de créneaux horaires recommandés
        List<String> timeSlots = new ArrayList<>();
        
        // Ajouter quelques créneaux génériques en attendant l'implémentation complète
        timeSlots.add("9:00 - 10:30");
        timeSlots.add("14:00 - 15:30");
        timeSlots.add("16:00 - 17:30");
        
        return timeSlots;
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
     * Ajoute une activité utilisateur pour l'apprentissage automatique
     * @param title Titre de l'activité
     * @param description Description de l'activité
     * @param category Catégorie de l'activité
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @param productivityScore Score de productivité (0-5)
     * @param completed Si l'activité a été complétée
     */
    public void addUserActivity(String title, String description, String category, 
                               Date startTime, Date endTime, float productivityScore, 
                               boolean completed) {
        executor.execute(() -> {
            try {
                // Créer une activité utilisateur
                com.shermine237.tempora.ai.backend.UserActivity activity = 
                    new com.shermine237.tempora.ai.backend.UserActivity(
                        title, description, category, startTime, endTime, 
                        productivityScore, completed);
                
                // Ajouter l'activité au backend d'IA
                aiBackendService.addUserActivity(activity);
                
                Log.i(TAG, "Added user activity: " + title);
            } catch (Exception e) {
                Log.e(TAG, "Error adding user activity", e);
            }
        });
    }
    
    /**
     * Récupère une tâche par son ID
     * @param taskId ID de la tâche
     * @return Tâche ou null si non trouvée
     */
    public Task getTaskById(int taskId) {
        try {
            return taskRepository.getTaskById(taskId).getValue();
        } catch (Exception e) {
            Log.e(TAG, "Error getting task by ID: " + taskId, e);
            return null;
        }
    }
    
    /**
     * Génère une recommandation personnalisée pour une tâche
     * @param taskTitle Titre de la tâche
     * @param category Catégorie de la tâche
     * @return Recommandation personnalisée
     */
    public String generateTaskRecommendation(String taskTitle, String category) {
        try {
            return aiBackendService.generateTaskRecommendation(taskTitle, category);
        } catch (Exception e) {
            Log.e(TAG, "Error generating task recommendation", e);
            return "Essayez de planifier cette tâche le matin pour une meilleure productivité.";
        }
    }
}
