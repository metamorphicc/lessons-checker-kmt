package ru.is232.lessons;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class Settings {
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final SharedPreferences prefs;
    public Settings(Context context) { prefs = context.getSharedPreferences("schedule", Context.MODE_PRIVATE); }
    public ZoneId zone() { return prefs.getBoolean("deviceZone", false) ? ZoneId.systemDefault() : ZoneId.of("Asia/Novosibirsk"); }
    public boolean deviceZone() { return prefs.getBoolean("deviceZone", false); }
    public LocalDate anchor() {
        String value = prefs.getString("anchor", "");
        return value.isEmpty() ? null : LocalDate.parse(value);
    }
    public String anchorWeek() { return prefs.getString("week", null); }
    public String week(LocalDate date) { return ScheduleEngine.week(date, anchor(), anchorWeek()); }
    public List<ScheduleEngine.Lesson> lessons(LocalDate date) { return ScheduleEngine.lessons(date, anchor(), anchorWeek()); }
    public String time(int slot, boolean end) { return prefs.getString("bell" + slot + (end ? "end" : "start"), ""); }
    public List<ScheduleEngine.Bell> bells() {
        List<ScheduleEngine.Bell> result = new ArrayList<>();
        try {
            for (int i=1; i<=5; i++) result.add(new ScheduleEngine.Bell(LocalTime.parse(time(i,false)), LocalTime.parse(time(i,true))));
            return ScheduleEngine.validBells(result) ? result : Collections.emptyList();
        } catch (RuntimeException e) { return Collections.emptyList(); }
    }
    public void save(String week, boolean deviceZone, List<ScheduleEngine.Bell> bells) {
        ZoneId zone = deviceZone ? ZoneId.systemDefault() : ZoneId.of("Asia/Novosibirsk");
        SharedPreferences.Editor edit = prefs.edit().putBoolean("deviceZone", deviceZone);
        if (week == null) { edit.remove("anchor"); edit.remove("week"); }
        else { edit.putString("anchor", ScheduleEngine.monday(LocalDate.now(zone)).toString()); edit.putString("week", week); }
        for (int i=1; i<=5; i++) {
            edit.putString("bell"+i+"start", bells.isEmpty() ? "" : bells.get(i-1).start().format(TIME));
            edit.putString("bell"+i+"end", bells.isEmpty() ? "" : bells.get(i-1).end().format(TIME));
        }
        edit.apply();
    }
    public String bellLabel(int slot) {
        List<ScheduleEngine.Bell> bells = bells();
        if (bells.isEmpty()) return "Время не задано";
        ScheduleEngine.Bell bell = bells.get(slot-1);
        return bell.start().format(TIME) + "–" + bell.end().format(TIME);
    }
}
