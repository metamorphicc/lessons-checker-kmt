package ru.is232.lessons;

import android.content.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public final class ScheduleRepository {
    public static final long REFRESH_MS=15*60_000L;
    private static final ExecutorService NETWORK=Executors.newSingleThreadExecutor();
    private static volatile boolean refreshing;
    public record Day(List<ScheduleEngine.Lesson> lessons, boolean remoteOnly, String source, long fetchedAt, boolean failed) {
        public String badge(ZoneId zone) {
            if("base".equals(source)) return failed ? "Базовое · сайт не удалось обновить" : fetchedAt==0 ? "Базовое · сайт ещё не проверен" : "Базовое · дата не опубликована на сайте";
            String time=Instant.ofEpochMilli(fetchedAt).atZone(zone).format(DateTimeFormatter.ofPattern("d.MM HH:mm"));
            return ("cache".equals(source) ? "Кэш сайта · " : "Сайт · ")+time+(failed ? " · ошибка обновления" : "");
        }
        public String widgetBadge(ZoneId zone,LocalDate date) {
            if("base".equals(source)) return date.format(DateTimeFormatter.ofPattern("d.MM"))+" · база"+(failed ? " !" : "");
            return ("cache".equals(source) ? "Кэш " : "Сайт ")+Instant.ofEpochMilli(fetchedAt).atZone(zone).format(DateTimeFormatter.ofPattern("d.MM HH:mm"));
        }
    }
    private static SharedPreferences prefs(Context c) {return c.getSharedPreferences("live_schedule",Context.MODE_PRIVATE);}
    public static Day day(Context c,LocalDate date,Settings settings) {
        SharedPreferences p=prefs(c); long fetched=p.getLong("success",0); boolean failed=p.getBoolean("failed",false);
        return resolve(date,settings.lessons(date),date.getDayOfWeek()==DayOfWeek.SATURDAY,p.getString("json","[]"),fetched,failed,System.currentTimeMillis());
    }
    static Day resolve(LocalDate date,List<ScheduleEngine.Lesson> base,boolean baseRemote,String json,long fetched,boolean failed,long now) {
        try {
            for(LiveSchedule.Day day:LiveSchedule.parse(json)) if(day.date().equals(date)) {
                long age=now-fetched;
                return new Day(day.campus(),day.remoteOnly(),failed || age<0 || age>2*REFRESH_MS ? "cache" : "site",fetched,failed);
            }
        } catch(Exception ignored) {failed=true;}
        return new Day(base,baseRemote,"base",fetched,failed);
    }
    public static boolean isRefreshing() {return refreshing;}
    public static boolean due(Context c,boolean force) {
        SharedPreferences p=prefs(c); long now=System.currentTimeMillis(),attempt=p.getLong("attempt",0),success=p.getLong("success",0);
        if(refreshing) return false;
        if(now>=attempt && now-attempt<(force ? 10_000L : 5*60_000L)) return false;
        return force || now<success || now-success>=REFRESH_MS;
    }
    public static synchronized void refreshAsync(Context c,boolean force,Runnable complete) {
        Context app=c.getApplicationContext();
        if(!due(app,force)) {if(complete!=null) complete.run(); return;}
        refreshing=true; prefs(app).edit().putLong("attempt",System.currentTimeMillis()).apply();
        NETWORK.execute(()->{
            try {
                String clean=LiveSchedule.sanitize(LiveScheduleClient.download());
                prefs(app).edit().putString("json",clean).putLong("success",System.currentTimeMillis()).putBoolean("failed",false).commit();
            } catch(Exception e) {prefs(app).edit().putBoolean("failed",true).commit();}
            finally {refreshing=false; if(complete!=null) complete.run();}
        });
    }
    private ScheduleRepository() {}
}
