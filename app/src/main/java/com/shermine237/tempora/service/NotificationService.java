package com.shermine237.tempora.service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.shermine237.tempora.R;
import com.shermine237.tempora.model.Schedule;
import com.shermine237.tempora.model.ScheduleItem;
import com.shermine237.tempora.model.Task;
import com.shermine237.tempora.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Service de notification intelligent pour l'application Tempero.
 * Cette classe gère les notifications pour les tâches, les plannings et les recommandations.
 */
public class NotificationService {
    
    private static final String CHANNEL_ID_TASKS = "tempero_tasks";
    private static final String CHANNEL_ID_SCHEDULE = "tempero_schedule";
    private static final String CHANNEL_ID_RECOMMENDATIONS = "tempero_recommendations";
    private static final String CHANNEL_ID_TIPS = "tempero_tips";
    
    private static final int NOTIFICATION_ID_TASK_REMINDER = 1000;
    private static final int NOTIFICATION_ID_SCHEDULE_REMINDER = 2000;
    private static final int NOTIFICATION_ID_PRODUCTIVITY_TIP = 3000;
    private static final int NOTIFICATION_ID_TIP = 4000;
    
    private final Context context;
    private final NotificationManagerCompat notificationManager;
    
    /**
     * Constructeur
     * @param context Contexte de l'application
     */
    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        
        // Créer les canaux de notification (requis pour Android 8.0+)
        createNotificationChannels();
    }
    
    /**
     * Crée les canaux de notification
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal pour les rappels de tâches
            NotificationChannel tasksChannel = new NotificationChannel(
                    CHANNEL_ID_TASKS,
                    "Rappels de tâches",
                    NotificationManager.IMPORTANCE_HIGH
            );
            tasksChannel.setDescription("Notifications pour les tâches à venir et en retard");
            
            // Canal pour les rappels de planning
            NotificationChannel scheduleChannel = new NotificationChannel(
                    CHANNEL_ID_SCHEDULE,
                    "Rappels de planning",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            scheduleChannel.setDescription("Notifications pour le planning quotidien");
            
            // Canal pour les recommandations
            NotificationChannel recommendationsChannel = new NotificationChannel(
                    CHANNEL_ID_RECOMMENDATIONS,
                    "Conseils de productivité",
                    NotificationManager.IMPORTANCE_LOW
            );
            recommendationsChannel.setDescription("Conseils et astuces pour améliorer votre productivité");
            
            // Canal pour les conseils
            NotificationChannel tipsChannel = new NotificationChannel(
                    CHANNEL_ID_TIPS,
                    "Conseils",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            tipsChannel.setDescription("Conseils pour améliorer votre productivité");
            
            // Enregistrer les canaux
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(tasksChannel);
            manager.createNotificationChannel(scheduleChannel);
            manager.createNotificationChannel(recommendationsChannel);
            manager.createNotificationChannel(tipsChannel);
        }
    }
    
    /**
     * Planifie une notification pour une tâche à venir
     * @param task Tâche pour laquelle planifier la notification
     * @param minutesBeforeTask Minutes avant la tâche pour envoyer la notification
     */
    public void scheduleTaskReminder(Task task, int minutesBeforeTask) {
        if (task.getDueDate() == null) {
            return; // Impossible de planifier sans date d'échéance
        }
        
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, task.getId(), intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Calculer le temps de notification
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getDueDate());
        calendar.add(Calendar.MINUTE, -minutesBeforeTask);
        
        // Vérifier si le temps de notification est dans le futur
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            return; // Ne pas planifier de notification dans le passé
        }
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Rappel de tâche")
                .setContentText("N'oubliez pas : " + task.getTitle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Planifier la notification
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
    
    /**
     * Envoie une notification pour une tâche en retard
     * @param task Tâche en retard
     */
    public void notifyOverdueTask(Task task) {
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, task.getId() + 1000, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Tâche en retard")
                .setContentText("La tâche \"" + task.getTitle() + "\" est en retard")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Envoyer la notification
        notificationManager.notify(NOTIFICATION_ID_TASK_REMINDER + task.getId(), builder.build());
    }
    
    /**
     * Envoie une notification pour le planning quotidien
     * @param schedule Planning du jour
     */
    public void notifyDailySchedule(Schedule schedule) {
        if (schedule == null || schedule.getItems() == null || schedule.getItems().isEmpty()) {
            return;
        }
        
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("date", schedule.getDate().getTime());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID_SCHEDULE_REMINDER, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Formater la date pour l'affichage
        String dateStr;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.setTime(schedule.getDate());
        scheduleCalendar.set(Calendar.HOUR_OF_DAY, 0);
        scheduleCalendar.set(Calendar.MINUTE, 0);
        scheduleCalendar.set(Calendar.SECOND, 0);
        scheduleCalendar.set(Calendar.MILLISECOND, 0);
        
        if (scheduleCalendar.getTimeInMillis() == today.getTimeInMillis()) {
            dateStr = "aujourd'hui";
        } else if (scheduleCalendar.getTimeInMillis() == today.getTimeInMillis() + 24*60*60*1000) {
            dateStr = "demain";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM", Locale.FRENCH);
            dateStr = "le " + sdf.format(schedule.getDate());
        }
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_SCHEDULE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Votre planning pour " + dateStr + " est prêt")
                .setContentText("Appuyez pour voir votre planning")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Ajouter un style inbox pour montrer les éléments du planning
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle("Votre planning pour " + dateStr);
        
        // Ajouter les 5 premières tâches du planning
        List<ScheduleItem> items = schedule.getItems();
        int count = Math.min(items.size(), 5);
        for (int i = 0; i < count; i++) {
            ScheduleItem item = items.get(i);
            String timeStr = formatTime(item.getStartTime()) + " - " + formatTime(item.getEndTime());
            inboxStyle.addLine(timeStr + " : " + item.getTitle());
        }
        
        // Ajouter le style à la notification
        builder.setStyle(inboxStyle);
        
        // Envoyer la notification
        notificationManager.notify(NOTIFICATION_ID_SCHEDULE_REMINDER, builder.build());
    }
    
    /**
     * Envoie une notification avec un conseil de productivité
     * @param tip Conseil de productivité
     */
    public void notifyProductivityTip(String tip) {
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID_PRODUCTIVITY_TIP, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_RECOMMENDATIONS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Conseil de productivité")
                .setContentText(tip)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Envoyer la notification
        notificationManager.notify(NOTIFICATION_ID_PRODUCTIVITY_TIP, builder.build());
    }
    
    /**
     * Envoie une notification pour une alerte de surcharge
     * @param message Message d'alerte
     */
    public void notifyOverloadAlert(String message) {
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID_PRODUCTIVITY_TIP + 1, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_RECOMMENDATIONS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alerte de surcharge")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Envoyer la notification
        notificationManager.notify(NOTIFICATION_ID_PRODUCTIVITY_TIP + 1, builder.build());
    }
    
    /**
     * Programme des notifications pour toutes les tâches d'un planning approuvé
     * @param schedule Planning approuvé
     */
    public void scheduleNotificationsForApprovedSchedule(Schedule schedule) {
        if (schedule == null || !schedule.isApproved() || schedule.getItems() == null) {
            return;
        }
        
        for (ScheduleItem item : schedule.getItems()) {
            if ("task".equals(item.getType())) {
                scheduleNotificationForItem(schedule.getDate(), item);
            }
        }
        
        // Envoyer une notification immédiate pour informer que le planning a été approuvé
        notifyScheduleApproved(schedule);
    }
    
    /**
     * Programme une notification pour un élément de planning
     * @param scheduleDate Date du planning
     * @param item Élément du planning
     */
    private void scheduleNotificationForItem(Date scheduleDate, ScheduleItem item) {
        if (item == null || scheduleDate == null || item.getStartTime() == null) {
            return;
        }
        
        // Calculer la date et l'heure de la notification (15 minutes avant le début)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(scheduleDate);
        
        // Utiliser directement l'heure de début (Date)
        Calendar startTimeCal = Calendar.getInstance();
        startTimeCal.setTime(item.getStartTime());
        
        int hours = startTimeCal.get(Calendar.HOUR_OF_DAY);
        int minutes = startTimeCal.get(Calendar.MINUTE);
        
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.add(Calendar.MINUTE, -15); // 15 minutes avant
        
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Générer un ID unique pour cette notification basé sur l'élément du planning
        int notificationId = (item.getTitle().hashCode() + item.getStartTime().hashCode()) % 10000;
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Tâche à venir")
                .setContentText(item.getTitle() + " commence à " + formatTime(item.getStartTime()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Programmer la notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }
    
    /**
     * Envoie une notification pour informer que le planning a été approuvé
     * @param schedule Planning approuvé
     */
    private void notifyScheduleApproved(Schedule schedule) {
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID_SCHEDULE_REMINDER, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_SCHEDULE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Planning approuvé")
                .setContentText("Votre planning pour " + formatDate(schedule.getDate()) + " a été approuvé")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Envoyer la notification
        notificationManager.notify(NOTIFICATION_ID_SCHEDULE_REMINDER, builder.build());
    }
    
    /**
     * Formate une date en chaîne de caractères lisible
     * @param date Date à formater
     * @return Date formatée
     */
    private String formatDate(Date date) {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("EEEE dd MMMM", java.util.Locale.FRENCH);
        return dateFormat.format(date);
    }
    
    /**
     * Formate une date en chaîne d'heure (HH:mm)
     * @param date Date à formater
     * @return Chaîne d'heure formatée
     */
    private String formatTime(Date date) {
        if (date == null) {
            return "00:00";
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        
        return String.format("%02d:%02d", hours, minutes);
    }
    
    /**
     * Envoie une notification avec un conseil de productivité
     * @param title Titre de la notification
     * @param message Message de la notification
     */
    public void sendProductivityTipNotification(String title, String message) {
        // Créer l'intent pour la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_TIPS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Afficher la notification
        notificationManager.notify(NOTIFICATION_ID_TIP, builder.build());
    }
}
