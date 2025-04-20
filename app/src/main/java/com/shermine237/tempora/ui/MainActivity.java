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
    private static final String PREFS_NAME = "TemporaPrefs";
    private static final String KEY_FIRST_LAUNCH = "firstLaunch";

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
    
    /**
     * Vérifie si l'onboarding a été effectué
     */
    private boolean isOnboardingCompleted() {
        SharedPreferences preferences = getSharedPreferences("onboarding_prefs", MODE_PRIVATE);
        return preferences.getBoolean("onboarding_completed", false);
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
    
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
