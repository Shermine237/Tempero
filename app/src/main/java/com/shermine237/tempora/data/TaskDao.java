package com.shermine237.tempora.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shermine237.tempora.model.Task;

import java.util.Date;
import java.util.List;

/**
 * Interface DAO pour accéder aux tâches dans la base de données.
 */
@Dao
public interface TaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);
    
    @Update
    void update(Task task);
    
    @Delete
    void delete(Task task);
    
    @Query("DELETE FROM tasks")
    void deleteAll();
    
    @Query("SELECT * FROM tasks WHERE approved = 1 ORDER BY dueDate ASC")
    LiveData<List<Task>> getAllTasks();
    
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    LiveData<List<Task>> getAllTasksIncludingUnapproved();
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    LiveData<Task> getTaskById(int id);
    
    @Query("SELECT * FROM tasks WHERE completed = 0 AND approved = 1 ORDER BY dueDate ASC")
    LiveData<List<Task>> getIncompleteTasks();
    
    @Query("SELECT * FROM tasks WHERE completed = 1 AND approved = 1 ORDER BY completionDate DESC")
    LiveData<List<Task>> getCompletedTasks();
    
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate AND approved = 1 ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksForDateRange(Date startDate, Date endDate);
    
    @Query("SELECT * FROM tasks WHERE category = :category AND approved = 1 ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksByCategory(String category);
    
    @Query("SELECT * FROM tasks WHERE priority >= :minPriority AND approved = 1 ORDER BY priority DESC, dueDate ASC")
    LiveData<List<Task>> getTasksByMinPriority(int minPriority);
    
    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0 AND approved = 1")
    LiveData<Integer> getIncompleteTaskCount();
    
    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate < :currentDate AND completed = 0 AND approved = 1")
    LiveData<Integer> getOverdueTaskCount(Date currentDate);
    
    @Query("SELECT * FROM tasks WHERE dueDate = :date AND approved = 1 ORDER BY priority DESC")
    LiveData<List<Task>> getTasksForDate(Date date);
    
    @Query("SELECT * FROM tasks WHERE scheduledDate = :date AND approved = 1 ORDER BY priority DESC")
    LiveData<List<Task>> getTasksScheduledForDate(Date date);
}
