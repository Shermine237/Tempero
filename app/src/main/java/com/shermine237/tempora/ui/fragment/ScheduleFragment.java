package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
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
import com.shermine237.tempora.service.AIService;
import com.shermine237.tempora.ui.adapter.ScheduleAdapter;
import com.shermine237.tempora.ui.decorator.ScheduleDateDecorator;
import com.shermine237.tempora.viewmodel.ScheduleViewModel;

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
    private ScheduleAdapter scheduleAdapter;
    private AIService aiService;
    private Date selectedDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH);
    private ScheduleDateDecorator scheduleDateDecorator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        
        // Initialiser le service IA
        aiService = new AIService(requireActivity().getApplication());
        
        // Initialiser la date sélectionnée à aujourd'hui
        selectedDate = new Date();
        
        // Configurer le RecyclerView
        setupRecyclerView();
        
        // Configurer le calendrier
        setupCalendar();
        
        // Observer les changements de données
        observeScheduleData();
        
        // Configurer le bouton de génération de planning
        binding.fabGenerateSchedule.setOnClickListener(v -> {
            generateSchedule();
        });
    }

    private void setupRecyclerView() {
        scheduleAdapter = new ScheduleAdapter(new ArrayList<>(), this);
        binding.recyclerSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerSchedule.setAdapter(scheduleAdapter);
    }

    private void setupCalendar() {
        // Initialiser le décorateur avec un ensemble vide
        scheduleDateDecorator = new ScheduleDateDecorator(requireContext(), new HashSet<>());
        binding.calendarView.addDecorator(scheduleDateDecorator);
        
        // Configurer le listener de sélection de date
        binding.calendarView.setOnDateChangedListener(this);
        
        // Sélectionner la date du jour
        CalendarDay today = CalendarDay.today();
        binding.calendarView.setDateSelected(today, true);
        
        // Charger les dates avec des plannings
        loadScheduleDates();
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
        scheduleViewModel.getScheduleForDate(selectedDate).observe(getViewLifecycleOwner(), schedule -> {
            updateScheduleList(schedule);
        });
    }

    private void updateScheduleList(Schedule schedule) {
        if (schedule != null && schedule.getItems() != null && !schedule.getItems().isEmpty()) {
            scheduleAdapter.updateScheduleItems(schedule.getItems());
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
        
        // Générer le planning
        aiService.generateScheduleForDate(selectedDate);
        
        // Attendre un peu puis recharger le planning
        new Handler().postDelayed(() -> {
            loadScheduleForSelectedDate();
            loadScheduleDates(); // Recharger les dates avec des plannings
            binding.fabGenerateSchedule.setEnabled(true);
        }, 3000); // Attendre 3 secondes avant de recharger
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
            calendar.set(date.getYear(), date.getMonth() - 1, date.getDay()); // Ajuster le mois (0-11)
            selectedDate = calendar.getTime();
            
            // Charger le planning pour la date sélectionnée
            loadScheduleForSelectedDate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
