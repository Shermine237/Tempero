package com.shermine237.tempora.ui.decorator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

/**
 * Décorateur pour mettre en évidence les jours avec des plannings
 */
public class ScheduleDateDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final Drawable highlightDrawable;
    private final int textColor;

    /**
     * Constructeur pour le décorateur
     * @param context Contexte de l'application
     * @param dates Collection de dates à décorer
     */
    public ScheduleDateDecorator(Context context, Collection<CalendarDay> dates) {
        this.dates = new HashSet<>(dates);
        
        // Créer un drawable pour le fond avec une couleur plus claire
        int colorPrimary = context.getResources().getColor(android.R.color.holo_blue_light);
        
        // Créer un drawable avec une couleur plus opaque pour un meilleur contraste
        int highlightColor = Color.argb(150, Color.red(colorPrimary), 
                                        Color.green(colorPrimary), 
                                        Color.blue(colorPrimary));
        this.highlightDrawable = new ColorDrawable(highlightColor);
        
        // Définir la couleur du texte en blanc pour un meilleur contraste
        this.textColor = Color.WHITE;
    }

    /**
     * Mettre à jour les dates à décorer
     * @param dates Nouvelles dates à décorer
     */
    public void setDates(Collection<CalendarDay> dates) {
        this.dates.clear();
        this.dates.addAll(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
        view.addSpan(new ForegroundColorSpan(textColor));
    }
}
