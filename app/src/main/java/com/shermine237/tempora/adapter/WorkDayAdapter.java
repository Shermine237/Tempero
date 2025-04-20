package com.shermine237.tempora.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shermine237.tempora.R;
import com.shermine237.tempora.model.WorkHours;

import java.util.List;

/**
 * Adaptateur pour afficher la liste des jours de travail avec leurs heures.
 */
public class WorkDayAdapter extends RecyclerView.Adapter<WorkDayAdapter.WorkDayViewHolder> {
    
    private final List<WorkHours> workHoursList;
    private final Context context;
    private final OnWorkDayChangedListener listener;
    private final String[] hours = new String[24];
    
    /**
     * Interface pour écouter les changements dans les jours de travail
     */
    public interface OnWorkDayChangedListener {
        void onWorkDayChanged(WorkHours workHours);
    }
    
    /**
     * Constructeur
     * @param context Contexte
     * @param workHoursList Liste des heures de travail par jour
     * @param listener Écouteur pour les changements
     */
    public WorkDayAdapter(Context context, List<WorkHours> workHoursList, OnWorkDayChangedListener listener) {
        this.context = context;
        this.workHoursList = workHoursList;
        this.listener = listener;
        
        // Initialiser le tableau des heures
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
        }
    }
    
    @NonNull
    @Override
    public WorkDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_work_day, parent, false);
        return new WorkDayViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WorkDayViewHolder holder, int position) {
        WorkHours workHours = workHoursList.get(position);
        
        // Configurer la checkbox du jour
        holder.checkBoxWorkDay.setText(workHours.getDayName());
        holder.checkBoxWorkDay.setChecked(workHours.isWorkDay());
        
        // Configurer les spinners d'heures
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        holder.spinnerStartHour.setAdapter(hourAdapter);
        holder.spinnerEndHour.setAdapter(hourAdapter);
        
        holder.spinnerStartHour.setSelection(workHours.getStartHour());
        holder.spinnerEndHour.setSelection(workHours.getEndHour());
        
        // Configurer l'état d'expansion
        holder.layoutDetails.setVisibility(workHours.isWorkDay() ? View.VISIBLE : View.GONE);
        holder.buttonExpand.setRotation(workHours.isWorkDay() ? 180 : 0);
        
        // Configurer les écouteurs
        holder.checkBoxWorkDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            workHours.setWorkDay(isChecked);
            holder.layoutDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            holder.buttonExpand.setRotation(isChecked ? 180 : 0);
            listener.onWorkDayChanged(workHours);
        });
        
        holder.buttonExpand.setOnClickListener(v -> {
            if (workHours.isWorkDay()) {
                boolean isVisible = holder.layoutDetails.getVisibility() == View.VISIBLE;
                holder.layoutDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                holder.buttonExpand.setRotation(isVisible ? 0 : 180);
            }
        });
        
        holder.spinnerStartHour.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                workHours.setStartHour(position);
                listener.onWorkDayChanged(workHours);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Ne rien faire
            }
        });
        
        holder.spinnerEndHour.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                workHours.setEndHour(position);
                listener.onWorkDayChanged(workHours);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Ne rien faire
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return workHoursList.size();
    }
    
    /**
     * ViewHolder pour les jours de travail
     */
    static class WorkDayViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxWorkDay;
        ImageButton buttonExpand;
        LinearLayout layoutDetails;
        Spinner spinnerStartHour;
        Spinner spinnerEndHour;
        
        public WorkDayViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxWorkDay = itemView.findViewById(R.id.checkbox_work_day);
            buttonExpand = itemView.findViewById(R.id.button_expand);
            layoutDetails = itemView.findViewById(R.id.layout_details);
            spinnerStartHour = itemView.findViewById(R.id.spinner_start_hour);
            spinnerEndHour = itemView.findViewById(R.id.spinner_end_hour);
        }
    }
}
