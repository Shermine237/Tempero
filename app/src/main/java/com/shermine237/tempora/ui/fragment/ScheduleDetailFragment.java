package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.FragmentScheduleDetailBinding;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.ui.adapter.ScheduleDetailAdapter;
import com.shermine237.tempora.viewmodel.ScheduleViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleDetailFragment extends Fragment implements ScheduleDetailAdapter.OnScheduleItemClickListener {

    private FragmentScheduleDetailBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private ScheduleDetailAdapter scheduleAdapter;
    private Schedule currentSchedule;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduleDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        
        // Configurer le RecyclerView
        setupRecyclerView();
        
        // Récupérer l'ID du planning depuis les arguments
        if (getArguments() != null) {
            int scheduleId = getArguments().getInt("scheduleId");
            loadScheduleDetails(scheduleId);
        }
        
        // Configurer le bouton d'approbation
        binding.buttonApproveSchedule.setOnClickListener(v -> {
            approveSchedule();
        });
    }

    private void setupRecyclerView() {
        scheduleAdapter = new ScheduleDetailAdapter(new ArrayList<>(), this);
        binding.recyclerScheduleItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerScheduleItems.setAdapter(scheduleAdapter);
    }

    private void loadScheduleDetails(int scheduleId) {
        scheduleViewModel.getScheduleById(scheduleId).observe(getViewLifecycleOwner(), schedule -> {
            if (schedule != null) {
                currentSchedule = schedule;
                updateUI(schedule);
            }
        });
    }

    private void updateUI(Schedule schedule) {
        // Date du planning
        if (schedule.getDate() != null) {
            binding.textScheduleDate.setText(dateFormat.format(schedule.getDate()));
        }
        
        // Éléments du planning
        List<ScheduleItem> items = schedule.getItems();
        if (items != null && !items.isEmpty()) {
            scheduleAdapter.updateScheduleItems(items);
            binding.textEmptySchedule.setVisibility(View.GONE);
            binding.recyclerScheduleItems.setVisibility(View.VISIBLE);
        } else {
            binding.textEmptySchedule.setVisibility(View.VISIBLE);
            binding.recyclerScheduleItems.setVisibility(View.GONE);
        }
        
        // Mettre à jour le bouton d'approbation
        if (schedule.isApproved()) {
            binding.buttonApproveSchedule.setText("Planning approuvé");
            binding.buttonApproveSchedule.setEnabled(false);
        } else {
            binding.buttonApproveSchedule.setText("Approuver ce planning");
            binding.buttonApproveSchedule.setEnabled(true);
        }
    }

    private void approveSchedule() {
        if (currentSchedule == null) {
            return;
        }
        
        scheduleViewModel.approveSchedule(currentSchedule);
        Toast.makeText(requireContext(), "Planning approuvé", Toast.LENGTH_SHORT).show();
        binding.buttonApproveSchedule.setText("Planning approuvé");
        binding.buttonApproveSchedule.setEnabled(false);
    }

    @Override
    public void onScheduleItemClick(ScheduleItem item) {
        // Si c'est une tâche, naviguer vers les détails de la tâche
        if (item.getTaskId() > 0) {
            Bundle bundle = new Bundle();
            bundle.putInt("taskId", item.getTaskId());
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_schedule_detail_to_navigation_task_detail, bundle);
        }
    }

    @Override
    public void onScheduleItemCompleteClick(ScheduleItem item, boolean isCompleted) {
        // Mettre à jour l'état de l'élément du planning
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
                Toast.makeText(requireContext(), "Toutes les tâches sont terminées !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
