package com.shermine237.tempora.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.shermine237.tempora.data.TemporaDatabase;
import com.shermine237.tempora.data.UserProfileDao;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.utils.DataBackupManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository pour gérer les opérations de données liées au profil utilisateur.
 * Cette classe fournit une API propre pour accéder aux données du profil utilisateur.
 */
public class UserProfileRepository {
    
    private final UserProfileDao userProfileDao;
    private final ExecutorService executorService;
    private final DataBackupManager backupManager;
    
    // Données en cache
    private final LiveData<UserProfile> userProfile;
    
    public UserProfileRepository(Application application) {
        TemporaDatabase db = TemporaDatabase.getDatabase(application);
        userProfileDao = db.userProfileDao();
        executorService = Executors.newFixedThreadPool(2);
        backupManager = new DataBackupManager(application);
        
        // Initialiser les données en cache
        userProfile = userProfileDao.getUserProfile();
        
        // Observer le profil utilisateur pour le sauvegarder automatiquement
        userProfile.observeForever(profile -> {
            if (profile != null) {
                backupManager.backupUserProfile(profile);
            }
        });
    }
    
    // Méthodes d'accès aux données
    
    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }
    
    /**
     * Récupère un profil utilisateur par son adresse email
     * @param email Adresse email de l'utilisateur
     * @return LiveData contenant le profil utilisateur correspondant à l'email
     */
    public LiveData<UserProfile> getUserProfileByEmail(String email) {
        return userProfileDao.getUserProfileByEmail(email);
    }
    
    // Méthodes de modification des données
    
    /**
     * Insère un nouveau profil utilisateur dans la base de données
     * @param userProfile Profil utilisateur à insérer
     */
    public void insert(UserProfile userProfile) {
        executorService.execute(() -> {
            userProfileDao.insert(userProfile);
            backupManager.backupUserProfile(userProfile);
        });
    }
    
    /**
     * Met à jour un profil utilisateur existant dans la base de données
     * @param userProfile Profil utilisateur à mettre à jour
     */
    public void update(UserProfile userProfile) {
        executorService.execute(() -> {
            userProfileDao.update(userProfile);
            backupManager.backupUserProfile(userProfile);
        });
    }
    
    /**
     * Supprime tous les profils utilisateurs de la base de données
     */
    public void deleteAll() {
        executorService.execute(() -> {
            userProfileDao.deleteAll();
            backupManager.clearUserProfileBackup();
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
    
    /**
     * Restaure le profil utilisateur depuis la sauvegarde si la base de données est vide
     * @return true si une restauration a été effectuée, false sinon
     */
    public boolean restoreFromBackupIfNeeded() {
        if (userProfile.getValue() == null && backupManager.hasUserProfileBackup()) {
            UserProfile restoredProfile = backupManager.restoreUserProfile();
            if (restoredProfile != null) {
                insert(restoredProfile);
                return true;
            }
        }
        return false;
    }
}
