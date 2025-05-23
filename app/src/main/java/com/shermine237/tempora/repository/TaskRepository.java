package com.shermine237.tempora.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.shermine237.tempora.data.TaskDao;
import com.shermine237.tempora.data.TemporaDatabase;
import com.shermine237.tempora.model.Task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository pour gérer les opérations de données liées aux tâches.
 * Cette classe fournit une API propre pour accéder aux données des tâches.
 */
public class TaskRepository {
    
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    
    // Données en cache
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> incompleteTasks;
    private final LiveData<List<Task>> completedTasks;
    
    public TaskRepository(Application application) {
        TemporaDatabase db = TemporaDatabase.getDatabase(application);
        taskDao = db.taskDao();
        executorService = Executors.newFixedThreadPool(4);
        
        // Initialiser les données en cache
        allTasks = taskDao.getAllTasks();
        incompleteTasks = taskDao.getIncompleteTasks();
        completedTasks = taskDao.getCompletedTasks();
    }
    
    // Méthodes d'accès aux données
    
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    
    public LiveData<List<Task>> getAllTasksIncludingUnapproved() {
        return taskDao.getAllTasksIncludingUnapproved();
    }
    
    public LiveData<List<Task>> getIncompleteTasks() {
        return incompleteTasks;
    }
    
    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }
    
    public LiveData<Task> getTaskById(int id) {
        return taskDao.getTaskById(id);
    }
    
    public LiveData<List<Task>> getTasksForDateRange(Date startDate, Date endDate) {
        return taskDao.getTasksForDateRange(startDate, endDate);
    }
    
    public LiveData<List<Task>> getTasksByCategory(String category) {
        return taskDao.getTasksByCategory(category);
    }
    
    public LiveData<List<Task>> getTasksByMinPriority(int minPriority) {
        return taskDao.getTasksByMinPriority(minPriority);
    }
    
    public LiveData<List<Task>> getTasksForDate(Date date) {
        return taskDao.getTasksForDate(date);
    }
    
    /**
     * Récupère les tâches planifiées pour une date spécifique
     * @param date Date pour laquelle récupérer les tâches planifiées
     * @return LiveData contenant la liste des tâches planifiées pour cette date
     */
    public LiveData<List<Task>> getTasksScheduledForDate(Date date) {
        return taskDao.getTasksScheduledForDate(date);
    }
    
    public LiveData<Integer> getIncompleteTaskCount() {
        return taskDao.getIncompleteTaskCount();
    }
    
    public LiveData<Integer> getOverdueTaskCount(Date currentDate) {
        return taskDao.getOverdueTaskCount(currentDate);
    }
    
    // Méthodes de modification des données
    
    public void insert(Task task) {
        executorService.execute(() -> {
            taskDao.insert(task);
        });
    }
    
    public void update(Task task) {
        // Ajouter des logs pour le débogage
        Log.d("TaskRepository", "Mise à jour de la tâche: " + task.getTitle() + 
              ", ID: " + task.getId() + 
              ", Générée par IA: " + task.isAiGenerated() + 
              ", Approuvée: " + task.isApproved() + 
              ", Date planifiée: " + (task.getScheduledDate() != null ? task.getScheduledDate() : "null") + 
              ", Date d'échéance: " + (task.getDueDate() != null ? task.getDueDate() : "null"));
        
        executorService.execute(() -> {
            taskDao.update(task);
        });
    }
    
    public void delete(Task task) {
        executorService.execute(() -> {
            taskDao.delete(task);
        });
    }
    
    public void deleteAll() {
        executorService.execute(() -> {
            taskDao.deleteAll();
        });
    }
    
    /**
     * Marque une tâche comme complétée
     * @param task Tâche à marquer comme complétée
     */
    public void completeTask(Task task) {
        executorService.execute(() -> {
            task.setCompleted(true);
            task.setCompletionDate(new Date());
            taskDao.update(task);
        });
    }
}
