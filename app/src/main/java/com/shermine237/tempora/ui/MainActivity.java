package com.shermine237.tempora.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.ActivityMainBinding;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.repository.UserProfileRepository;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserProfileRepository userProfileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Configure les insets pour le mode edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Configure la navigation
        BottomNavigationView navView = binding.bottomNavigation;
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_tasks, R.id.navigation_schedule, 
                R.id.navigation_stats, R.id.navigation_profile)
                .build();
        
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        
        // Initialiser le profil utilisateur par défaut si nécessaire
        initializeUserProfile();
    }
    
    /**
     * Initialise le profil utilisateur par défaut si aucun n'existe
     */
    private void initializeUserProfile() {
        userProfileRepository = new UserProfileRepository(getApplication());
        userProfileRepository.createDefaultProfileIfNotExists("Utilisateur", "utilisateur@example.com");
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
