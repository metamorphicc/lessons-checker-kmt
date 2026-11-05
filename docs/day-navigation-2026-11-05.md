# Daily navigation UX · November 5, 2026

Version 1.4.2 replaces the week pager with direct day navigation. Both arrows
move by exactly one calendar day. The seven-date strip and standalone year row
are removed, reducing the distance to the current class and daily agenda.

On today's date the center "Сегодня" label is passive. On other dates it becomes
an explicit "На сегодня" return action. The large selected date and weekday remain
visible above the controls. Exact-date live lookup and the labeled PDF fallback
are unchanged; browsing an unpublished date does not imply that replacements
have been confirmed.

## Verification

The installed release APK was exercised on the Android 16 AOSP emulator.
Native instrumentation clicks the actual arrow and return views, then reads the
Activity's selected date. It asserts forward/backward steps, return to today,
removal of the return action, and the passive label's non-clickable state.

| Starting date | Next | Previous |
| --- | --- | --- |
| 2026-11-05 | 2026-11-06 | 2026-11-04 |
| 2026-11-08 (Sunday) | 2026-11-09 | 2026-11-07 |
| 2026-11-30 | 2026-12-01 | 2026-11-29 |
| 2026-12-31 | 2027-01-01 | 2026-12-30 |

Every scenario returned to November 5, 2026, and verified the passive Today state.
Individual reports are in [the evidence directory](day-navigation-2026-11-05).

Native layouts passed at 320/375/414/768 dp. The return action on another date
also passed at 320 dp with font scale 1.3: no clipped control text or undersized
touch targets. See [layout measurements](day-navigation-2026-11-05/layout-matrix.json).
Release build, Android lint and APK signature verification passed.

[Today](day-navigation-2026-11-05/today.png) ·
[Other date, large font](day-navigation-2026-11-05/other-day-font13.png)

Screenshots use the emulator's retained dated feed and the PDF fallback.
No physical phone was available for verification.
