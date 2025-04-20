package com.shermine237.tempora.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.utils.DateConverter;
import com.shermine237.tempora.utils.ScheduleItemListConverter;
import com.shermine237.tempora.utils.StringListConverter;
import com.shermine237.tempora.utils.WorkHoursListConverter;

/**
 * Base de données principale de l'application Tempero.
 * Cette classe gère la création et la mise à jour de la base de données SQLite.
 */
@Database(entities = {Task.class, UserProfile.class, Schedule.class}, version = 4, exportSchema = false)
@TypeConverters({DateConverter.class, StringListConverter.class, ScheduleItemListConverter.class, WorkHoursListConverter.class})
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
                    // Utiliser des migrations au lieu de détruire la base de données
                    .addMigrations(MIGRATION_3_4)
                    // Conserver fallbackToDestructiveMigration comme solution de secours
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Migration de la version 3 à 4 de la base de données
     * Cette migration préserve les données existantes
     */
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Aucune modification structurelle n'est nécessaire pour cette migration
            // car nous avons seulement ajouté le support pour les heures de travail personnalisées
            // qui est géré par le TypeConverter
        }
    };
}
