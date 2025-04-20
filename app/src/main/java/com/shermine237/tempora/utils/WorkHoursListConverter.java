package com.shermine237.tempora.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shermine237.tempora.model.WorkHours;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Convertisseur pour stocker une liste d'heures de travail dans la base de données Room.
 * Cette classe convertit une liste d'objets WorkHours en chaîne JSON et vice-versa.
 */
public class WorkHoursListConverter {
    
    private static final Gson gson = new Gson();
    
    /**
     * Convertit une liste d'heures de travail en chaîne JSON
     * @param workHoursList Liste d'heures de travail
     * @return Chaîne JSON
     */
    @TypeConverter
    public static String fromWorkHoursList(List<WorkHours> workHoursList) {
        if (workHoursList == null) {
            return null;
        }
        return gson.toJson(workHoursList);
    }
    
    /**
     * Convertit une chaîne JSON en liste d'heures de travail
     * @param workHoursJson Chaîne JSON
     * @return Liste d'heures de travail
     */
    @TypeConverter
    public static List<WorkHours> toWorkHoursList(String workHoursJson) {
        if (workHoursJson == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<WorkHours>>() {}.getType();
        return gson.fromJson(workHoursJson, listType);
    }
}
