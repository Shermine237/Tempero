package com.shermine237.tempora.ai.backend;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Service météo pour l'IA
 * Ce service permet d'obtenir les prévisions météorologiques pour optimiser la planification des tâches
 */
public class WeatherService {
    
    private static final String TAG = "WeatherService";
    
    // Types de météo
    public static final int WEATHER_SUNNY = 0;
    public static final int WEATHER_CLOUDY = 1;
    public static final int WEATHER_RAINY = 2;
    public static final int WEATHER_SNOWY = 3;
    
    // Cache des prévisions météo
    private Map<String, Integer> weatherForecast;
    private Random random;
    
    /**
     * Constructeur
     */
    public WeatherService() {
        weatherForecast = new HashMap<>();
        random = new Random();
        Log.i(TAG, "Service météo initialisé");
    }
    
    /**
     * Obtient les prévisions météorologiques pour une date donnée
     * @param date Date pour laquelle obtenir les prévisions
     * @param location Localisation (ville ou coordonnées)
     * @return Type de météo prévu
     */
    public int getWeatherForecast(Date date, String location) {
        // Clé de cache pour cette date et cette localisation
        String cacheKey = formatDate(date) + "_" + location;
        
        // Vérifier si nous avons déjà les prévisions en cache
        if (weatherForecast.containsKey(cacheKey)) {
            return weatherForecast.get(cacheKey);
        }
        
        // Dans une implémentation réelle, nous ferions un appel API à un service météo
        // Pour cette démo, nous générons des prévisions aléatoires
        int forecast = simulateWeatherForecast(date);
        
        // Mettre en cache les prévisions
        weatherForecast.put(cacheKey, forecast);
        
        Log.d(TAG, "Prévisions météo pour " + formatDate(date) + " à " + location + ": " + getWeatherDescription(forecast));
        
        return forecast;
    }
    
    /**
     * Détermine si une tâche est adaptée aux conditions météorologiques
     * @param taskTitle Titre de la tâche
     * @param taskCategory Catégorie de la tâche
     * @param date Date de la tâche
     * @param location Localisation
     * @return true si la tâche est adaptée, false sinon
     */
    public boolean isTaskSuitableForWeather(String taskTitle, String category, Date date, String location) {
        int forecast = getWeatherForecast(date, location);
        
        // Tâches extérieures
        boolean isOutdoorTask = isOutdoorTask(taskTitle, category);
        
        // Vérifier si la météo est adaptée pour les tâches extérieures
        if (isOutdoorTask) {
            return forecast == WEATHER_SUNNY || forecast == WEATHER_CLOUDY;
        }
        
        // Pour les tâches intérieures, la météo n'a pas d'importance
        return true;
    }
    
    /**
     * Détermine si une tâche est une activité extérieure
     * @param taskTitle Titre de la tâche
     * @param category Catégorie de la tâche
     * @return true si c'est une tâche extérieure, false sinon
     */
    private boolean isOutdoorTask(String taskTitle, String category) {
        String lowerTitle = taskTitle.toLowerCase();
        String lowerCategory = category.toLowerCase();
        
        // Mots-clés pour les activités extérieures
        String[] outdoorKeywords = {
            "course", "jogging", "marche", "randonnée", "vélo", "cyclisme", "pique-nique", 
            "jardin", "jardinage", "extérieur", "parc", "promenade", "sport"
        };
        
        // Vérifier si le titre contient un mot-clé d'activité extérieure
        for (String keyword : outdoorKeywords) {
            if (lowerTitle.contains(keyword)) {
                return true;
            }
        }
        
        // Vérifier si la catégorie est liée aux activités extérieures
        return lowerCategory.contains("sport") || lowerCategory.contains("extérieur") || 
               lowerCategory.contains("plein air") || lowerCategory.contains("fitness");
    }
    
    /**
     * Simule des prévisions météorologiques
     * @param date Date pour laquelle simuler les prévisions
     * @return Type de météo simulé
     */
    private int simulateWeatherForecast(Date date) {
        // Utiliser la date comme graine pour la génération aléatoire
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        
        random.setSeed(day + year * 1000);
        
        // Générer des prévisions aléatoires mais cohérentes pour une date donnée
        int month = calendar.get(Calendar.MONTH);
        
        // Ajuster les probabilités en fonction de la saison
        if (month >= Calendar.DECEMBER || month <= Calendar.FEBRUARY) {
            // Hiver: plus de chances de neige et de pluie
            int[] possibilities = {
                WEATHER_CLOUDY, WEATHER_CLOUDY, WEATHER_RAINY, WEATHER_RAINY, WEATHER_SNOWY, WEATHER_SUNNY
            };
            return possibilities[random.nextInt(possibilities.length)];
        } else if (month >= Calendar.MARCH && month <= Calendar.MAY) {
            // Printemps: plus de chances de pluie et de soleil
            int[] possibilities = {
                WEATHER_SUNNY, WEATHER_SUNNY, WEATHER_CLOUDY, WEATHER_CLOUDY, WEATHER_RAINY
            };
            return possibilities[random.nextInt(possibilities.length)];
        } else if (month >= Calendar.JUNE && month <= Calendar.AUGUST) {
            // Été: plus de chances de soleil
            int[] possibilities = {
                WEATHER_SUNNY, WEATHER_SUNNY, WEATHER_SUNNY, WEATHER_CLOUDY, WEATHER_RAINY
            };
            return possibilities[random.nextInt(possibilities.length)];
        } else {
            // Automne: plus de chances de pluie et de nuages
            int[] possibilities = {
                WEATHER_CLOUDY, WEATHER_CLOUDY, WEATHER_RAINY, WEATHER_RAINY, WEATHER_SUNNY
            };
            return possibilities[random.nextInt(possibilities.length)];
        }
    }
    
    /**
     * Formate une date pour l'utiliser comme clé de cache
     * @param date Date à formater
     * @return Date formatée (AAAA-MM-JJ)
     */
    private String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        return calendar.get(Calendar.YEAR) + "-" + 
               (calendar.get(Calendar.MONTH) + 1) + "-" + 
               calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Obtient une description textuelle de la météo
     * @param weatherType Type de météo
     * @return Description textuelle
     */
    public String getWeatherDescription(int weatherType) {
        switch (weatherType) {
            case WEATHER_SUNNY:
                return "Ensoleillé";
            case WEATHER_CLOUDY:
                return "Nuageux";
            case WEATHER_RAINY:
                return "Pluvieux";
            case WEATHER_SNOWY:
                return "Neigeux";
            default:
                return "Inconnu";
        }
    }
}
