package ru.is232.lessons;

import android.content.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public final class ScheduleRepository {
    public static final String ENDPOINT="https://script.google.com/macros/s/AKfycbwW8yTxVGMvsklnIscEP87grbdxHkWv9-Dvztm00rjhM2xeNFqL-HRtnDZuldpfRVtI/exec";
    public static final long REFRESH_MS=15*60_000L;
    private static final ExecutorService NETWORK=Executors.newSingleThreadExecutor();
    private static volatile boolean refreshing;
    public record Day(List<ScheduleEngine.Lesson> lessons, boolean remoteOnly, String source, long fetchedAt, boolean failed) {
        public String badge(ZoneId zone) {
            if("base".equals(source)) return "Базовое · нет данных сайта на эту дату";
            String time=Instant.ofEpochMilli(fetchedAt).atZone(zone).format(DateTimeFormatter.ofPattern("d.MM HH:mm"));
            return ("cache".equals(source) ? "Кэш сайта · " : "Сайт · ")+time+(failed ? " · ошибка обновления" : "");
        }
        public String widgetBadge(ZoneId zone,LocalDate date) {
            if("base".equals(source)) return date.format(DateTimeFormatter.ofPattern("d.MM"))+" · база";
            return ("cache".equals(source) ? "Кэш " : "Сайт ")+Instant.ofEpochMilli(fetchedAt).atZone(zone).format(DateTimeFormatter.ofPattern("d.MM HH:mm"));
        }
    }
    private static SharedPreferences prefs(Context c) {return c.getSharedPreferences("live_schedule",Context.MODE_PRIVATE);}
    public static Day day(Context c,LocalDate date,Settings settings) {
        SharedPreferences p=prefs(c); long fetched=p.getLong("success",0); boolean failed=p.getBoolean("failed",false);
        try {
            for(LiveSchedule.Day day:LiveSchedule.parse(p.getString("json","[]"))) if(day.date().equals(date)) {
                long age=System.currentTimeMillis()-fetched;
                return new Day(day.campus(),day.remoteOnly(),failed || age<0 || age>2*REFRESH_MS ? "cache" : "site",fetched,failed);
            }
        } catch(Exception ignored) { /* An invalid cache falls back to the bundled dataset. */ }
        return new Day(settings.lessons(date),date.getDayOfWeek()==DayOfWeek.SATURDAY,"base",0,failed);
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
                String clean=LiveSchedule.sanitize(download());
                prefs(app).edit().putString("json",clean).putLong("success",System.currentTimeMillis()).putBoolean("failed",false).commit();
            } catch(Exception e) {prefs(app).edit().putBoolean("failed",true).commit();}
            finally {refreshing=false; if(complete!=null) complete.run();}
        });
    }
    static String download() throws IOException {
        URL url=new URL(ENDPOINT);
        for(int redirects=0;redirects<6;redirects++) {
            if(!"https".equals(url.getProtocol()) || !("script.google.com".equals(url.getHost()) || "script.googleusercontent.com".equals(url.getHost()))) throw new IOException("Unexpected endpoint");
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(10_000); connection.setReadTimeout(10_000); connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false); connection.setRequestProperty("Accept","application/json");
            try {
                int code=connection.getResponseCode();
                if(code>=300 && code<400) {
                    String location=connection.getHeaderField("Location");
                    if(location==null) throw new IOException("Redirect without location");
                    url=new URL(url,location); continue;
                }
                if(code!=200) throw new IOException("HTTP "+code);
                try(InputStream in=connection.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                    byte[] buffer=new byte[4096]; int count,total=0;
                    while((count=in.read(buffer))!=-1) {total+=count;if(total>1_000_000) throw new IOException("Response too large");out.write(buffer,0,count);}
                    return new String(out.toByteArray(),StandardCharsets.UTF_8);
                }
            } finally {connection.disconnect();}
        }
        throw new IOException("Too many redirects");
    }
    private ScheduleRepository() {}
}
