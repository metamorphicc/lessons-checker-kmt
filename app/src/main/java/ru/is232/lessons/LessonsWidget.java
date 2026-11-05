package ru.is232.lessons;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.Bundle;
import android.os.Build;
import android.widget.RemoteViews;
import android.util.SizeF;
import android.util.TypedValue;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LessonsWidget extends AppWidgetProvider {
    private static final String REFRESH="ru.is232.lessons.REFRESH";
    private static final String MANUAL="ru.is232.lessons.MANUAL_REFRESH";
    private static final Locale RU=Locale.forLanguageTag("ru");
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { updateAll(c); }
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle options) { updateAll(c); }
    @Override public void onReceive(Context c, Intent intent) {
        super.onReceive(c,intent);
        String action=intent.getAction();
        if(MANUAL.equals(action)) {LiveSyncService.requestNow(c,true);updateAll(c);}
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
    static PendingIntent manualIntent(Context c) {
        return PendingIntent.getBroadcast(c,1,new Intent(c,LessonsWidget.class).setAction(MANUAL),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void updateAll(Context c) {
        AppWidgetManager manager=AppWidgetManager.getInstance(c);
        int[] ids=manager.getAppWidgetIds(new ComponentName(c,LessonsWidget.class));
        if(ids.length==0) return;
        LiveSyncService.ensureScheduled(c); LiveSyncService.requestNow(c,false);
        Settings settings=new Settings(c);
        ZonedDateTime now=ZonedDateTime.now(settings.zone());
        LocalDate date=now.toLocalDate();
        ScheduleRepository.Day day=ScheduleRepository.day(c,date,settings);
        List<ScheduleEngine.Lesson> lessons=day.lessons();
        ScheduleEngine.Moment moment=ScheduleEngine.moment(lessons,now.toLocalTime(),settings.bells());
        for(int id:ids) {
            Bundle options=manager.getAppWidgetOptions(id);
            if(Build.VERSION.SDK_INT>=31) {
                ArrayList<SizeF> sizes=options.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
                if(sizes!=null && !sizes.isEmpty()) {
                    Map<SizeF,RemoteViews> layouts=new LinkedHashMap<>();
                    for(SizeF size:sizes) {
                        if(size.getWidth()>0 && size.getHeight()>0 && layouts.size()<16)
                            layouts.put(size,render(c,Math.round(size.getWidth()),Math.round(size.getHeight()),now,settings,day,moment));
                    }
                    if(!layouts.isEmpty()) {manager.updateAppWidget(id,new RemoteViews(layouts));continue;}
                }
            }
            int minWidth=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,130);
            int minHeight=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,130);
            int maxWidth=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,minWidth);
            int maxHeight=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,minHeight);
            RemoteViews portrait=render(c,minWidth,maxHeight,now,settings,day,moment);
            RemoteViews landscape=render(c,maxWidth,minHeight,now,settings,day,moment);
            manager.updateAppWidget(id,new RemoteViews(landscape,portrait));
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
    static RemoteViews render(Context c,int width,int height,ZonedDateTime now,Settings settings,
                              ScheduleRepository.Day day,ScheduleEngine.Moment moment) {
        width=Math.max(1,width);height=Math.max(1,height);
        boolean compact=width<210 || height<170;
        int cardHeight=compact?Math.min(width,height):height;
        boolean tiny=Math.min(width,cardHeight)<112;
        RemoteViews views=new RemoteViews(c.getPackageName(),tiny?R.layout.widget_tiny:R.layout.widget);
        if(Build.VERSION.SDK_INT>=31) {
            views.setViewLayoutWidth(R.id.widget_root,width,TypedValue.COMPLEX_UNIT_DIP);
            views.setViewLayoutHeight(R.id.widget_root,cardHeight,TypedValue.COMPLEX_UNIT_DIP);
        }
        views.setViewVisibility(R.id.widget_refresh,compact?android.view.View.GONE:android.view.View.VISIBLE);
        views.setViewVisibility(R.id.widget_footer,compact?android.view.View.GONE:android.view.View.VISIBLE);
        views.setInt(R.id.widget_title,"setMaxLines",tiny || (c.getResources().getConfiguration().fontScale>1.2f && cardHeight<160)?1:2);
        List<ScheduleEngine.Lesson> lessons=day.lessons();
        PendingIntent open=PendingIntent.getActivity(c,1,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String source="base".equals(day.source())?"База":"cache".equals(day.source())?"Кэш":"Сайт";
        boolean distance=day.remoteOnly();
        ScheduleEngine.Lesson focus=moment.current()!=null ? moment.current() : moment.next();
        boolean unknownBells=!lessons.isEmpty() && settings.bells().isEmpty();
        if(unknownBells) focus=lessons.get(0);
        if(focus!=null) {
            String status=unknownBells ? "Сегодня" : (moment.current()!=null ? "Сейчас" : "Далее")+" · "+focus.slot();
            if(compact) status=source+" · "+focus.slot()+" пара";
            if(compact && !"base".equals(day.source())) views.setInt(R.id.widget_title,"setMaxLines",1);
            views.setTextViewText(R.id.widget_status,status);
            views.setTextViewText(R.id.widget_title,focus.shortTitle());
            String time=compact && unknownBells?"Без звонков":settings.bellLabel(focus.slot());
            String room=ScheduleEngine.room(focus);
            if(compact) room=focus.room()==null?"Ауд. —":focus.room().equals("Тренажёрный зал")?"Трен. зал":focus.room();
            views.setTextViewText(R.id.widget_time,time);
            views.setTextViewText(R.id.widget_room,room);
        } else {
            views.setTextViewText(R.id.widget_status,compact?source+" · "+now.toLocalDate().format(DateTimeFormatter.ofPattern("d.MM")):"ИС-232");
            views.setTextViewText(R.id.widget_title,distance ? "Сегодня в ДО" : lessons.isEmpty() ? "Сегодня без пар" : "На сегодня всё");
            views.setTextViewText(R.id.widget_time,"Открыть расписание");
            views.setTextViewText(R.id.widget_room,"");
        }
        views.setTextViewText(R.id.widget_footer,day.widgetBadge(settings.zone(),now.toLocalDate()));
        views.setContentDescription(R.id.widget_footer,now.toLocalDate().toString()+". "+day.badge(settings.zone()));
        views.setOnClickPendingIntent(R.id.widget_root,open);
        views.setOnClickPendingIntent(R.id.widget_refresh,manualIntent(c));
        return views;
    }

}
