package com.shermine237.tempora.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.ActivitySplashBinding;

/**
 * Activité d'écran de démarrage avec animation
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_APP_ACTIVE = "app_active";
    private static final int SPLASH_DURATION = 3000; // 3 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser le binding
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Lancer les animations
        startAnimations();
        
        // Rediriger vers l'écran approprié après un délai
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }
    
    /**
     * Lance les animations de l'écran de démarrage
     */
    private void startAnimations() {
        // Animation du logo
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        binding.imageLogo.startAnimation(logoAnimation);
        
        // Animation du texte
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.text_animation);
        binding.textAppName.startAnimation(textAnimation);
        binding.textSlogan.startAnimation(textAnimation);
    }
    
    /**
     * Redirige vers l'écran approprié en fonction de l'état de l'application
     */
    private void navigateToNextScreen() {
        // Vérifier si c'est la première utilisation de l'application
        if (isFirstLaunch()) {
            // Rediriger vers l'activité de création de compte
            startActivity(new Intent(this, CreateAccountActivity.class));
        } else if (!isOnboardingCompleted()) {
            // Rediriger vers l'activité d'onboarding
            startActivity(new Intent(this, OnboardingActivity.class));
        } else if (!isAppActive()) {
            // Rediriger vers l'activité de connexion
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            // Rediriger vers l'activité principale
            startActivity(new Intent(this, MainActivity.class));
        }
        
        // Terminer cette activité
        finish();
        
        // Ajouter une transition fluide
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    /**
     * Vérifie si c'est la première fois que l'application est lancée
     */
    private boolean isFirstLaunch() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Par défaut, c'est la première fois (true)
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    
    /**
     * Vérifie si l'onboarding a été effectué
     */
    private boolean isOnboardingCompleted() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }
    
    /**
     * Vérifie si l'application était active lors de la dernière utilisation
     */
    private boolean isAppActive() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_APP_ACTIVE, false);
    }
}
