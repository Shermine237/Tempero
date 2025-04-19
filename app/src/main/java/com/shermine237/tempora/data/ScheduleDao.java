package com.shermine237.tempora.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shermine237.tempora.model.Schedule;

import java.util.Date;
import java.util.List;

/**
 * Interface DAO pour accéder aux plannings dans la base de données.
 */
@Dao
public interface ScheduleDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Schedule schedule);
    
    @Update
    void update(Schedule schedule);
    
    @Delete
    void delete(Schedule schedule);
    
    @Query("DELETE FROM schedules")
    void deleteAll();
    
    @Query("SELECT * FROM schedules ORDER BY date DESC")
    LiveData<List<Schedule>> getAllSchedules();
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    LiveData<Schedule> getScheduleById(int id);
    
    @Query("SELECT * FROM schedules WHERE date = :date LIMIT 1")
    LiveData<Schedule> getScheduleForDate(Date date);
    
    @Query("SELECT * FROM schedules WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    LiveData<List<Schedule>> getSchedulesForDateRange(Date startDate, Date endDate);
    
    @Query("SELECT * FROM schedules WHERE approved = 1 ORDER BY date DESC")
    LiveData<List<Schedule>> getApprovedSchedules();
    
    @Query("SELECT * FROM schedules WHERE completed = 1 ORDER BY date DESC")
    LiveData<List<Schedule>> getCompletedSchedules();
    
    @Query("SELECT AVG(productivityScore) FROM schedules WHERE completed = 1")
    LiveData<Float> getAverageProductivityScore();
}
