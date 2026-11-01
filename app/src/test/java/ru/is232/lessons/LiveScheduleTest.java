package ru.is232.lessons;

import org.json.*;
import org.junit.Test;
import java.time.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

public class LiveScheduleTest {
    private JSONObject row() throws Exception {
        JSONObject r=new JSONObject().put("Группа","ИС-232");
        for(int i=1;i<=5;i++) r.put(i+" пара","Нет занятия").put("ауд"+i,"Нет ");
        return r;
    }
    private String feed(String date,JSONObject... rows) throws Exception {
        JSONArray list=new JSONArray();for(JSONObject row:rows) list.put(row);
        return new JSONArray().put(new JSONObject().put("лист","Понедельник").put("дата",date).put("данные",list)).toString();
    }
    @Test public void recordedFeedUsesOnlyExplicitDates() throws Exception {
        String json;
        try(java.io.InputStream in=getClass().getResourceAsStream("/live-is232.json")) {assertNotNull(in);json=new String(in.readAllBytes(),StandardCharsets.UTF_8);}
        List<LiveSchedule.Day> days=LiveSchedule.parse(json);
        assertEquals(3,days.size());
        LiveSchedule.Day monday=days.stream().filter(d->d.date().equals(LocalDate.of(2026,11,2))).findFirst().orElseThrow();
        assertEquals(2,monday.campus().size());assertEquals(3,monday.campus().get(0).slot());
        assertEquals("Бородина Е.Н.",monday.campus().get(0).title());assertEquals("106 К1",monday.campus().get(0).room());
        assertEquals(4,monday.campus().get(1).slot());assertTrue(days.stream().noneMatch(d->d.date().equals(LocalDate.of(2026,11,3))));
        assertEquals(3,LiveSchedule.parse(LiveSchedule.sanitize(json)).size());
    }
    @Test public void undatedRowNeverBecomesToday() throws Exception {assertTrue(LiveSchedule.parse(feed("",row())).isEmpty());}
    @Test public void missingGroupIsNotACancelledDay() throws Exception {assertTrue(LiveSchedule.parse(feed("02.11.2026",row().put("Группа","ИС-2321"))).isEmpty());}
    @Test public void explicitCancellationRemainsAnAuthoritativeDay() throws Exception {
        List<LiveSchedule.Day> days=LiveSchedule.parse(feed("02.11.2026",row()));
        assertEquals(1,days.size());assertTrue(days.get(0).campus().isEmpty());assertFalse(days.get(0).remoteOnly());
    }
    @Test public void remoteDayIsExcludedFromCampusClasses() throws Exception {
        LiveSchedule.Day day=LiveSchedule.parse(feed("31.10.2026",row().put("1 пара","Бородина Е.Н.").put("ауд1"," ДО "))).get(0);
        assertTrue(day.campus().isEmpty());assertTrue(day.remoteOnly());
    }
    @Test public void mixedDayKeepsCampusClass() throws Exception {
        LiveSchedule.Day day=LiveSchedule.parse(feed("02.11.2026",row().put("1 пара","Teacher").put("ауд1","ДО").put("2 пара","Another").put("ауд2","106 К1"))).get(0);
        assertEquals(1,day.campus().size());assertEquals(2,day.campus().get(0).slot());assertFalse(day.remoteOnly());
    }
    @Test public void changedRoomComesFromWebsite() throws Exception {
        assertEquals("314 К2",LiveSchedule.parse(feed("05.11.2026",row().put("2 пара","Казаков Е.А.").put("ауд2","314 К2"))).get(0).campus().get(0).room());
    }
    @Test public void missingRoomDoesNotInventOne() throws Exception {
        assertNull(LiveSchedule.parse(feed("02.11.2026",row().put("1 пара","Teacher"))).get(0).campus().get(0).room());
    }
    @Test public void rejectsInvalidDate() throws Exception {assertThrows(JSONException.class,()->LiveSchedule.parse(feed("31.02.2026",row())));}
    @Test public void rejectsDuplicateGroup() throws Exception {assertThrows(JSONException.class,()->LiveSchedule.parse(feed("02.11.2026",row(),row())));}
    @Test public void rejectsDuplicateDate() throws Exception {
        JSONArray days=new JSONArray(feed("02.11.2026",row()));days.put(days.getJSONObject(0));
        assertThrows(JSONException.class,()->LiveSchedule.parse(days.toString()));
    }
    @Test public void rejectsBrokenColumns() throws Exception {JSONObject r=row();r.remove("3 пара");assertThrows(JSONException.class,()->LiveSchedule.parse(feed("02.11.2026",r)));}
    @Test public void rejectsUnexpectedFieldTypes() throws Exception {assertThrows(JSONException.class,()->LiveSchedule.parse(feed("02.11.2026",row().put("1 пара",42))));}
    @Test public void rejectsHtmlResponse() {assertThrows(JSONException.class,()->LiveSchedule.parse("<html>Error</html>"));}
    @Test public void sanitizedCacheRemovesOtherGroupsAndExtraFields() throws Exception {
        String clean=LiveSchedule.sanitize(feed("02.11.2026",row().put("extra","discard"),row().put("Группа","Other")));
        assertFalse(clean.contains("Other"));assertFalse(clean.contains("discard"));assertEquals(1,LiveSchedule.parse(clean).size());
    }
}
