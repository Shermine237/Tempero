package com.shermine237.tempora.ui.decorator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

/**
 * Décorateur pour mettre en évidence la date du jour
 */
public class TodayDecorator implements DayViewDecorator {

    private final CalendarDay today;
    private final Drawable highlightDrawable;
    private final int textColor;

    /**
     * Constructeur pour le décorateur
     * @param context Contexte de l'application
     */
    public TodayDecorator(Context context) {
        // Obtenir la date du jour
        Calendar calendar = Calendar.getInstance();
        today = CalendarDay.from(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, // MaterialCalendarView utilise 1-12 pour les mois
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Créer un drawable pour le fond avec une couleur plus vive
        int highlightColor = Color.argb(150, 255, 0, 0); // Rouge semi-transparent
        this.highlightDrawable = new ColorDrawable(highlightColor);
        
        // Définir la couleur du texte en blanc pour un meilleur contraste
        this.textColor = Color.WHITE;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(today);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
        view.addSpan(new ForegroundColorSpan(textColor));
    }
}
