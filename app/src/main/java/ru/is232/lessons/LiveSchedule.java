package ru.is232.lessons;

import org.json.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/** Validates the public feed and retains only explicitly dated IS-232 rows. */
public final class LiveSchedule {
    private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    public record Day(LocalDate date, List<ScheduleEngine.Lesson> campus, boolean remoteOnly) {}
    private static String string(JSONObject row,String key) throws JSONException {
        Object value=row.opt(key);
        if(value==null || value==JSONObject.NULL) return "";
        if(!(value instanceof String)) throw new JSONException("Expected string: "+key);
        return ((String)value).replace('\u00a0',' ').trim();
    }
    public static List<Day> parse(String json) throws JSONException {
        JSONArray days=new JSONArray(json);
        List<Day> result=new ArrayList<>(); Set<LocalDate> seen=new HashSet<>();
        for(int i=0;i<days.length();i++) {
            JSONObject day=days.getJSONObject(i);
            String rawDate=string(day,"дата");
            if(rawDate.isEmpty()) continue;
            LocalDate date;
            try {date=LocalDate.parse(rawDate,DATE);} catch(DateTimeException e) {throw new JSONException("Invalid schedule date");}
            JSONArray rows=day.getJSONArray("данные"); JSONObject match=null;
            for(int r=0;r<rows.length();r++) {
                JSONObject row=rows.getJSONObject(r);
                if("ИС-232".equalsIgnoreCase(string(row,"Группа"))) {
                    if(match!=null) throw new JSONException("Duplicate group row");
                    match=row;
                }
            }
            if(match==null) continue;
            if(!seen.add(date)) throw new JSONException("Duplicate dated schedule");
            List<ScheduleEngine.Lesson> campus=new ArrayList<>(); int remote=0;
            for(int slot=1;slot<=5;slot++) {
                String key=slot+" пара";
                // Missing columns must not turn a broken response into a cancelled day.
                if(!match.has(key) || !match.has("ауд"+slot)) throw new JSONException("Missing class columns");
                String teacher=string(match,key),room=string(match,"ауд"+slot);
                if(teacher.isEmpty() || "Нет занятия".equalsIgnoreCase(teacher)) continue;
                if(room.toUpperCase(Locale.ROOT).matches("ДО(?:\\s.*)?")) {remote++; continue;}
                if(room.isEmpty() || "Нет".equalsIgnoreCase(room)) room=null;
                campus.add(new ScheduleEngine.Lesson(date.getDayOfWeek().getValue(),slot,teacher,teacher,"",room,null,"campus"));
            }
            result.add(new Day(date,Collections.unmodifiableList(campus),remote>0 && campus.isEmpty()));
        }
        return Collections.unmodifiableList(result);
    }
    /** Do not persist unrelated groups or undated rows. */
    public static String sanitize(String json) throws JSONException {
        List<Day> validated=parse(json); Set<String> allowedDates=new HashSet<>();
        for(Day day:validated) allowedDates.add(day.date().format(DATE));
        JSONArray input=new JSONArray(json),output=new JSONArray();
        for(int i=0;i<input.length();i++) {
            JSONObject day=input.getJSONObject(i);
            if(!allowedDates.contains(string(day,"дата"))) continue;
            JSONArray rows=day.getJSONArray("данные");
            for(int r=0;r<rows.length();r++) {
                JSONObject row=rows.getJSONObject(r);
                if(!"ИС-232".equalsIgnoreCase(string(row,"Группа"))) continue;
                JSONObject cleanRow=new JSONObject().put("Группа","ИС-232");
                for(int slot=1;slot<=5;slot++) cleanRow.put(slot+" пара",string(row,slot+" пара")).put("ауд"+slot,string(row,"ауд"+slot));
                output.put(new JSONObject().put("дата",string(day,"дата")).put("данные",new JSONArray().put(cleanRow)));
            }
        }
        return output.toString();
    }
    private LiveSchedule() {}
}
