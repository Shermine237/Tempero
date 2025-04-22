package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.FragmentScheduleBinding;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.service.AIService;
import com.shermine237.tempora.ui.adapter.ScheduleAdapter;
import com.shermine237.tempora.ui.decorator.ScheduleDateDecorator;
import com.shermine237.tempora.ui.decorator.TodayDecorator;
import com.shermine237.tempora.viewmodel.ScheduleViewModel;
import com.shermine237.tempora.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduleFragment extends Fragment implements ScheduleAdapter.OnScheduleItemClickListener, OnDateSelectedListener {

    private FragmentScheduleBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private TaskViewModel taskViewModel;
    private AIService aiService;
    private ScheduleAdapter scheduleAdapter;
    private ScheduleDateDecorator scheduleDateDecorator;
    private TodayDecorator todayDecorator;
    private Date selectedDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser les ViewModels
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        
        // Initialiser le service AI
        aiService = new AIService(requireActivity().getApplication());
        
        // Configurer le RecyclerView
        scheduleAdapter = new ScheduleAdapter(new ArrayList<>(), this);
        binding.recyclerSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerSchedule.setAdapter(scheduleAdapter);
        
        // Configurer le calendrier
        binding.calendarView.setOnDateChangedListener(this);
        
        // Initialiser le décorateur pour les dates avec des plannings
        scheduleDateDecorator = new ScheduleDateDecorator(requireContext(), new HashSet<>());
        binding.calendarView.addDecorator(scheduleDateDecorator);
        
        // Ajouter un décorateur pour la date du jour
        todayDecorator = new TodayDecorator(requireContext());
        binding.calendarView.addDecorator(todayDecorator);
        
        // Sélectionner la date du jour par défaut
        Calendar calendar = Calendar.getInstance();
        selectedDate = calendar.getTime();
        binding.textSelectedDate.setText(dateFormat.format(selectedDate));
        
        // Charger les dates avec des plannings
        loadScheduleDates();
        
        // Observer les données du planning
        observeScheduleData();
        
        // Configurer le bouton de génération de planning
        binding.fabGenerateSchedule.setOnClickListener(v -> {
            showGenerateScheduleConfirmationDialog();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Réinitialiser la vue à l'état d'origine avec le calendrier visible
        resetView();
        
        // Recharger les données à chaque fois que le fragment devient visible
        loadScheduleForSelectedDate();
        loadScheduleDates();
    }

    /**
     * Réinitialise la vue à l'état d'origine avec le calendrier visible
     */
    private void resetView() {
        // Afficher le calendrier
        binding.calendarView.setVisibility(View.VISIBLE);
        
        // Masquer le planning s'il est vide
        if (scheduleAdapter.getItemCount() == 0) {
            binding.textEmptySchedule.setVisibility(View.VISIBLE);
            binding.recyclerSchedule.setVisibility(View.GONE);
        }
        
        // Réinitialiser le titre à la date sélectionnée
        binding.textSelectedDate.setText(dateFormat.format(selectedDate));
        
        // Log pour le débogage
        Log.d("ScheduleFragment", "Vue réinitialisée à l'état d'origine");
    }

    private void loadScheduleDates() {
        scheduleViewModel.getAllSchedules().observe(getViewLifecycleOwner(), schedules -> {
            if (schedules != null && !schedules.isEmpty()) {
                Set<CalendarDay> scheduleDates = new HashSet<>();
                
                for (Schedule schedule : schedules) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(schedule.getDate());
                    
                    CalendarDay day = CalendarDay.from(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1, // MaterialCalendarView utilise 1-12 pour les mois
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    
                    scheduleDates.add(day);
                }
                
                // Mettre à jour le décorateur avec les nouvelles dates
                scheduleDateDecorator.setDates(scheduleDates);
                binding.calendarView.invalidateDecorators();
            }
        });
    }

    private void observeScheduleData() {
        loadScheduleForSelectedDate();
    }

    private void loadScheduleForSelectedDate() {
        // Charger le planning généré par l'IA pour la date sélectionnée
        scheduleViewModel.getScheduleForDate(selectedDate).observe(getViewLifecycleOwner(), schedule -> {
            // Charger les tâches planifiées manuellement pour cette date
            taskViewModel.getTasksScheduledForDate(selectedDate).observe(getViewLifecycleOwner(), tasks -> {
                // Combiner le planning généré et les tâches planifiées manuellement
                List<ScheduleItem> combinedItems = new ArrayList<>();
                
                // Ajouter les éléments du planning généré s'il existe
                if (schedule != null && schedule.getItems() != null && !schedule.getItems().isEmpty()) {
                    combinedItems.addAll(schedule.getItems());
                }
                
                // Convertir les tâches planifiées manuellement en éléments de planning et les ajouter
                if (tasks != null && !tasks.isEmpty()) {
                    for (Task task : tasks) {
                        // Créer un élément de planning à partir de la tâche
                        ScheduleItem item = convertTaskToScheduleItem(task);
                        if (item != null) {
                            combinedItems.add(item);
                        }
                    }
                }
                
                // Mettre à jour l'interface utilisateur
                updateScheduleList(combinedItems);
            });
        });
    }
    
    /**
     * Convertit une tâche en élément de planning
     * @param task Tâche à convertir
     * @return Élément de planning correspondant à la tâche
     */
    private ScheduleItem convertTaskToScheduleItem(Task task) {
        if (task.getScheduledDate() == null) {
            return null;
        }
        
        // Créer une heure de début à partir de la date planifiée
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(task.getScheduledDate());
        Date startTime = startCal.getTime();
        
        // Créer une heure de fin en ajoutant la durée estimée
        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.MINUTE, task.getEstimatedDuration());
        Date endTime = endCal.getTime();
        
        // Créer un élément de planning avec l'ID de la tâche
        // Utiliser l'attribut aiGenerated pour déterminer si la tâche a été générée par l'IA
        return new ScheduleItem(task.getId(), task.getTitle(), startTime, endTime, !task.isAiGenerated());
    }

    private void updateScheduleList(List<ScheduleItem> items) {
        if (items != null && !items.isEmpty()) {
            // Trier les éléments par heure de début
            items.sort((item1, item2) -> item1.getStartTime().compareTo(item2.getStartTime()));
            
            scheduleAdapter.updateScheduleItems(items);
            binding.textEmptySchedule.setVisibility(View.GONE);
            binding.recyclerSchedule.setVisibility(View.VISIBLE);
        } else {
            binding.textEmptySchedule.setVisibility(View.VISIBLE);
            binding.recyclerSchedule.setVisibility(View.GONE);
        }
    }

    private void generateSchedule() {
        // Afficher un indicateur de chargement
        binding.textEmptySchedule.setText("Génération du planning en cours...");
        binding.textEmptySchedule.setVisibility(View.VISIBLE);
        binding.recyclerSchedule.setVisibility(View.GONE);
        
        // Désactiver le bouton pendant la génération
        binding.fabGenerateSchedule.setEnabled(false);
        
        // Observer l'état de génération du planning
        aiService.getIsGenerating().observe(getViewLifecycleOwner(), isGenerating -> {
            if (!isGenerating) {
                // La génération est terminée, recharger le planning
                loadScheduleForSelectedDate();
                loadScheduleDates(); // Recharger les dates avec des plannings
                binding.fabGenerateSchedule.setEnabled(true);
                
                // Arrêter d'observer
                aiService.getIsGenerating().removeObservers(getViewLifecycleOwner());
            }
        });
        
        // Générer le planning
        Log.d("ScheduleFragment", "Génération du planning pour la date sélectionnée: " + selectedDate);
        aiService.generateScheduleForDate(selectedDate);
    }

    private void showGenerateScheduleConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Générer un planning");
        builder.setMessage("Voulez-vous générer un planning pour " + dateFormat.format(selectedDate) + " ?\n\nCela remplacera tout planning existant pour cette date.");
        
        builder.setPositiveButton("Générer", (dialog, which) -> {
            generateSchedule();
        });
        
        builder.setNegativeButton("Annuler", (dialog, which) -> {
            dialog.dismiss();
        });
        
        builder.create().show();
    }

    @Override
    public void onScheduleItemClick(ScheduleItem item) {
        if (item.getTaskId() > 0) {
            // Si c'est une tâche, naviguer vers les détails de la tâche
            Bundle bundle = new Bundle();
            bundle.putInt("taskId", item.getTaskId());
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_schedule_to_task_detail, bundle);
        } else {
            // Si c'est un autre type d'élément (pause, repas), naviguer vers les détails du planning
            Bundle bundle = new Bundle();
            // Utiliser la date sélectionnée comme identifiant du planning
            bundle.putLong("date", selectedDate.getTime());
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_schedule_to_schedule_detail, bundle);
        }
    }

    @Override
    public void onScheduleItemCompleteClick(ScheduleItem item, boolean isCompleted) {
        // Mettre à jour l'état de l'élément du planning
        Schedule currentSchedule = scheduleViewModel.getScheduleForDate(selectedDate).getValue();
        if (currentSchedule != null) {
            List<ScheduleItem> items = currentSchedule.getItems();
            for (ScheduleItem scheduleItem : items) {
                if (scheduleItem.getTitle().equals(item.getTitle()) && 
                    scheduleItem.getStartTime().equals(item.getStartTime())) {
                    scheduleItem.setCompleted(isCompleted);
                    break;
                }
            }
            currentSchedule.setItems(items);
            scheduleViewModel.update(currentSchedule);
            
            // Vérifier si tous les éléments sont complétés
            boolean allCompleted = true;
            for (ScheduleItem scheduleItem : items) {
                if (!scheduleItem.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                // Marquer le planning comme complété
                currentSchedule.setCompleted(true);
                scheduleViewModel.update(currentSchedule);
            }
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if (selected) {
            // Convertir CalendarDay en Date
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, date.getYear());
            calendar.set(Calendar.MONTH, date.getMonth() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, date.getDay());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDate = calendar.getTime();
            
            // Mettre à jour le titre avec la date sélectionnée
            binding.textSelectedDate.setText(dateFormat.format(selectedDate));
            
            // Réinitialiser le texte vide
            binding.textEmptySchedule.setText("Aucun planning pour cette date");
            
            // Charger le planning pour la date sélectionnée
            loadScheduleForSelectedDate();
        }
    }
}
