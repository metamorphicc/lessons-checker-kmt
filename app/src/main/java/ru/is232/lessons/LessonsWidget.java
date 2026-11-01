package ru.is232.lessons;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LessonsWidget extends AppWidgetProvider {
    private static final String REFRESH="ru.is232.lessons.REFRESH";
    private static final Locale RU=Locale.forLanguageTag("ru");
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { updateAll(c); }
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle options) { updateAll(c); }
    @Override public void onReceive(Context c, Intent intent) {
        super.onReceive(c,intent);
        String action=intent.getAction();
        if (REFRESH.equals(action) || Intent.ACTION_BOOT_COMPLETED.equals(action)
            || Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
            || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action)) updateAll(c);
    }
    @Override public void onDisabled(Context c) {
        c.getSystemService(AlarmManager.class).cancel(refreshIntent(c));
    }
    static PendingIntent refreshIntent(Context c) {
        return PendingIntent.getBroadcast(c,0,new Intent(c,LessonsWidget.class).setAction(REFRESH),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void updateAll(Context c) {
        AppWidgetManager manager=AppWidgetManager.getInstance(c);
        int[] ids=manager.getAppWidgetIds(new ComponentName(c,LessonsWidget.class));
        if(ids.length==0) return;
        Settings settings=new Settings(c);
        ZonedDateTime now=ZonedDateTime.now(settings.zone());
        LocalDate date=now.toLocalDate();
        List<ScheduleEngine.Lesson> lessons=settings.lessons(date);
        ScheduleEngine.Moment moment=ScheduleEngine.moment(lessons,now.toLocalTime(),settings.bells());
        PendingIntent open=PendingIntent.getActivity(c,1,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        for(int id:ids) {
            RemoteViews views=new RemoteViews(c.getPackageName(),R.layout.widget);
            views.setTextViewText(R.id.widget_date,"ИС-232 · "+date.format(DateTimeFormatter.ofPattern("EEE, d MMM",RU)));
            boolean distance=date.getDayOfWeek()==DayOfWeek.SATURDAY;
            views.setTextViewText(R.id.widget_status,distance ? "Очных пар нет · ДО" : moment.label());
            views.setTextViewText(R.id.widget_week,"Корпус 1 · Очные пары");
            views.removeAllViews(R.id.widget_lessons);
            for(ScheduleEngine.Lesson lesson:lessons) {
                RemoteViews row=new RemoteViews(c.getPackageName(),R.layout.widget_row);
                String variant=lesson.week()!=null && settings.week(date)==null ? " ("+ScheduleEngine.weekLabel(lesson.week()).toLowerCase(RU)+")" : "";
                boolean current=moment.current()!=null && moment.current().slot()==lesson.slot();
                row.setTextViewText(R.id.row_title,(current?"Сейчас → ":"")+lesson.slot()+". "+lesson.shortTitle()+variant);
                row.setTextViewText(R.id.row_meta,settings.bellLabel(lesson.slot())+" · "+ScheduleEngine.room(lesson));
                views.addView(R.id.widget_lessons,row);
            }
            if(lessons.isEmpty()) {
                RemoteViews row=new RemoteViews(c.getPackageName(),R.layout.widget_row);
                row.setTextViewText(R.id.row_title,distance ? "Субботние занятия — в ДО" : "Сегодня без пар");
                row.setTextViewText(R.id.row_meta,distance ? "На очные пары ехать не нужно." : "Отдыхай. Замены проверяй отдельно."); views.addView(R.id.widget_lessons,row);
            }
            views.setTextViewText(R.id.widget_footer,"Данные на "+now.format(DateTimeFormatter.ofPattern("d.MM HH:mm"))+" · PDF без замен\nНажми ↻, если время обновления устарело");
            views.setOnClickPendingIntent(R.id.widget_root,open);
            views.setOnClickPendingIntent(R.id.widget_refresh,refreshIntent(c));
            manager.updateAppWidget(id,views);
        }
        // Inexact alarm: no special permission or exact-alarm access required.
        ZonedDateTime boundary=date.plusDays(1).atStartOfDay(settings.zone());
        for(ScheduleEngine.Bell bell:settings.bells()) for(LocalTime time: new LocalTime[]{bell.start(),bell.end()}) {
            ZonedDateTime candidate=date.atTime(time).atZone(settings.zone());
            if(candidate.isAfter(now) && candidate.isBefore(boundary)) boundary=candidate;
        }
        c.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.RTC,boundary.toInstant().toEpochMilli(),refreshIntent(c));
    }
}
