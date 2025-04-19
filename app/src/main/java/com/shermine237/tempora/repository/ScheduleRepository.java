package com.shermine237.tempora.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.shermine237.tempora.data.ScheduleDao;
import com.shermine237.tempora.data.TemporaDatabase;
import com.shermine237.tempora.model.Schedule;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository pour gérer les opérations de données liées aux plannings.
 * Cette classe fournit une API propre pour accéder aux données des plannings.
 */
public class ScheduleRepository {
    
    private final ScheduleDao scheduleDao;
    private final ExecutorService executorService;
    
    // Données en cache
    private final LiveData<List<Schedule>> allSchedules;
    private final LiveData<List<Schedule>> approvedSchedules;
    private final LiveData<List<Schedule>> completedSchedules;
    
    public ScheduleRepository(Application application) {
        TemporaDatabase db = TemporaDatabase.getDatabase(application);
        scheduleDao = db.scheduleDao();
        executorService = Executors.newFixedThreadPool(4);
        
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
            schedule.setApproved(true);
            scheduleDao.update(schedule);
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
        });
    }
}
