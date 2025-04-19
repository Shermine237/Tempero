package com.shermine237.tempora.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.repository.ScheduleRepository;

import java.util.Date;
import java.util.List;

/**
 * ViewModel pour gérer les données des plannings et les exposer à l'interface utilisateur.
 */
public class ScheduleViewModel extends AndroidViewModel {
    
    private final ScheduleRepository repository;
    
    // Données en cache
    private final LiveData<List<Schedule>> allSchedules;
    private final LiveData<List<Schedule>> approvedSchedules;
    private final LiveData<List<Schedule>> completedSchedules;
    private final LiveData<Float> averageProductivityScore;
    
    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        repository = new ScheduleRepository(application);
        
        // Initialiser les données en cache
        allSchedules = repository.getAllSchedules();
        approvedSchedules = repository.getApprovedSchedules();
        completedSchedules = repository.getCompletedSchedules();
        averageProductivityScore = repository.getAverageProductivityScore();
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
        return repository.getScheduleById(id);
    }
    
    public LiveData<Schedule> getScheduleForDate(Date date) {
        return repository.getScheduleForDate(date);
    }
    
    public LiveData<List<Schedule>> getSchedulesForDateRange(Date startDate, Date endDate) {
        return repository.getSchedulesForDateRange(startDate, endDate);
    }
    
    public LiveData<Float> getAverageProductivityScore() {
        return averageProductivityScore;
    }
    
    // Méthodes de modification des données
    
    public void insert(Schedule schedule) {
        repository.insert(schedule);
    }
    
    public void update(Schedule schedule) {
        repository.update(schedule);
    }
    
    public void delete(Schedule schedule) {
        repository.delete(schedule);
    }
    
    public void deleteAll() {
        repository.deleteAll();
    }
    
    public void approveSchedule(Schedule schedule) {
        repository.approveSchedule(schedule);
    }
    
    public void completeSchedule(Schedule schedule, int productivityScore) {
        repository.completeSchedule(schedule, productivityScore);
    }
    
    /**
     * Crée un nouveau planning avec les paramètres spécifiés
     */
    public void createSchedule(Date date, List<ScheduleItem> items) {
        Schedule newSchedule = new Schedule(date, items);
        insert(newSchedule);
    }
}
