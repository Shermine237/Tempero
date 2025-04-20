package com.shermine237.tempora.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.ActivityCreateAccountBinding;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.viewmodel.UserProfileViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité de création de compte pour les nouveaux utilisateurs
 */
public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;
    private UserProfileViewModel userProfileViewModel;
    private static final String TAG = "CreateAccountActivity";
    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser le binding
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialiser le ViewModel
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        
        // Configurer le bouton de création de compte
        binding.buttonCreateAccount.setOnClickListener(v -> createAccount());
    }
    
    /**
     * Crée un compte utilisateur avec les informations fournies
     */
    private void createAccount() {
        // Récupérer les valeurs des champs
        String name = binding.editName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString().trim();
        
        // Valider les champs
        if (validateFields(name, email, password)) {
            // Créer un profil utilisateur
            UserProfile userProfile = new UserProfile(name, email);
            
            // Définir des valeurs par défaut pour les préférences
            userProfile.setPreferredWorkStartHour(9); // 9h00
            userProfile.setPreferredWorkEndHour(17);  // 17h00
            userProfile.setShortBreakDuration(15);    // 15 minutes
            userProfile.setLongBreakDuration(30);     // 30 minutes
            userProfile.setWorkSessionsBeforeLongBreak(4); // 4 sessions avant une pause longue
            
            // Initialiser les jours de travail (lundi à vendredi par défaut)
            List<Integer> workDays = new ArrayList<>();
            workDays.add(1); // Lundi
            workDays.add(2); // Mardi
            workDays.add(3); // Mercredi
            workDays.add(4); // Jeudi
            workDays.add(5); // Vendredi
            userProfile.setWorkDays(workDays);
            
            // Initialiser les catégories personnalisées
            List<String> customCategories = new ArrayList<>();
            customCategories.add("Travail");
            customCategories.add("Personnel");
            customCategories.add("Études");
            customCategories.add("Santé");
            userProfile.setCustomCategories(customCategories);
            
            // Enregistrer le profil utilisateur
            userProfileViewModel.insert(userProfile);
            
            // Marquer l'application comme ayant déjà été lancée
            markFirstLaunchComplete();
            
            // Afficher un message de succès
            Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
            
            // Rediriger vers l'activité principale
            startMainActivity();
        }
    }
    
    /**
     * Valide les champs du formulaire
     */
    private boolean validateFields(String name, String email, String password) {
        boolean isValid = true;
        
        // Valider le nom
        if (TextUtils.isEmpty(name)) {
            binding.layoutName.setError("Veuillez entrer votre nom");
            isValid = false;
        } else {
            binding.layoutName.setError(null);
        }
        
        // Valider l'email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Veuillez entrer une adresse email valide");
            isValid = false;
        } else {
            binding.layoutEmail.setError(null);
        }
        
        // Valider le mot de passe
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.layoutPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            isValid = false;
        } else {
            binding.layoutPassword.setError(null);
        }
        
        return isValid;
    }
    
    /**
     * Marque l'application comme ayant déjà été lancée
     */
    private void markFirstLaunchComplete() {
        // Marquer que ce n'est plus le premier lancement et que l'onboarding est terminé
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
        editor.apply();
        
        Log.d(TAG, "Premier lancement et onboarding marqués comme terminés");
    }
    
    /**
     * Démarre l'activité principale
     */
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
