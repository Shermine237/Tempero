package com.shermine237.tempora.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.shermine237.tempora.data.TemporaDatabase;
import com.shermine237.tempora.data.UserProfileDao;
import com.shermine237.tempora.model.UserProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository pour gérer les opérations de données liées au profil utilisateur.
 * Cette classe fournit une API propre pour accéder aux données du profil utilisateur.
 */
public class UserProfileRepository {
    
    private final UserProfileDao userProfileDao;
    private final ExecutorService executorService;
    
    // Données en cache
    private final LiveData<UserProfile> userProfile;
    
    public UserProfileRepository(Application application) {
        TemporaDatabase db = TemporaDatabase.getDatabase(application);
        userProfileDao = db.userProfileDao();
        executorService = Executors.newFixedThreadPool(2);
        
        // Initialiser les données en cache
        userProfile = userProfileDao.getUserProfile();
    }
    
    // Méthodes d'accès aux données
    
    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }
    
    // Méthodes de modification des données
    
    public void insert(UserProfile userProfile) {
        executorService.execute(() -> {
            userProfileDao.insert(userProfile);
        });
    }
    
    public void update(UserProfile userProfile) {
        executorService.execute(() -> {
            userProfileDao.update(userProfile);
        });
    }
    
    public void deleteAll() {
        executorService.execute(() -> {
            userProfileDao.deleteAll();
        });
    }
    
    /**
     * Crée un profil utilisateur par défaut s'il n'en existe pas déjà un
     * @param name Nom de l'utilisateur
     * @param email Email de l'utilisateur
     */
    public void createDefaultProfileIfNotExists(String name, String email) {
        executorService.execute(() -> {
            if (userProfileDao.getUserProfile().getValue() == null) {
                UserProfile defaultProfile = new UserProfile(name, email);
                userProfileDao.insert(defaultProfile);
            }
        });
    }
}
