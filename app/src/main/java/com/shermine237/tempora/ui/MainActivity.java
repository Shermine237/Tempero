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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Vérifier si l'onboarding a été effectué
        if (!isOnboardingCompleted()) {
            startOnboarding();
            return;
        }
        
        // Initialiser le profil utilisateur
        initializeUserProfile();
        
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
        SharedPreferences prefs = getSharedPreferences("TemporaPrefs", MODE_PRIVATE);
        return prefs.getBoolean("onboarding_completed", false);
    }
    
    /**
     * Démarre l'activité d'onboarding
     */
    private void startOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * Initialise le profil utilisateur
     */
    private void initializeUserProfile() {
        userProfileRepository = new UserProfileRepository(getApplication());
        
        // Nous avons supprimé la partie concernant le thème car elle n'est pas encore implémentée
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
