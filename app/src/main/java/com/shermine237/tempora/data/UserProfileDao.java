package com.shermine237.tempora.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shermine237.tempora.model.UserProfile;

/**
 * Interface DAO pour accéder au profil utilisateur dans la base de données.
 */
@Dao
public interface UserProfileDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserProfile userProfile);
    
    @Update
    void update(UserProfile userProfile);
    
    @Query("SELECT * FROM user_profile WHERE id = 1")
    LiveData<UserProfile> getUserProfile();
    
    @Query("SELECT * FROM user_profile WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    LiveData<UserProfile> getUserProfileByEmail(String email);
    
    @Query("DELETE FROM user_profile")
    void deleteAll();
}
