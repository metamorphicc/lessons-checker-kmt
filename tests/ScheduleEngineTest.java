package ru.is232.lessons;

import java.time.*;
import java.util.*;

public final class ScheduleEngineTest {
    private static int checks=0;
    static void check(boolean condition,String name) { checks++; if(!condition) throw new AssertionError(name); }
    static List<ScheduleEngine.Lesson> day(String date) {return ScheduleEngine.lessons(LocalDate.parse(date),null,null);}
    static ScheduleEngine.Moment at(String date,String time) {
        List<ScheduleEngine.Bell> bells=new ArrayList<>();
        for(String[] bell:ScheduleData.BELLS) bells.add(new ScheduleEngine.Bell(LocalTime.parse(bell[0]),LocalTime.parse(bell[1])));
        return ScheduleEngine.moment(day(date),LocalTime.parse(time),bells);
    }
    public static void main(String[] args) {
        check(day("2026-11-02").size()==2,"Monday has two lessons");
        check(day("2026-11-02").get(0).slot()==3,"Monday starts at third slot");
        check(day("2026-11-03").size()==2,"Tuesday has two lessons");
        check(day("2026-11-04").size()==3,"Wednesday has three lessons (static, holidays not inferred)");
        check(day("2026-11-05").size()==4,"Thursday has four lessons");
        check(day("2026-11-06").size()==3,"Friday has three lessons");
        check(day("2026-11-07").isEmpty(),"Saturday distance learning excluded");
        check(day("2026-11-14").isEmpty(),"Both Saturday variants excluded");
        check(day("2026-11-01").isEmpty(),"Sunday empty");
        check(at("2026-11-02","08:30").current()==null,"No invented first lesson on Monday");
        check(at("2026-11-02","08:30").next().slot()==3,"Monday next is third lesson");
        check(at("2026-11-02","12:19").current()==null,"Before third lesson");
        check(at("2026-11-02","12:20").current().slot()==3,"Exact start is current");
        check(at("2026-11-02","13:49").current().slot()==3,"Last minute current");
        check(at("2026-11-02","13:50").current()==null,"Exact end no longer current");
        check(at("2026-11-02","13:50").next().slot()==4,"Break points to fourth lesson");
        check(at("2026-11-02","14:00").current().slot()==4,"Fourth starts at 14:00");
        check(at("2026-11-02","15:30").next()==null,"After last lesson no next");
        check(at("2026-11-04","17:09").current().slot()==5,"Wednesday fifth lesson");
        check(at("2026-11-04","17:10").current()==null,"Last exact end");
        check(at("2026-11-05","08:30").current().slot()==1,"Thursday first lesson");
        check(at("2026-11-05","10:00").next().slot()==2,"Thirty-minute break");
        check(at("2026-11-06","10:29").next().slot()==2,"Friday starts at second lesson");
        check(at("2026-11-07","12:20").current()==null,"No current Saturday class");
        check(at("2026-11-01","12:20").next()==null,"No Sunday next class");
        check(ScheduleEngine.moment(day("2026-11-02"),LocalTime.NOON,List.of()).current()==null,"Unknown bells never invent current");
        LocalDate anchor=LocalDate.of(2026,11,2);
        check("odd".equals(ScheduleEngine.week(anchor,anchor,"odd")),"Anchor week");
        check("even".equals(ScheduleEngine.week(anchor.minusDays(7),anchor,"odd")),"Before anchor");
        check("even".equals(ScheduleEngine.week(anchor.plusDays(7),anchor,"odd")),"Following week");
        check(ScheduleEngine.week(anchor,null,null)==null,"Unknown parity stays unknown");
        check("even".equals(ScheduleEngine.week(LocalDate.of(2027,1,4),LocalDate.of(2026,12,28),"odd")),"Year boundary");
        check(Instant.parse("2026-11-01T17:00:00Z").atZone(ZoneId.of("Asia/Novosibirsk")).toLocalDate().equals(anchor),"Novosibirsk midnight");
        check(ScheduleEngine.room(day("2026-11-02").get(0)).equals("Ауд. 106"),"Classroom preserved");
        check(ScheduleEngine.room(day("2026-11-02").get(1)).equals("Тренажёрный зал"),"Gym label preserved");
        List<ScheduleEngine.Bell> bad=new ArrayList<>();
        for(int i=0;i<5;i++) bad.add(new ScheduleEngine.Bell(LocalTime.of(8,30),LocalTime.of(10,0)));
        check(!ScheduleEngine.validBells(bad),"Overlapping bells rejected");
        try {new ScheduleEngine.Bell(LocalTime.NOON,LocalTime.NOON); throw new AssertionError("Zero duration accepted");}
        catch(IllegalArgumentException expected) {checks++;}
        System.out.println(checks+" checks passed");
    }
}
