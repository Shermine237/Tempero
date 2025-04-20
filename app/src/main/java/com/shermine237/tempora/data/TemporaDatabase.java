package com.shermine237.tempora.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.utils.DateConverter;
import com.shermine237.tempora.utils.ScheduleItemListConverter;
import com.shermine237.tempora.utils.StringListConverter;

/**
 * Base de données principale de l'application Tempero.
 * Cette classe gère la création et la mise à jour de la base de données SQLite.
 */
@Database(entities = {Task.class, UserProfile.class, Schedule.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class, StringListConverter.class, ScheduleItemListConverter.class})
public abstract class TemporaDatabase extends RoomDatabase {
    
    // DAOs
    public abstract TaskDao taskDao();
    public abstract UserProfileDao userProfileDao();
    public abstract ScheduleDao scheduleDao();
    
    // Instance unique de la base de données
    private static volatile TemporaDatabase INSTANCE;
    
    /**
     * Obtient l'instance unique de la base de données.
     * @param context Contexte de l'application
     * @return Instance de la base de données
     */
    public static TemporaDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TemporaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TemporaDatabase.class,
                            "tempora_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
