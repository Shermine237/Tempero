package com.shermine237.tempora.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.shermine237.tempora.data.ScheduleDao;
import com.shermine237.tempora.data.TemporaDatabase;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.service.NotificationService;
import com.shermine237.tempora.service.AIService;
import com.shermine237.tempora.model.Task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Calendar;

/**
 * Repository pour gérer les opérations de données liées aux plannings.
 * Cette classe fournit une API propre pour accéder aux données des plannings.
 */
public class ScheduleRepository {
    
    private final ScheduleDao scheduleDao;
    private final ExecutorService executorService;
    private final NotificationService notificationService;
    private final Application application;
    
    // Données en cache
    private final LiveData<List<Schedule>> allSchedules;
    private final LiveData<List<Schedule>> approvedSchedules;
    private final LiveData<List<Schedule>> completedSchedules;
    
    public ScheduleRepository(Application application) {
        TemporaDatabase db = TemporaDatabase.getDatabase(application);
        scheduleDao = db.scheduleDao();
        executorService = Executors.newFixedThreadPool(4);
        notificationService = new NotificationService(application);
        this.application = application;
        
        // Initialiser les données en cache
        allSchedules = scheduleDao.getAllSchedules();
        approvedSchedules = scheduleDao.getApprovedSchedules();
        completedSchedules = scheduleDao.getCompletedSchedules();
    }
    
    // Méthodes d'accès aux données
    
    public LiveData<List<Schedule>> getAllSchedules() {
        return allSchedules;
    }
    
    public LiveData<List<Schedule>> getApprovedSchedules() {
        return approvedSchedules;
    }
    
    public LiveData<List<Schedule>> getCompletedSchedules() {
        return completedSchedules;
    }
    
    public LiveData<Schedule> getScheduleById(int id) {
        return scheduleDao.getScheduleById(id);
    }
    
    public LiveData<Schedule> getScheduleForDate(Date date) {
        return scheduleDao.getScheduleForDate(date);
    }
    
    public LiveData<List<Schedule>> getSchedulesForDateRange(Date startDate, Date endDate) {
        return scheduleDao.getSchedulesForDateRange(startDate, endDate);
    }
    
    public LiveData<Float> getAverageProductivityScore() {
        return scheduleDao.getAverageProductivityScore();
    }
    
    // Méthodes de modification des données
    
    public void insert(Schedule schedule) {
        executorService.execute(() -> {
            scheduleDao.insert(schedule);
        });
    }
    
    public void update(Schedule schedule) {
        executorService.execute(() -> {
            scheduleDao.update(schedule);
        });
    }
    
    public void delete(Schedule schedule) {
        executorService.execute(() -> {
            scheduleDao.delete(schedule);
        });
    }
    
    public void deleteAll() {
        executorService.execute(() -> {
            scheduleDao.deleteAll();
        });
    }
    
    /**
     * Approuve un planning généré par l'IA
     * @param schedule Planning à approuver
     */
    public void approveSchedule(Schedule schedule) {
        executorService.execute(() -> {
            // Marquer le planning comme approuvé
            schedule.setApproved(true);
            scheduleDao.update(schedule);
            
            // Programmer des notifications pour chaque tâche du planning
            notificationService.scheduleNotificationsForApprovedSchedule(schedule);
        });
    }
    
    /**
     * Marque un planning comme complété
     * @param schedule Planning à marquer comme complété
     * @param productivityScore Score de productivité (0-100)
     */
    public void completeSchedule(Schedule schedule, int productivityScore) {
        executorService.execute(() -> {
            schedule.setCompleted(true);
            schedule.setProductivityScore(productivityScore);
            scheduleDao.update(schedule);
            
            // Collecter les données pour l'apprentissage automatique
            collectUserActivityData(schedule, productivityScore);
        });
    }
    
    /**
     * Collecte les données d'activité utilisateur pour l'apprentissage automatique
     * @param schedule Planning complété
     * @param productivityScore Score de productivité global
     */
    private void collectUserActivityData(Schedule schedule, int productivityScore) {
        try {
            // Convertir le score de productivité de 0-100 à 0-5
            float normalizedScore = productivityScore / 20.0f;
            
            // Récupérer le service AI
            AIService aiService = new AIService(application);
            
            // Collecter les données pour chaque élément du planning
            for (ScheduleItem item : schedule.getItems()) {
                if (item.isCompleted() && item.getType().equals("task")) {
                    // Créer une activité utilisateur pour le backend d'IA
                    String description = ""; // ScheduleItem n'a pas de description
                    
                    // Récupérer la catégorie de la tâche
                    String category = "Autre";
                    if (item.getTaskId() > 0) {
                        Task task = aiService.getTaskById(item.getTaskId());
                        if (task != null) {
                            category = task.getCategory();
                            description = task.getDescription();
                        }
                    }
                    
                    // Ajouter l'activité au backend d'IA
                    aiService.addUserActivity(
                        item.getTitle(),
                        description,
                        category,
                        item.getStartTime(),
                        item.getEndTime(),
                        normalizedScore,
                        true
                    );
                }
            }
        } catch (Exception e) {
            Log.e("ScheduleRepository", "Error collecting user activity data", e);
        }
    }
}
