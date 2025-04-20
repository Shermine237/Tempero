package com.shermine237.tempora.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shermine237.tempora.databinding.ActivityOnboardingBinding;
import com.shermine237.tempora.model.UserProfile;
import com.shermine237.tempora.viewmodel.UserProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private UserProfileViewModel userProfileViewModel;
    private final CheckBox[] dayCheckboxes = new CheckBox[7];
    private final String[] hours = new String[24];
    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialiser le ViewModel
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        // Initialiser les tableaux d'heures
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
        }

        // Configurer les spinners d'heures
        setupHourSpinners();

        // Configurer les checkboxes des jours
        setupDayCheckboxes();

        // Configurer le bouton de démarrage
        binding.buttonGetStarted.setOnClickListener(v -> createProfile());
    }

    private void setupHourSpinners() {
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinnerStartHour.setAdapter(hourAdapter);
        binding.spinnerEndHour.setAdapter(hourAdapter);

        // Définir les valeurs par défaut (9h - 17h)
        binding.spinnerStartHour.setSelection(9);
        binding.spinnerEndHour.setSelection(17);
    }

    private void setupDayCheckboxes() {
        dayCheckboxes[0] = binding.checkboxSunday;
        dayCheckboxes[1] = binding.checkboxMonday;
        dayCheckboxes[2] = binding.checkboxTuesday;
        dayCheckboxes[3] = binding.checkboxWednesday;
        dayCheckboxes[4] = binding.checkboxThursday;
        dayCheckboxes[5] = binding.checkboxFriday;
        dayCheckboxes[6] = binding.checkboxSaturday;

        // Cocher les jours de la semaine par défaut (lundi à vendredi)
        for (int i = 1; i <= 5; i++) {
            dayCheckboxes[i].setChecked(true);
        }
    }

    /**
     * Crée un profil utilisateur avec les préférences de travail
     */
    private void createProfile() {
        // Récupérer les informations personnelles
        String name = binding.editName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer un nouveau profil utilisateur
        UserProfile profile = new UserProfile(name, email);

        // Récupérer les heures de travail
        int startHour = binding.spinnerStartHour.getSelectedItemPosition();
        int endHour = binding.spinnerEndHour.getSelectedItemPosition();
        profile.setPreferredWorkStartHour(startHour);
        profile.setPreferredWorkEndHour(endHour);

        // Récupérer les jours de travail
        List<Integer> workDays = new ArrayList<>();
        for (int i = 0; i < dayCheckboxes.length; i++) {
            if (dayCheckboxes[i].isChecked()) {
                workDays.add(i);
            }
        }
        profile.setWorkDays(workDays);

        // Enregistrer le profil
        userProfileViewModel.insert(profile);

        // Marquer l'onboarding comme terminé
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
        editor.apply();

        // Rediriger vers l'activité principale
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
