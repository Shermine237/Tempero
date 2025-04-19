package com.shermine237.tempora.utils;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Classe utilitaire pour convertir les dates entre le format Date et le format Long
 * pour le stockage dans la base de données Room.
 */
public class DateConverter {
    
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
