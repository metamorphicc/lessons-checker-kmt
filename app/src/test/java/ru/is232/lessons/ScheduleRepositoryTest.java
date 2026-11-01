package ru.is232.lessons;

import org.junit.Test;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public class ScheduleRepositoryTest {
    private static final LocalDate MONDAY=LocalDate.of(2026,11,2);
    private static final String CANCELLED="[{\"дата\":\"02.11.2026\",\"данные\":[{\"Группа\":\"ИС-232\",\"1 пара\":\"Нет занятия\",\"ауд1\":\"\",\"2 пара\":\"Нет занятия\",\"ауд2\":\"\",\"3 пара\":\"Нет занятия\",\"ауд3\":\"\",\"4 пара\":\"Нет занятия\",\"ауд4\":\"\",\"5 пара\":\"Нет занятия\",\"ауд5\":\"\"}]}]";
    private ScheduleRepository.Day resolve(LocalDate date,String json,long fetched,boolean failed,long now) {
        return ScheduleRepository.resolve(date,ScheduleEngine.lessons(date,null,null),false,json,fetched,failed,now);
    }
    @Test public void cancellationOverridesBaseClasses() {
        ScheduleRepository.Day day=resolve(MONDAY,CANCELLED,1000,false,2000);
        assertEquals("site",day.source());assertTrue(day.lessons().isEmpty());
    }
    @Test public void missingDateFallsBackWithExplicitSource() {
        ScheduleRepository.Day day=resolve(MONDAY.plusDays(1),CANCELLED,1000,false,2000);
        assertEquals("base",day.source());assertEquals(2,day.lessons().size());assertEquals(1000,day.fetchedAt());
    }
    @Test public void failedFetchKeepsKnownCancellationAsCache() {
        ScheduleRepository.Day day=resolve(MONDAY,CANCELLED,1000,true,2000);
        assertEquals("cache",day.source());assertTrue(day.failed());assertTrue(day.lessons().isEmpty());
    }
    @Test public void oldSnapshotIsLabelledCache() {assertEquals("cache",resolve(MONDAY,CANCELLED,1000,false,1001+2*ScheduleRepository.REFRESH_MS).source());}
    @Test public void clockGoingBackDoesNotClaimFreshness() {assertEquals("cache",resolve(MONDAY,CANCELLED,2000,false,1000).source());}
    @Test public void corruptSnapshotFallsBackToBase() {assertEquals("base",resolve(MONDAY,"<html>error</html>",1000,true,2000).source());}
    @Test public void noPublishedDaysNeverCancelsBase() {assertEquals(2,resolve(MONDAY,"[]",1000,false,2000).lessons().size());}
    @Test public void firstRunDoesNotClaimWebsiteWasChecked() {assertTrue(resolve(MONDAY,"[]",0,false,2000).badge(ZoneId.of("Asia/Novosibirsk")).contains("ещё не проверен"));}
    @Test public void failedRequestIsVisibleWithoutCache() {assertTrue(resolve(MONDAY,"[]",0,true,2000).badge(ZoneId.of("Asia/Novosibirsk")).contains("не удалось обновить"));}
    @Test public void missingDateAfterSuccessfulFetchIsVisible() {assertTrue(resolve(MONDAY.plusDays(1),CANCELLED,1000,false,2000).badge(ZoneId.of("Asia/Novosibirsk")).contains("дата не опубликована"));}
}
