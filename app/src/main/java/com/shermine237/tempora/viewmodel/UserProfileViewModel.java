package com.shermine237.tempora.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.repository.UserProfileRepository;

import java.util.List;

/**
 * ViewModel pour gérer les données du profil utilisateur et les exposer à l'interface utilisateur.
 */
public class UserProfileViewModel extends AndroidViewModel {
    
    private final UserProfileRepository repository;
    
    // Données en cache
    private final LiveData<UserProfile> userProfile;
    
    public UserProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserProfileRepository(application);
        
        // Initialiser les données en cache
        userProfile = repository.getUserProfile();
    }
    
    // Méthodes d'accès aux données
    
    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }
    
    // Méthodes de modification des données
    
    public void insert(UserProfile userProfile) {
        repository.insert(userProfile);
    }
    
    public void update(UserProfile userProfile) {
        repository.update(userProfile);
    }
    
    public void deleteAll() {
        repository.deleteAll();
    }
    
    /**
     * Crée un profil utilisateur par défaut s'il n'en existe pas déjà un
     */
    public void createDefaultProfileIfNotExists(String name, String email) {
        repository.createDefaultProfileIfNotExists(name, email);
    }
    
    /**
     * Met à jour les préférences de travail de l'utilisateur
     */
    public void updateWorkPreferences(UserProfile profile, int startHour, int endHour, List<Integer> workDays) {
        profile.setPreferredWorkStartHour(startHour);
        profile.setPreferredWorkEndHour(endHour);
        profile.setWorkDays(workDays);
        repository.update(profile);
    }
    
    /**
     * Met à jour les préférences de pause de l'utilisateur
     */
    public void updateBreakPreferences(UserProfile profile, int shortBreakDuration, 
                                     int longBreakDuration, int sessionsBeforeLongBreak) {
        profile.setShortBreakDuration(shortBreakDuration);
        profile.setLongBreakDuration(longBreakDuration);
        profile.setWorkSessionsBeforeLongBreak(sessionsBeforeLongBreak);
        repository.update(profile);
    }
    
    /**
     * Met à jour les catégories personnalisées de l'utilisateur
     */
    public void updateCustomCategories(UserProfile profile, List<String> categories) {
        profile.setCustomCategories(categories);
        repository.update(profile);
    }
}
