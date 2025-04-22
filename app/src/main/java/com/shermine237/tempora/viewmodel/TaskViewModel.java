package com.shermine237.tempora.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.repository.TaskRepository;
import com.shermine237.tempora.service.AIService;

import java.util.Date;
import java.util.List;

/**
 * ViewModel pour gérer les données des tâches et les exposer à l'interface utilisateur.
 */
public class TaskViewModel extends AndroidViewModel {
    
    private final TaskRepository repository;
    private final AIService aiService;
    
    // Données en cache
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> incompleteTasks;
    private final LiveData<List<Task>> completedTasks;
    private final LiveData<Integer> incompleteTaskCount;
    private final LiveData<Integer> overdueTaskCount;
    
    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        aiService = new AIService(application); // Ajout de l'initialisation de AIService
        
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
    
    public LiveData<List<Task>> getAllTasksIncludingUnapproved() {
        return repository.getAllTasksIncludingUnapproved();
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
    
    /**
     * Marque une tâche comme complétée
     * @param task Tâche à compléter
     */
    public void completeTask(Task task) {
        // Enregistrer la complétion pour l'apprentissage de l'IA
        aiService.recordTaskCompletion(task);
        
        // Mettre à jour la tâche dans la base de données
        repository.completeTask(task);
    }
    
    /**
     * Reporte une tâche à une date ultérieure
     * @param task Tâche à reporter
     * @param newDate Nouvelle date planifiée
     */
    public void postponeTask(Task task, Date newDate) {
        // Enregistrer le report pour l'apprentissage de l'IA
        aiService.recordTaskPostponement(task);
        
        // Mettre à jour la date planifiée
        task.setScheduledDate(newDate);
        update(task);
    }
    
    /**
     * Crée une nouvelle tâche
     * @param title Titre de la tâche
     * @param description Description de la tâche
     * @param dueDate Date d'échéance
     * @param priority Priorité (1-5)
     * @param difficulty Difficulté (1-5)
     * @param estimatedDuration Durée estimée en minutes
     * @param category Catégorie de la tâche
     */
    public void createTask(String title, String description, Date dueDate, int priority, 
                         int difficulty, int estimatedDuration, String category) {
        Task newTask = new Task(title, description, dueDate, priority, difficulty, 
                              estimatedDuration, category);
        newTask.setApproved(true); // Les tâches créées manuellement sont automatiquement approuvées
        newTask.setAiGenerated(false); // Les tâches créées manuellement ne sont pas générées par l'IA
        insert(newTask);
    }
    
    /**
     * Crée une nouvelle tâche avec une date planifiée
     */
    public void createTaskWithScheduledDate(String title, String description, Date dueDate, 
                                          Date scheduledDate, int priority, int difficulty, 
                                          int estimatedDuration, String category) {
        Task newTask = new Task(title, description, dueDate, priority, difficulty, 
                               estimatedDuration, category);
        newTask.setScheduledDate(scheduledDate);
        newTask.setApproved(true); // Les tâches créées manuellement sont approuvées par défaut
        insert(newTask);
    }
    
    /**
     * Récupère les tâches planifiées pour une date spécifique
     */
    public LiveData<List<Task>> getTasksScheduledForDate(Date date) {
        return repository.getTasksScheduledForDate(date);
    }
}
