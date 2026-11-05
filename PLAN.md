# Plan

Each completed substep gets a separate commit with a short English message.

## 1. Source data

- [x] 1.1. Inspect the project and record the plan.
- [x] 1.2. Transcribe ИС-232 from page 23, preserving conditions and missing information.

## 2. Schedule logic

- [x] 2.1. Calculate dates, week parity, and daily classes.
- [x] 2.2. Add configurable class times and current/next class detection.

## 3. Android app

- [x] 3.1. Build the daily schedule screen and settings.
- [x] 3.2. Add a native home screen widget.
- [x] 3.3. Schedule updates at class boundaries, handle Android restrictions, and support compact layouts.

## 4. Verification and installation

- [x] 4.1. Check dates, class boundaries, weekends, and week transitions.
- [x] 4.2. Build the APK and write installation instructions.

## Source limitations

- The PDF is scanned; the ИС-232 schedule is on page 23.
- The PDF has no class times. Defaults come from the user's screenshot supplied on November 1, 2026.
- Saturday classrooms are missing from the PDF; do not infer them from other days.
- The user confirmed Saturday classes are remote. They are excluded from on-campus classes, so the UI does not need week parity.
- The baseline does not contain substitutions or holidays. Version 1.2 uses dated website rows when available and labels the PDF fallback explicitly.

## 5. Compact widget

- [x] 5.1. Reduce the widget to 2 × 2 cells and show the current or next class.
- [x] 5.2. Build and verify version 1.1: 36 checks, Android lint, and APK signature verification.

## 6. Live schedule

- [x] 6.1. Validate exact dates and group rows, add HTTPS loading and a dated cache.
- [x] 6.2. Connect background sync, the daily screen, and the compact widget.
- [x] 6.3. Test cancellations, missing dates, remote classes, and fallback behavior.
- [x] 6.4. Build version 1.2 and document the live feed contract: 36 time checks, 25 live-data tests, release lint, Java HTTPS smoke check, and APK signature verification.

## 7. Native UI audit and fixes

- [x] 7.1. Audit UI with Hallmark and reproduce issues on Android 16.
- [x] 7.2. Preserve scrolling and unsaved settings across updates/recreation; disable in-flight refresh.
- [x] 7.3. Correct launcher minimum spans and compact text sizing; remove undersized compact controls.
- [x] 7.4. Verify fixes with native UI instrumentation, rerun 61 checks and lint, and build version 1.3.

## 8. Interface redesign

- [x] 8.1. Establish shared colors and typography; redesign the daily agenda and date navigation.
- [x] 8.2. Restyle settings and the 2 × 2 widget without changing schedule retrieval.
- [x] 8.3. Handle modern and legacy back navigation and missing bell times.
- [x] 8.4. Verify native layouts, settings flows and release version 1.4.

## 9. Widget geometry correction

- [x] 9.1. Inspect real launcher dimensions and cap the compact card's aspect ratio.
- [x] 9.2. Align content insets and show the full start/end time interval.
- [x] 9.3. Measure actual provider RemoteViews at normal and large fonts; release 1.4.1.

## 10. Daily navigation UX

- [x] 10.1. Replace week paging and the seven-date strip with adjacent-day arrows.
- [x] 10.2. Make Today non-interactive on today's date; offer a return action on other dates.
- [x] 10.3. Verify native navigation and narrow layouts; release version 1.4.2.
