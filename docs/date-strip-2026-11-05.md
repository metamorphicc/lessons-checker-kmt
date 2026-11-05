# Date strip restoration · November 5, 2026

Version 1.4.3 restores the seven-day date strip below the daily navigation controls.
Each cell shows the weekday initials and date number. Tapping selects that exact
date, with a single highlighted cell. The selected cell stays visible on narrow
screens. Arrow controls still move one day; "На сегодня" only appears on other dates.

## Verification

The release APK was installed on the Android 16 AOSP emulator. Native checks
clicked Sunday, November 8, then the next-day arrow. The selected date became
Monday, November 9, and the new week's Monday was highlighted exactly once.
The selected Sunday cell was fully visible at 320 dp. Returning to today and
one-day forward/backward behavior also passed.

Layouts passed at 320/375/414/768 dp and at 320 dp with font scale 1.3 on another
date. The probe found no clipped controls, overflowing text or undersized targets.
Release build, Android lint and APK signature verification passed.

[Preview](date-strip-2026-11-05/today.png) ·
[Selection report](date-strip-2026-11-05/date-strip-selection.json) ·
[Navigation report](date-strip-2026-11-05/navigation-2026-11-05.json) ·
[Layout measurements](date-strip-2026-11-05/layout-matrix.json)

The capture uses the emulator's dated website snapshot. No physical phone was tested.
