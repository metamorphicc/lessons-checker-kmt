package ru.is232.lessons;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.Bundle;
import android.os.Build;
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
            || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action)
            || AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.equals(action)) updateAll(c);
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
            boolean distance=date.getDayOfWeek()==DayOfWeek.SATURDAY;
            ScheduleEngine.Lesson focus=moment.current()!=null ? moment.current() : moment.next();
            boolean unknownBells=!lessons.isEmpty() && settings.bells().isEmpty();
            if(unknownBells) focus=lessons.get(0);
            if(focus!=null) {
                String status=unknownBells ? "Сегодня" : (moment.current()!=null ? "Сейчас" : "Далее")+" · "+focus.slot();
                views.setTextViewText(R.id.widget_status,status);
                views.setTextViewText(R.id.widget_title,focus.shortTitle());
                views.setTextViewText(R.id.widget_time,settings.bellLabel(focus.slot()));
                views.setTextViewText(R.id.widget_room,ScheduleEngine.room(focus));
            } else {
                views.setTextViewText(R.id.widget_status,"ИС-232");
                views.setTextViewText(R.id.widget_title,distance ? "Сегодня в ДО" : lessons.isEmpty() ? "Сегодня без пар" : "На сегодня всё");
                views.setTextViewText(R.id.widget_time,"Полный список —");
                views.setTextViewText(R.id.widget_room,"по нажатию");
            }
            views.setTextViewText(R.id.widget_footer,now.format(DateTimeFormatter.ofPattern("d MMM · HH:mm",RU)));
            views.setContentDescription(R.id.widget_footer,"Обновлено "+now.format(DateTimeFormatter.ofPattern("d MMMM HH:mm",RU))+". Статичное расписание без замен.");
            views.setOnClickPendingIntent(R.id.widget_root,open);
            views.setOnClickPendingIntent(R.id.widget_refresh,refreshIntent(c));
            manager.updateAppWidget(id,views);
        }
        ZonedDateTime boundary=date.plusDays(1).atStartOfDay(settings.zone());
        for(ScheduleEngine.Lesson lesson:lessons) {
            if(settings.bells().isEmpty()) break;
            ScheduleEngine.Bell bell=settings.bells().get(lesson.slot()-1);
            for(LocalTime time: new LocalTime[]{bell.start(),bell.end()}) {
            ZonedDateTime candidate=date.atTime(time).atZone(settings.zone());
            if(candidate.isAfter(now) && candidate.isBefore(boundary)) boundary=candidate;
            }
        }
        AlarmManager alarms=c.getSystemService(AlarmManager.class);
        long trigger=boundary.toInstant().toEpochMilli();
        if(Build.VERSION.SDK_INT<31 || alarms.canScheduleExactAlarms()) {
            try { alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,refreshIntent(c)); return; }
            catch(SecurityException ignored) { /* Access can be revoked between check and call. */ }
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC,trigger,refreshIntent(c));
    }
}
