# Lessons · ИС-232

An offline Android app with a 2 × 2 home screen widget for group ИС-232. The widget shows the current or next class, its time, and classroom. Tap it to open the full daily schedule, teacher names, and date navigation.

## Installation

1. Transfer `output/lessons-is232.apk` to your phone and open it. Requires Android 8.0 or later.
2. If Android asks, allow your file manager to install the APK and finish installation.
3. Open **Пары · ИС-232**. Class times are already filled in from the supplied screenshot.
4. Tap **Добавить виджет** (Add widget). You can also long-press an empty area of the home screen, open **Widgets**, and select **Пары · ИС-232**. The default size is **2 × 2 cells**; it can be enlarged.
5. In **Настройки** (Settings), tap **Разрешить точные обновления** (Allow exact updates), if available. Android's Alarms & reminders permission allows the widget to switch classes at the scheduled time. Without it, updates may be delayed. Use **↻** to refresh manually; the footer shows when the displayed information was last updated.

When upgrading from version 1.0, install the APK over the existing app, then remove and re-add the widget. Your launcher may otherwise retain its previous large size. App settings are preserved.

Long subject names are shortened in the widget. Tap it to read the full schedule.

The default time zone is Novosibirsk (UTC+7). For another time zone, enable **Часовой пояс телефона** (Phone time zone) in Settings and save.

## Schedule

- Group ИС-232, building 1, first semester of 2026–2027. The source is page 23 of the supplied PDF.
- Class times: 08:30–10:00, 10:30–12:00, 12:20–13:50, 14:00–15:30, and 15:40–17:10.
- Saturday classes are remote, as confirmed by the user. They are excluded from the on-campus schedule. No week parity setting is needed.
- Substitutions, rescheduled classes, holidays, breaks, and the end of the semester are not handled automatically. Replace the dataset for a new semester.

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

[docs/live-source.md](docs/live-source.md) describes the public JSON feed used by the college website. Online loading has not been integrated into the APK yet.
