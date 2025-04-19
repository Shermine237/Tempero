package com.shermine237.tempora.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour convertir les listes de chaînes de caractères en JSON et vice-versa
 * pour le stockage dans la base de données Room.
 */
public class StringListConverter {
    
    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    
    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) {
            return null;
        }
        
        Gson gson = new Gson();
        return gson.toJson(list);
    }
    
    @TypeConverter
    public static List<Integer> integerListFromString(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    
    @TypeConverter
    public static String fromIntegerList(List<Integer> list) {
        if (list == null) {
            return null;
        }
        
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
