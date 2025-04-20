package com.shermine237.tempora.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shermine237.tempora.R;
import com.shermine237.tempora.databinding.FragmentTasksBinding;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.ui.adapter.TaskAdapter;
import com.shermine237.tempora.viewmodel.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private FragmentTasksBinding binding;
    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        
        // Configurer le RecyclerView
        setupRecyclerView();
        
        // Observer les changements de données
        observeTaskData();
        
        // Configurer le bouton d'ajout de tâche
        binding.fabAddTask.setOnClickListener(v -> {
            // Utiliser l'action définie dans le fichier de navigation
            Navigation.findNavController(v).navigate(R.id.action_navigation_tasks_to_task_create);
        });
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTasks.setAdapter(taskAdapter);
    }

    private void observeTaskData() {
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            updateTaskList(tasks);
        });
    }

    private void updateTaskList(List<Task> tasks) {
        if (tasks != null && !tasks.isEmpty()) {
            taskAdapter.updateTasks(tasks);
            binding.textEmptyTasks.setVisibility(View.GONE);
            binding.recyclerTasks.setVisibility(View.VISIBLE);
        } else {
            binding.textEmptyTasks.setVisibility(View.VISIBLE);
            binding.recyclerTasks.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTaskClick(Task task) {
        // Naviguer vers les détails de la tâche
        Bundle bundle = new Bundle();
        bundle.putInt("taskId", task.getId());
        Navigation.findNavController(binding.getRoot())
                .navigate(R.id.action_navigation_tasks_to_task_detail, bundle);
    }

    @Override
    public void onTaskCompleteClick(Task task, boolean isCompleted) {
        if (isCompleted) {
            taskViewModel.completeTask(task);
        } else {
            task.setCompleted(false);
            task.setCompletionDate(null);
            taskViewModel.update(task);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
