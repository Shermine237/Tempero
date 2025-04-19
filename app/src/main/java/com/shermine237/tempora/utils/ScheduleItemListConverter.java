package com.shermine237.tempora.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shermine237.tempora.model.ScheduleItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour convertir les listes d'éléments de planning en JSON et vice-versa
 * pour le stockage dans la base de données Room.
 */
public class ScheduleItemListConverter {
    
    @TypeConverter
    public static List<ScheduleItem> fromString(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<ScheduleItem>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    
    @TypeConverter
    public static String fromList(List<ScheduleItem> list) {
        if (list == null) {
            return null;
        }
        
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
