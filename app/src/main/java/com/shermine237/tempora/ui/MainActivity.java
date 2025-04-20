package com.shermine237.tempora.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.ActivityMainBinding;
import com.shermine237.tempora.repository.UserProfileRepository;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserProfileRepository userProfileRepository;
    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_LAST_ACTIVE = "last_active_time";
    private static final String KEY_APP_ACTIVE = "app_active";
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes en millisecondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser le repository du profil utilisateur
        userProfileRepository = new UserProfileRepository(getApplication());
        
        // Vérifier si c'est la première utilisation de l'application
        if (isFirstLaunch()) {
            // Rediriger vers l'activité de création de compte
            startCreateAccountActivity();
            return;
        }
        
        // Vérifier si l'onboarding a été effectué
        if (!isOnboardingCompleted()) {
            startOnboarding();
            return;
        }
        
        // Vérifier si l'application était active ou si la session a expiré
        // Après un force stop, l'application ne sera pas marquée comme active
        // mais nous ne voulons pas bloquer le lancement
        if (isSessionExpired()) {
            // Rediriger vers l'activité de connexion
            startLoginActivity();
            return;
        }
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Configurer la Toolbar comme ActionBar
        setSupportActionBar(binding.toolbar);
        
        // Configure la navigation
        BottomNavigationView navView = binding.bottomNavigation;
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_tasks, R.id.navigation_schedule, 
                R.id.navigation_statistics, R.id.navigation_profile)
                .build();
        
        // Obtenir le NavController à partir du NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        
        // Configurer la barre d'action et la navigation inférieure
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Mettre à jour le temps de dernière activité et marquer l'application comme active
        updateLastActiveTime();
        setAppActive(true);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Mettre à jour le temps de dernière activité
        updateLastActiveTime();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Marquer l'application comme inactive lorsqu'elle est arrêtée
        setAppActive(false);
    }
    
    /**
     * Vérifie si l'onboarding a été effectué
     */
    private boolean isOnboardingCompleted() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }
    
    /**
     * Démarre l'activité d'onboarding
     */
    private void startOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish(); // Terminer l'activité actuelle
    }
    
    /**
     * Initialise le profil utilisateur
     */
    private void initializeUserProfile() {
        // Cette méthode n'est plus utilisée car nous vérifions le premier lancement
        // directement dans onCreate
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
     * Démarre l'activité de création de compte
     */
    private void startCreateAccountActivity() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * Vérifie si la session a expiré
     */
    private boolean isSessionExpired() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastActiveTime = preferences.getLong(KEY_LAST_ACTIVE, 0);
        long currentTime = System.currentTimeMillis();
        
        // Si c'est la première fois, la session n'est pas expirée
        if (lastActiveTime == 0) {
            return false;
        }
        
        // Si le délai d'inactivité est dépassé
        boolean isExpired = (currentTime - lastActiveTime) > SESSION_TIMEOUT;
        
        // Si l'application n'était pas active (après un force stop par exemple)
        // et que la session n'est pas expirée, on considère que c'est OK
        if (!isAppActive() && !isExpired) {
            // Marquer l'application comme active pour éviter des problèmes futurs
            setAppActive(true);
        }
        
        return isExpired;
    }
    
    /**
     * Met à jour le temps de dernière activité
     */
    private void updateLastActiveTime() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_LAST_ACTIVE, System.currentTimeMillis());
        editor.apply();
    }
    
    /**
     * Démarre l'activité de connexion
     */
    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * Vérifie si l'application était active lors de la dernière utilisation
     */
    private boolean isAppActive() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_APP_ACTIVE, false);
    }
    
    /**
     * Définit si l'application est active ou non
     */
    private void setAppActive(boolean active) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_APP_ACTIVE, active);
        editor.apply();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
