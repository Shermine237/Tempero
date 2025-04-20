package com.shermine237.tempora.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.ActivityLoginBinding;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.viewmodel.UserProfileViewModel;

/**
 * Activité de connexion pour les utilisateurs existants
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserProfileViewModel userProfileViewModel;
    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_LAST_LOGIN = "last_login";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    private static final String KEY_APP_ACTIVE = "app_active";
    private static final long AUTO_LOGIN_TIMEOUT = 7 * 24 * 60 * 60 * 1000; // 7 jours en millisecondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser le binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialiser le ViewModel
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        
        // Vérifier si l'utilisateur peut être connecté automatiquement
        if (checkAutoLogin()) {
            // Connexion automatique
            proceedToMainActivity();
            return;
        }
        
        // Charger les préférences de connexion
        loadLoginPreferences();
        
        // Configurer les listeners
        setupListeners();
    }
    
    /**
     * Configure les listeners pour les éléments de l'interface utilisateur
     */
    private void setupListeners() {
        // Bouton de connexion
        binding.buttonLogin.setOnClickListener(v -> login());
        
        // Lien "Mot de passe oublié"
        binding.textForgotPassword.setOnClickListener(v -> {
            // Pour l'instant, afficher simplement un message Toast
            Toast.makeText(this, "Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
        });
        
        // Lien "Créer un compte"
        binding.textCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }
    
    /**
     * Vérifie si l'utilisateur peut être connecté automatiquement
     */
    private boolean checkAutoLogin() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean autoLogin = preferences.getBoolean(KEY_AUTO_LOGIN, false);
        
        if (autoLogin) {
            long lastLogin = preferences.getLong(KEY_LAST_LOGIN, 0);
            long currentTime = System.currentTimeMillis();
            
            // Vérifier si le délai d'auto-login n'est pas expiré
            if (currentTime - lastLogin < AUTO_LOGIN_TIMEOUT) {
                return true;
            } else {
                // Désactiver l'auto-login si le délai est expiré
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_AUTO_LOGIN, false);
                editor.apply();
            }
        }
        
        return false;
    }
    
    /**
     * Charge les préférences de connexion
     */
    private void loadLoginPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER_ME, false);
        
        if (rememberMe) {
            String savedEmail = preferences.getString(KEY_EMAIL, "");
            binding.editEmail.setText(savedEmail);
            binding.checkboxRememberMe.setChecked(true);
        }
    }
    
    /**
     * Gère la connexion de l'utilisateur
     */
    private void login() {
        // Récupérer les valeurs des champs
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString().trim();
        
        // Valider les champs
        if (validateFields(email, password)) {
            // Afficher l'indicateur de chargement
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.buttonLogin.setEnabled(false);
            
            // Vérifier les informations d'identification
            verifyCredentials(email, password);
        }
    }
    
    /**
     * Valide les champs du formulaire
     */
    private boolean validateFields(String email, String password) {
        boolean isValid = true;
        
        // Valider l'email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Veuillez entrer une adresse email valide");
            isValid = false;
        } else {
            binding.layoutEmail.setError(null);
        }
        
        // Valider le mot de passe
        if (TextUtils.isEmpty(password)) {
            binding.layoutPassword.setError("Veuillez entrer votre mot de passe");
            isValid = false;
        } else {
            binding.layoutPassword.setError(null);
        }
        
        return isValid;
    }
    
    /**
     * Vérifie les informations d'identification de l'utilisateur
     */
    private void verifyCredentials(String email, String password) {
        // Simuler une vérification d'identifiants avec un délai
        new Handler().postDelayed(() -> {
            // Rechercher l'utilisateur par email
            userProfileViewModel.getUserProfileByEmail(email).observe(this, userProfile -> {
                if (userProfile != null) {
                    // Dans une application réelle, nous vérifierions le mot de passe haché
                    // Pour cette démo, nous vérifions simplement que le mot de passe n'est pas vide
                    if (password.length() >= 6) {
                        loginSuccess(email, userProfile);
                    } else {
                        loginFailed("Mot de passe incorrect");
                    }
                } else {
                    loginFailed("Aucun compte trouvé avec cette adresse email");
                }
            });
        }, 1500); // Délai de 1.5 secondes pour simuler une vérification
    }
    
    /**
     * Gère le succès de la connexion
     */
    private void loginSuccess(String email, UserProfile userProfile) {
        // Masquer l'indicateur de chargement
        binding.progressBar.setVisibility(View.GONE);
        binding.buttonLogin.setEnabled(true);
        
        // Enregistrer les préférences de connexion
        saveLoginPreferences(email);
        
        // Afficher un message de succès
        Toast.makeText(this, "Connexion réussie ! Bienvenue, " + userProfile.getName(), Toast.LENGTH_SHORT).show();
        
        // Rediriger vers l'activité principale
        proceedToMainActivity();
    }
    
    /**
     * Gère l'échec de la connexion
     */
    private void loginFailed(String errorMessage) {
        // Masquer l'indicateur de chargement
        binding.progressBar.setVisibility(View.GONE);
        binding.buttonLogin.setEnabled(true);
        
        // Afficher un message d'erreur
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Enregistre les préférences de connexion
     */
    private void saveLoginPreferences(String email) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        // Enregistrer l'option "Se souvenir de moi"
        boolean rememberMe = binding.checkboxRememberMe.isChecked();
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        
        if (rememberMe) {
            // Enregistrer l'email
            editor.putString(KEY_EMAIL, email);
        } else {
            // Effacer l'email
            editor.remove(KEY_EMAIL);
        }
        
        // Enregistrer la date de dernière connexion
        long currentTime = System.currentTimeMillis();
        editor.putLong(KEY_LAST_LOGIN, currentTime);
        
        // Activer l'auto-login
        editor.putBoolean(KEY_AUTO_LOGIN, true);
        
        // Marquer l'application comme active
        editor.putBoolean(KEY_APP_ACTIVE, true);
        
        editor.apply();
    }
    
    /**
     * Redirige vers l'activité principale
     */
    private void proceedToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
