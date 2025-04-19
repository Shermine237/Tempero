package com.shermine237.tempora.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.repository.TaskRepository;

import java.util.Date;
import java.util.List;

/**
 * ViewModel pour gérer les données des tâches et les exposer à l'interface utilisateur.
 */
public class TaskViewModel extends AndroidViewModel {
    
    private final TaskRepository repository;
    
    // Données en cache
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> incompleteTasks;
    private final LiveData<List<Task>> completedTasks;
    private final LiveData<Integer> incompleteTaskCount;
    private final LiveData<Integer> overdueTaskCount;
    
    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        
        // Initialiser les données en cache
        allTasks = repository.getAllTasks();
        incompleteTasks = repository.getIncompleteTasks();
        completedTasks = repository.getCompletedTasks();
        incompleteTaskCount = repository.getIncompleteTaskCount();
        overdueTaskCount = repository.getOverdueTaskCount(new Date());
    }
    
    // Méthodes d'accès aux données
    
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    
    public LiveData<List<Task>> getIncompleteTasks() {
        return incompleteTasks;
    }
    
    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }
    
    public LiveData<Task> getTaskById(int id) {
        return repository.getTaskById(id);
    }
    
    public LiveData<List<Task>> getTasksForDateRange(Date startDate, Date endDate) {
        return repository.getTasksForDateRange(startDate, endDate);
    }
    
    public LiveData<List<Task>> getTasksByCategory(String category) {
        return repository.getTasksByCategory(category);
    }
    
    public LiveData<List<Task>> getTasksByMinPriority(int minPriority) {
        return repository.getTasksByMinPriority(minPriority);
    }
    
    public LiveData<Integer> getIncompleteTaskCount() {
        return incompleteTaskCount;
    }
    
    public LiveData<Integer> getOverdueTaskCount() {
        return overdueTaskCount;
    }
    
    // Méthodes de modification des données
    
    public void insert(Task task) {
        repository.insert(task);
    }
    
    public void update(Task task) {
        repository.update(task);
    }
    
    public void delete(Task task) {
        repository.delete(task);
    }
    
    public void deleteAll() {
        repository.deleteAll();
    }
    
    public void completeTask(Task task) {
        repository.completeTask(task);
    }
    
    /**
     * Crée une nouvelle tâche avec les paramètres spécifiés
     */
    public void createTask(String title, String description, Date dueDate, int priority, 
                          int difficulty, int estimatedDuration, String category) {
        Task newTask = new Task(title, description, dueDate, priority, difficulty, 
                               estimatedDuration, category);
        insert(newTask);
    }
}
