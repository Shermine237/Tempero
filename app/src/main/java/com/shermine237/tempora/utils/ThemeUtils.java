package com.shermine237.tempora.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utilitaire pour gérer les thèmes de l'application.
 */
public class ThemeUtils {

    public static final int THEME_FOLLOW_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    /**
     * Applique le thème spécifié à l'application.
     * 
     * @param themePreference Le thème à appliquer (0 = système, 1 = clair, 2 = sombre)
     */
    public static void applyTheme(int themePreference) {
        int nightMode;
        
        switch (themePreference) {
            case THEME_LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case THEME_DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case THEME_FOLLOW_SYSTEM:
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                } else {
                    nightMode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                }
                break;
        }
        
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
