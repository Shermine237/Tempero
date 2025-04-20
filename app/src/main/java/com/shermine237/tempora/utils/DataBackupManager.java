package com.shermine237.tempora.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.shermine237.tempora.model.UserProfile;

/**
 * Gestionnaire de sauvegarde des données critiques de l'application.
 * Cette classe permet de sauvegarder et restaurer les données importantes
 * en cas de problème avec la base de données principale.
 */
public class DataBackupManager {
    
    private static final String TAG = "DataBackupManager";
    private static final String PREFS_NAME = "data_backup_prefs";
    private static final String KEY_USER_PROFILE = "user_profile_backup";
    
    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;
    
    public DataBackupManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Sauvegarde le profil utilisateur dans les SharedPreferences
     * @param userProfile Profil utilisateur à sauvegarder
     */
    public void backupUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            Log.w(TAG, "Tentative de sauvegarde d'un profil utilisateur null");
            return;
        }
        
        try {
            String json = gson.toJson(userProfile);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_USER_PROFILE, json);
            editor.apply();
            Log.i(TAG, "Profil utilisateur sauvegardé avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde du profil utilisateur", e);
        }
    }
    
    /**
     * Restaure le profil utilisateur depuis les SharedPreferences
     * @return Profil utilisateur restauré ou null si aucune sauvegarde n'existe
     */
    public UserProfile restoreUserProfile() {
        String json = preferences.getString(KEY_USER_PROFILE, null);
        
        if (json == null) {
            Log.i(TAG, "Aucune sauvegarde de profil utilisateur trouvée");
            return null;
        }
        
        try {
            UserProfile userProfile = gson.fromJson(json, UserProfile.class);
            Log.i(TAG, "Profil utilisateur restauré avec succès");
            return userProfile;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la restauration du profil utilisateur", e);
            return null;
        }
    }
    
    /**
     * Vérifie si une sauvegarde du profil utilisateur existe
     * @return true si une sauvegarde existe, false sinon
     */
    public boolean hasUserProfileBackup() {
        return preferences.contains(KEY_USER_PROFILE);
    }
    
    /**
     * Supprime la sauvegarde du profil utilisateur
     */
    public void clearUserProfileBackup() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_PROFILE);
        editor.apply();
        Log.i(TAG, "Sauvegarde du profil utilisateur supprimée");
    }
}
