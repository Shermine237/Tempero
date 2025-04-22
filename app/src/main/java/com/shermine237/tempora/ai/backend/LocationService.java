package com.shermine237.tempora.ai.backend;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Service de localisation pour l'IA
 * Ce service permet de prendre en compte la position de l'utilisateur pour optimiser la planification
 */
public class LocationService {
    
    private static final String TAG = "LocationService";
    private static final String PREFS_NAME = "location_prefs";
    private static final String KEY_HOME_LOCATION = "home_location";
    private static final String KEY_WORK_LOCATION = "work_location";
    private static final String KEY_CURRENT_LOCATION = "current_location";
    
    private Context context;
    private boolean hasLocationPermission;
    
    // Cache des temps de trajet
    private Map<String, Integer> travelTimeCache;
    
    /**
     * Constructeur
     */
    public LocationService(Application application) {
        this.context = application.getApplicationContext();
        this.hasLocationPermission = false; // Par défaut, on considère que l'on n'a pas la permission
        this.travelTimeCache = new HashMap<>();
        Log.i(TAG, "Service de localisation initialisé");
    }
    
    /**
     * Définit si l'application a la permission d'accéder à la localisation
     * @param hasPermission true si l'application a la permission, false sinon
     */
    public void setLocationPermission(boolean hasPermission) {
        this.hasLocationPermission = hasPermission;
        Log.d(TAG, "Permission d'accès à la localisation: " + hasPermission);
    }
    
    /**
     * Obtient la localisation actuelle de l'utilisateur
     * @return Localisation actuelle (adresse ou coordonnées)
     */
    public String getCurrentLocation() {
        if (!hasLocationPermission) {
            // Si nous n'avons pas la permission, on utilise la dernière localisation connue
            return getLastKnownLocation();
        }
        
        // Dans une implémentation réelle, nous utiliserions les services de localisation d'Android
        // Pour cette démo, nous retournons une valeur par défaut
        return "Paris, France";
    }
    
    /**
     * Obtient la dernière localisation connue de l'utilisateur
     * @return Dernière localisation connue
     */
    private String getLastKnownLocation() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_CURRENT_LOCATION, "Paris, France");
    }
    
    /**
     * Définit la localisation actuelle de l'utilisateur
     * @param location Localisation actuelle
     */
    public void setCurrentLocation(String location) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_CURRENT_LOCATION, location);
        editor.apply();
        
        Log.d(TAG, "Localisation actuelle définie: " + location);
    }
    
    /**
     * Obtient la localisation du domicile de l'utilisateur
     * @return Localisation du domicile
     */
    public String getHomeLocation() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_HOME_LOCATION, "");
    }
    
    /**
     * Définit la localisation du domicile de l'utilisateur
     * @param location Localisation du domicile
     */
    public void setHomeLocation(String location) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_HOME_LOCATION, location);
        editor.apply();
        
        Log.d(TAG, "Localisation du domicile définie: " + location);
    }
    
    /**
     * Obtient la localisation du lieu de travail de l'utilisateur
     * @return Localisation du lieu de travail
     */
    public String getWorkLocation() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_WORK_LOCATION, "");
    }
    
    /**
     * Définit la localisation du lieu de travail de l'utilisateur
     * @param location Localisation du lieu de travail
     */
    public void setWorkLocation(String location) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_WORK_LOCATION, location);
        editor.apply();
        
        Log.d(TAG, "Localisation du lieu de travail définie: " + location);
    }
    
    /**
     * Estime le temps de trajet entre deux localisations
     * @param origin Localisation d'origine
     * @param destination Localisation de destination
     * @return Temps de trajet estimé en minutes
     */
    public int estimateTravelTime(String origin, String destination) {
        // Clé de cache pour ce trajet
        String cacheKey = origin + "_to_" + destination;
        
        // Vérifier si nous avons déjà le temps de trajet en cache
        if (travelTimeCache.containsKey(cacheKey)) {
            return travelTimeCache.get(cacheKey);
        }
        
        // Dans une implémentation réelle, nous utiliserions une API comme Google Maps
        // Pour cette démo, nous simulons un temps de trajet
        int travelTime = simulateTravelTime(origin, destination);
        
        // Mettre en cache le temps de trajet
        travelTimeCache.put(cacheKey, travelTime);
        
        Log.d(TAG, "Temps de trajet estimé de " + origin + " à " + destination + ": " + travelTime + " minutes");
        
        return travelTime;
    }
    
    /**
     * Simule un temps de trajet entre deux localisations
     * @param origin Localisation d'origine
     * @param destination Localisation de destination
     * @return Temps de trajet simulé en minutes
     */
    private int simulateTravelTime(String origin, String destination) {
        // Si l'origine et la destination sont identiques
        if (origin.equals(destination)) {
            return 0;
        }
        
        // Si l'une des localisations est le domicile et l'autre le lieu de travail
        if ((origin.equals(getHomeLocation()) && destination.equals(getWorkLocation())) || 
            (origin.equals(getWorkLocation()) && destination.equals(getHomeLocation()))) {
            return 30; // 30 minutes de trajet domicile-travail
        }
        
        // Temps de trajet par défaut
        return 20; // 20 minutes par défaut
    }
    
    /**
     * Détermine si une tâche est adaptée à la localisation actuelle
     * @param taskTitle Titre de la tâche
     * @param taskCategory Catégorie de la tâche
     * @return true si la tâche est adaptée, false sinon
     */
    public boolean isTaskSuitableForLocation(String taskTitle, String taskCategory) {
        String currentLocation = getCurrentLocation();
        String homeLocation = getHomeLocation();
        String workLocation = getWorkLocation();
        
        // Déterminer si la tâche est liée au travail
        boolean isWorkTask = isWorkRelatedTask(taskTitle, taskCategory);
        
        // Déterminer si la tâche est liée au domicile
        boolean isHomeTask = isHomeRelatedTask(taskTitle, taskCategory);
        
        // Si la tâche est liée au travail et que l'utilisateur est au travail
        if (isWorkTask && currentLocation.equals(workLocation)) {
            return true;
        }
        
        // Si la tâche est liée au domicile et que l'utilisateur est à la maison
        if (isHomeTask && currentLocation.equals(homeLocation)) {
            return true;
        }
        
        // Si la tâche n'est pas spécifique à un lieu
        if (!isWorkTask && !isHomeTask) {
            return true;
        }
        
        // Par défaut, la tâche n'est pas adaptée à la localisation actuelle
        return false;
    }
    
    /**
     * Détermine si une tâche est liée au travail
     * @param taskTitle Titre de la tâche
     * @param taskCategory Catégorie de la tâche
     * @return true si la tâche est liée au travail, false sinon
     */
    private boolean isWorkRelatedTask(String taskTitle, String taskCategory) {
        String lowerTitle = taskTitle.toLowerCase();
        String lowerCategory = taskCategory.toLowerCase();
        
        // Mots-clés pour les tâches liées au travail
        String[] workKeywords = {
            "réunion", "présentation", "client", "rapport", "email", "collègue", "projet", 
            "bureau", "travail", "professionnel", "business"
        };
        
        // Vérifier si le titre contient un mot-clé lié au travail
        for (String keyword : workKeywords) {
            if (lowerTitle.contains(keyword)) {
                return true;
            }
        }
        
        // Vérifier si la catégorie est liée au travail
        return lowerCategory.contains("travail") || lowerCategory.contains("professionnel") || 
               lowerCategory.contains("bureau");
    }
    
    /**
     * Détermine si une tâche est liée au domicile
     * @param taskTitle Titre de la tâche
     * @param taskCategory Catégorie de la tâche
     * @return true si la tâche est liée au domicile, false sinon
     */
    private boolean isHomeRelatedTask(String taskTitle, String taskCategory) {
        String lowerTitle = taskTitle.toLowerCase();
        String lowerCategory = taskCategory.toLowerCase();
        
        // Mots-clés pour les tâches liées au domicile
        String[] homeKeywords = {
            "ménage", "cuisine", "courses", "lessive", "maison", "jardin", "bricolage", 
            "famille", "domicile", "personnel"
        };
        
        // Vérifier si le titre contient un mot-clé lié au domicile
        for (String keyword : homeKeywords) {
            if (lowerTitle.contains(keyword)) {
                return true;
            }
        }
        
        // Vérifier si la catégorie est liée au domicile
        return lowerCategory.contains("maison") || lowerCategory.contains("personnel") || 
               lowerCategory.contains("famille") || lowerCategory.contains("domicile");
    }
}
