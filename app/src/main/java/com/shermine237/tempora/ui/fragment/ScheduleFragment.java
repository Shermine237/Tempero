package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.FragmentScheduleBinding;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.service.AIService;
import com.shermine237.tempora.ui.adapter.ScheduleAdapter;
import com.shermine237.tempora.viewmodel.ScheduleViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements ScheduleAdapter.OnScheduleItemClickListener {

    private FragmentScheduleBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private ScheduleAdapter scheduleAdapter;
    private AIService aiService;
    private Date selectedDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH);

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
        updateDateDisplay();
        
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
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTime();
            updateDateDisplay();
            loadScheduleForSelectedDate();
        });
    }

    private void updateDateDisplay() {
        binding.textScheduleDate.setText("Planning du " + dateFormat.format(selectedDate));
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
        aiService.generateScheduleForDate(selectedDate);
        // Un message de chargement pourrait être affiché ici
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
                if (!scheduleItem.isCompleted() && scheduleItem.getType().equals("task")) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                currentSchedule.setCompleted(true);
                scheduleViewModel.update(currentSchedule);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
