package com.shermine237.tempora.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shermine237.tempora.databinding.ItemSectionHeaderBinding;
import com.shermine237.tempora.databinding.ItemTaskBinding;
import com.shermine237.tempora.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;
    
    private List<Object> items; // Peut contenir des Task ou des String (pour les en-têtes)
    private final OnTaskClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskCompleteClick(Task task, boolean isCompleted);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.listener = listener;
        processTasksIntoSections(tasks);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            ItemSectionHeaderBinding binding = ItemSectionHeaderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new HeaderViewHolder(binding);
        } else {
            ItemTaskBinding binding = ItemTaskBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new TaskViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof TaskViewHolder) {
            ((TaskViewHolder) holder).bind((Task) items.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_TASK;
    }

    public void updateTasks(List<Task> newTasks) {
        processTasksIntoSections(newTasks);
        notifyDataSetChanged();
    }

    private void processTasksIntoSections(List<Task> tasks) {
        // Trier les tâches par date planifiée en priorité, puis par date d'échéance
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                // Priorité aux dates planifiées si elles existent
                Date date1 = t1.getScheduledDate() != null ? t1.getScheduledDate() : t1.getDueDate();
                Date date2 = t2.getScheduledDate() != null ? t2.getScheduledDate() : t2.getDueDate();
                
                // Gérer les cas où la date est null
                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1; // Null après non-null
                if (date2 == null) return -1; // Non-null avant null
                return date1.compareTo(date2);
            }
        });

        // Grouper les tâches par date
        Map<String, List<Task>> tasksByDate = new LinkedHashMap<>(); // Utiliser LinkedHashMap pour préserver l'ordre
        
        // Initialiser les catégories (dans l'ordre souhaité)
        tasksByDate.put("Tâches en retard", new ArrayList<>());
        tasksByDate.put("Aujourd'hui", new ArrayList<>());
        tasksByDate.put("Demain", new ArrayList<>());
        tasksByDate.put("Cette semaine", new ArrayList<>());
        tasksByDate.put("Plus tard", new ArrayList<>());
        tasksByDate.put("Planifiées", new ArrayList<>());
        tasksByDate.put("Sans date", new ArrayList<>());
        
        // Obtenir les dates de référence
        Calendar cal = Calendar.getInstance();
        
        // Aujourd'hui à minuit
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long todayMillis = todayCal.getTimeInMillis();
        
        // Demain à minuit
        Calendar tomorrowCal = (Calendar) todayCal.clone();
        tomorrowCal.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrowMillis = tomorrowCal.getTimeInMillis();
        
        // Dans une semaine à minuit
        Calendar weekCal = (Calendar) todayCal.clone();
        weekCal.add(Calendar.DAY_OF_MONTH, 7);
        long weekMillis = weekCal.getTimeInMillis();
        
        // Ajouter des logs pour le débogage
        System.out.println("Date d'aujourd'hui: " + new Date(todayMillis));
        System.out.println("Date de demain: " + new Date(tomorrowMillis));
        System.out.println("Date dans une semaine: " + new Date(weekMillis));
        
        // Répartir les tâches dans les catégories
        for (Task task : tasks) {
            // Vérifier si la tâche a une date planifiée
            if (task.getScheduledDate() != null) {
                tasksByDate.get("Planifiées").add(task);
                continue;
            }
            
            // Utiliser la date d'échéance pour les autres catégories
            Date dateToUse = task.getDueDate();
            
            if (dateToUse == null) {
                tasksByDate.get("Sans date").add(task);
                continue;
            }
            
            // Normaliser la date (enlever l'heure)
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(dateToUse);
            dateCal.set(Calendar.HOUR_OF_DAY, 0);
            dateCal.set(Calendar.MINUTE, 0);
            dateCal.set(Calendar.SECOND, 0);
            dateCal.set(Calendar.MILLISECOND, 0);
            long dateMillis = dateCal.getTimeInMillis();
            
            // Ajouter un log pour le débogage
            System.out.println("Tâche: " + task.getTitle() + ", Date: " + new Date(dateMillis));
            
            // Classer la tâche selon sa date
            if (dateMillis < todayMillis) {
                tasksByDate.get("Tâches en retard").add(task);
                System.out.println("Ajoutée à: Tâches en retard");
            } else if (isSameDay(dateMillis, todayMillis)) {
                tasksByDate.get("Aujourd'hui").add(task);
                System.out.println("Ajoutée à: Aujourd'hui");
            } else if (isSameDay(dateMillis, tomorrowMillis)) {
                tasksByDate.get("Demain").add(task);
                System.out.println("Ajoutée à: Demain");
            } else if (dateMillis < weekMillis) {
                tasksByDate.get("Cette semaine").add(task);
                System.out.println("Ajoutée à: Cette semaine");
            } else {
                tasksByDate.get("Plus tard").add(task);
                System.out.println("Ajoutée à: Plus tard");
            }
        }
        
        // Pour le débogage - afficher le nombre de tâches dans chaque catégorie
        for (Map.Entry<String, List<Task>> entry : tasksByDate.entrySet()) {
            System.out.println("Catégorie: " + entry.getKey() + ", Nombre de tâches: " + entry.getValue().size());
        }

        // Créer la liste finale avec les en-têtes et les tâches
        items = new ArrayList<>();
        for (Map.Entry<String, List<Task>> entry : tasksByDate.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                items.add(entry.getKey()); // Ajouter l'en-tête
                items.addAll(entry.getValue()); // Ajouter les tâches
            }
        }
    }

    /**
     * Vérifie si deux timestamps en millisecondes correspondent au même jour
     * @param date1 Premier timestamp en millisecondes
     * @param date2 Deuxième timestamp en millisecondes
     * @return true si les deux dates correspondent au même jour, false sinon
     */
    private boolean isSameDay(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        cal2.setTimeInMillis(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemSectionHeaderBinding binding;

        public HeaderViewHolder(ItemSectionHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String headerText) {
            binding.textSectionHeader.setText(headerText);
        }
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
            
            // Afficher la source de la tâche (manuelle ou IA)
            if (task.getScheduledDate() != null) {
                binding.textTaskSource.setText("Planifiée manuellement");
                binding.textTaskSource.setTextColor(binding.getRoot().getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                binding.textTaskSource.setText("Générée par IA");
                binding.textTaskSource.setTextColor(binding.getRoot().getContext().getResources().getColor(android.R.color.holo_blue_dark));
            }
            binding.textTaskSource.setVisibility(android.view.View.VISIBLE);
            
            binding.checkboxTaskCompleted.setChecked(task.isCompleted());
            
            // Configurer les listeners
            binding.getRoot().setOnClickListener(v -> listener.onTaskClick(task));
            
            binding.checkboxTaskCompleted.setOnClickListener(v -> {
                boolean isChecked = binding.checkboxTaskCompleted.isChecked();
                listener.onTaskCompleteClick(task, isChecked);
            });
        }
    }
}
