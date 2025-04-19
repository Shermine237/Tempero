package com.shermine237.tempora.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shermine237.tempora.databinding.ItemTaskBinding;
import com.shermine237.tempora.model.Task;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private final OnTaskClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskCompleteClick(Task task, boolean isCompleted);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Task task, OnTaskClickListener listener) {
            binding.textTaskTitle.setText(task.getTitle());
            binding.textTaskDescription.setText(task.getDescription());
            
            if (task.getDueDate() != null) {
                binding.textTaskDueDate.setText("Échéance: " + dateFormat.format(task.getDueDate()));
            } else {
                binding.textTaskDueDate.setText("Échéance: Non définie");
            }
            
            String priorityText = "Priorité: ";
            switch (task.getPriority()) {
                case 1:
                    priorityText += "Très basse";
                    break;
                case 2:
                    priorityText += "Basse";
                    break;
                case 3:
                    priorityText += "Moyenne";
                    break;
                case 4:
                    priorityText += "Haute";
                    break;
                case 5:
                    priorityText += "Très haute";
                    break;
                default:
                    priorityText += "Non définie";
            }
            binding.textTaskPriority.setText(priorityText);
            
            binding.textTaskCategory.setText("Catégorie: " + task.getCategory());
            
            binding.checkboxTaskCompleted.setChecked(task.isCompleted());
            
            // Configurer les listeners
            binding.getRoot().setOnClickListener(v -> listener.onTaskClick(task));
            
            binding.checkboxTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) { // Éviter les appels récursifs
                    listener.onTaskCompleteClick(task, isChecked);
                }
            });
        }
    }
}
