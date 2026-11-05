# Lessons · ИС-232

An Android app with a 2 × 2 home screen widget for group ИС-232. It loads dated schedules from the college website and keeps a local cache. The widget shows the current or next class, its time, and classroom. Tap it to open the full daily schedule and date navigation.

## Installation

1. Transfer `output/lessons-is232.apk` to your phone and open it. Requires Android 8.0 or later.
2. If Android asks, allow your file manager to install the APK and finish installation.
3. Open **Пары · ИС-232**. Class times are already filled in from the supplied screenshot.
4. Tap **Добавить виджет** (Add widget). You can also long-press an empty area of the home screen, open **Widgets**, and select **Пары · ИС-232**. The default size is **2 × 2 cells**; it can be enlarged.
5. In **Настройки** (Settings), tap **Разрешить обновления** (Allow updates), if available. Android's Alarms & reminders permission allows the widget to switch classes at the scheduled time. Without it, updates may be delayed. Tap a compact widget to open the app and refresh there. Expanded widgets also have a **↻** button and a last-fetch footer.

When upgrading from version 1.0, install the APK over the existing app, then remove and re-add the widget. Your launcher may otherwise retain its previous large size. App settings are preserved.

Long labels are shortened in the widget. Tap it to read the full schedule.

The default time zone is Novosibirsk (UTC+7). For another time zone, enable **Как на телефоне** (Use phone time zone) in Settings and save.

## Schedule

- Group ИС-232. Dated website rows take priority; the fallback is the first-semester 2026–2027 schedule on page 23 of the supplied PDF.
- Class times: 08:30–10:00, 10:30–12:00, 12:20–13:50, 14:00–15:30, and 15:40–17:10.
- Saturday classes are remote, as confirmed by the user. They are excluded from the on-campus schedule. No week parity setting is needed.
- Published cancellations, extra classes, and classroom changes replace the entire baseline day. The app does not invent dates for undated rows or reuse a previous week's entry.
- Subjects are not included in the live feed. Live classes show the teacher or activity exactly as supplied by the website; the PDF fallback retains subject names.
- Holidays and breaks are reflected only when the college publishes the corresponding dated schedule. An unpublished day falls back to the PDF with an explicit warning. Replace the fallback dataset for a new semester.

## Live updates and offline behavior

- The app checks on opening (with a 15-minute freshness interval) and requests background sync every 15 minutes while a network is available. Android can delay background jobs, especially in battery-saving modes.
- Tap the refresh icon beside the source label in the app or **↻** on an expanded widget to request a new fetch. Compact widgets prioritize class, time, and classroom; their header shows the source. Repeated requests are briefly throttled.
- Compact class details use **До HH:MM** for a current class and **С HH:MM** for the next one. Classroom numbers are displayed without the extra label so they remain readable in small cells.
- **Сайт** means an exact-date website schedule was fetched recently. **Кэш** means the app is using an older successful fetch or the latest refresh failed. Tap the source label to see the fetch timestamp and details. The timestamp is the fetch time, not the college's edit time; the endpoint does not provide an edit timestamp.
- **База** means the displayed day comes from the PDF. The app distinguishes an unpublished date, a first run, and a failed refresh. A published day with all five classes cancelled stays empty and is never filled from the PDF.
- Remote classes marked **ДО** are excluded from on-campus classes. Saturday's baseline remains remote as requested. A dated website row can override it if the college publishes actual on-campus classes.
- Only dated ИС-232 rows are cached. Failed downloads and invalid responses preserve the last valid snapshot. A successful response replaces the snapshot, including removal of dates no longer published.

## Build and verification

Built with Java, the standard Android SDK, RemoteViews, and AlarmManager. The app has no third-party runtime dependencies.

Requires JDK 17+ (verified with JDK 21), Android SDK Platform 36.1, and an installed Android SDK. Gradle Wrapper is included. The first build downloads build dependencies.

```powershell
.\tools\test.ps1
.\tools\build.ps1
```

Set `JAVA_HOME` and `ANDROID_HOME` if needed. `tools/test.ps1` checks schedule logic without an Android device. The build produces `output/lessons-is232.apk` and a SHA-256 checksum file.

To update the dataset, edit `data/schedule.json`, run `python tools/generate_data.py`, and rebuild. Edit the JSON source rather than the generated Java dataset.

The APK is a local release build signed with this computer's standard Android development key. Use the same key for upgrades over an existing installation. The key is stored in the user's Android directory and is not tracked in this repository. This build has not been published to an app store.

[CHECKS.md](CHECKS.md) records completed checks and outstanding device checks. [PLAN.md](PLAN.md) tracks implementation steps. Each completed substep has a separate, short commit.

See the [Android alarm documentation](https://developer.android.com/develop/background-work/services/alarms) for background update restrictions. The app reschedules updates after a reboot, a system time change, or an app update.

## Live schedule research

[docs/live-source.md](docs/live-source.md) describes the public JSON feed used by the college website. Version 1.2 integrates this feed in the daily screen and widget through a standard Android JobService. The app requests internet and network-state access; no account or API key is needed.

## UI verification

Version 1.3 fixes the launcher span, compact text clipping, automatic scroll resets, and settings drafts lost during Activity recreation. The Android 16 AOSP launcher was checked at a 320 dp screen width and now offers 2 × 2 cells. Compact RemoteViews were checked at 90 × 90 dp with font scale 1.3. See [the UI audit and evidence](docs/ui-audit-2026-11-02.md). Physical launchers can use different cell dimensions.

## Interface

Version 1.4 introduces a light native agenda with a weekly date picker, a current
or next class summary, and grouped rows with a dedicated time column. Settings
and the 2 × 2 widget share the same colors and bundled Manrope typeface. The
source label stays visible; tap it for freshness and fallback details. Use the
arrow buttons to move between weeks, or **Сегодня** to return to today.

The data loading, exact-date matching, cache and baseline fallback are unchanged.
[DESIGN.md](DESIGN.md) records the native design system. Manrope's SIL Open Font
License is included in [docs/licenses/Manrope-OFL.txt](docs/licenses/Manrope-OFL.txt).
