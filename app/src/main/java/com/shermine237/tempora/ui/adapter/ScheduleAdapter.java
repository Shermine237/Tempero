package com.shermine237.tempora.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shermine237.tempora.databinding.ItemScheduleBinding;
import com.shermine237.tempora.model.ScheduleItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleItem> scheduleItems;
    private final OnScheduleItemClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnScheduleItemClickListener {
        void onScheduleItemClick(ScheduleItem item);
        void onScheduleItemCompleteClick(ScheduleItem item, boolean isCompleted);
    }

    public ScheduleAdapter(List<ScheduleItem> scheduleItems, OnScheduleItemClickListener listener) {
        this.scheduleItems = scheduleItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleBinding binding = ItemScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem item = scheduleItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return scheduleItems.size();
    }

    public void updateScheduleItems(List<ScheduleItem> newItems) {
        this.scheduleItems = newItems;
        notifyDataSetChanged();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final ItemScheduleBinding binding;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public ScheduleViewHolder(ItemScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScheduleItem item, OnScheduleItemClickListener listener) {
            String startTime = timeFormat.format(item.getStartTime());
            String endTime = timeFormat.format(item.getEndTime());
            binding.textScheduleTime.setText(startTime + " - " + endTime);
            
            binding.textScheduleTitle.setText(item.getTitle());
            
            int durationMinutes = item.getDurationMinutes();
            binding.textScheduleDuration.setText(durationMinutes + " min");
            
            String typeText = "Type: ";
            switch (item.getType()) {
                case "task":
                    typeText += "Tâche";
                    break;
                case "break":
                    typeText += "Pause";
                    break;
                case "meal":
                    typeText += "Repas";
                    break;
                default:
                    typeText += item.getType();
            }
            binding.textScheduleType.setText(typeText);
            
            binding.checkboxScheduleCompleted.setChecked(item.isCompleted());
            
            // Masquer la case à cocher pour les pauses et les repas
            if (!item.getType().equals("task")) {
                binding.checkboxScheduleCompleted.setVisibility(android.view.View.INVISIBLE);
            } else {
                binding.checkboxScheduleCompleted.setVisibility(android.view.View.VISIBLE);
            }
            
            // Configurer les listeners
            binding.getRoot().setOnClickListener(v -> listener.onScheduleItemClick(item));
            
            binding.checkboxScheduleCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) { // Éviter les appels récursifs
                    listener.onScheduleItemCompleteClick(item, isChecked);
                }
            });
        }
    }
}
