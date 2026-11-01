package ru.is232.lessons;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/** Calendar-only engine. The device UI and widget share this implementation. */
public final class ScheduleEngine {
    public record Lesson(int day, int slot, String title, String shortTitle,
                         String teacher, String room, String week) {}
    public record Bell(LocalTime start, LocalTime end) {
        public Bell {
            if (start == null || end == null || !start.isBefore(end))
                throw new IllegalArgumentException("Начало должно быть раньше конца");
        }
    }
    public record Moment(String label, Lesson current, Lesson next) {}

    public static LocalDate monday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static String week(LocalDate date, LocalDate anchorMonday, String anchorWeek) {
        if (anchorMonday == null || !("odd".equals(anchorWeek) || "even".equals(anchorWeek))) return null;
        long delta = ChronoUnit.WEEKS.between(monday(anchorMonday), monday(date));
        return Math.floorMod(delta, 2) == 0 ? anchorWeek : ("odd".equals(anchorWeek) ? "even" : "odd");
    }

    public static List<Lesson> lessons(LocalDate date, LocalDate anchor, String anchorWeek) {
        String week = week(date, anchor, anchorWeek);
        List<Lesson> result = new ArrayList<>();
        for (Lesson lesson : ScheduleData.LESSONS) {
            if (lesson.day() == date.getDayOfWeek().getValue()
                    && (lesson.week() == null || week == null || lesson.week().equals(week))) result.add(lesson);
        }
        return result;
    }

    public static boolean validBells(List<Bell> bells) {
        if (bells.size() != 5) return false;
        for (int i = 1; i < bells.size(); i++) {
            if (bells.get(i).start().isBefore(bells.get(i-1).end())) return false;
        }
        return true;
    }

    public static Moment moment(List<Lesson> lessons, LocalTime time, List<Bell> bells) {
        if (lessons.isEmpty()) return new Moment("Сегодня без пар", null, null);
        if (!validBells(bells)) return new Moment("Добавь время звонков", null, null);
        Lesson next = null;
        for (Lesson lesson : lessons) {
            Bell bell = bells.get(lesson.slot() - 1);
            if (!time.isBefore(bell.start()) && time.isBefore(bell.end())) {
                for (Lesson later : lessons) if (later.slot() > lesson.slot()) { next = later; break; }
                return new Moment("Сейчас · " + lesson.slot() + " пара", lesson, next);
            }
            if (time.isBefore(bell.start())) return new Moment("Далее · " + lesson.slot() + " пара", null, lesson);
        }
        return new Moment("На сегодня всё", null, null);
    }

    public static String room(Lesson lesson) {
        if (lesson.room() == null) return "Аудитория не указана";
        return lesson.room().matches("\\d+.*") ? "Ауд. " + lesson.room() : lesson.room();
    }
    public static String weekLabel(String week) {
        return "odd".equals(week) ? "Нечётная" : "even".equals(week) ? "Чётная" : "Чётность не задана";
    }
    private ScheduleEngine() {}
}
