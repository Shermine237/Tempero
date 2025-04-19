package com.shermine237.tempora.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shermine237.tempora.databinding.ItemScheduleDetailBinding;
import com.shermine237.tempora.model.ScheduleItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScheduleDetailAdapter extends RecyclerView.Adapter<ScheduleDetailAdapter.ScheduleDetailViewHolder> {

    private List<ScheduleItem> scheduleItems;
    private final OnScheduleItemClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnScheduleItemClickListener {
        void onScheduleItemClick(ScheduleItem item);
        void onScheduleItemCompleteClick(ScheduleItem item, boolean isCompleted);
    }

    public ScheduleDetailAdapter(List<ScheduleItem> scheduleItems, OnScheduleItemClickListener listener) {
        this.scheduleItems = scheduleItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleDetailBinding binding = ItemScheduleDetailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ScheduleDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleDetailViewHolder holder, int position) {
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

    static class ScheduleDetailViewHolder extends RecyclerView.ViewHolder {
        private final ItemScheduleDetailBinding binding;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public ScheduleDetailViewHolder(ItemScheduleDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScheduleItem item, OnScheduleItemClickListener listener) {
            String startTime = timeFormat.format(item.getStartTime());
            String endTime = timeFormat.format(item.getEndTime());
            binding.textScheduleTime.setText(startTime + " - " + endTime);
            
            binding.textScheduleTitle.setText(item.getTitle());
            
            binding.textScheduleDuration.setText("Durée: " + item.getDurationMinutes() + " min");
            
            String typeText = "Type: ";
            int indicatorColor;
            
            switch (item.getType()) {
                case "task":
                    typeText += "Tâche";
                    indicatorColor = Color.parseColor("#4CAF50"); // Vert
                    break;
                case "break":
                    typeText += "Pause";
                    indicatorColor = Color.parseColor("#2196F3"); // Bleu
                    break;
                case "meal":
                    typeText += "Repas";
                    indicatorColor = Color.parseColor("#FF9800"); // Orange
                    break;
                default:
                    typeText += item.getType();
                    indicatorColor = Color.parseColor("#9E9E9E"); // Gris
            }
            binding.textScheduleType.setText(typeText);
            binding.viewTypeIndicator.setBackgroundColor(indicatorColor);
            
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
