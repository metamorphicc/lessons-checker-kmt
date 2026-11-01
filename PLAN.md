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
- Substitutions, holidays, and breaks are not handled automatically.

## 5. Compact widget

- [x] 5.1. Reduce the widget to 2 × 2 cells and show the current or next class.
- [x] 5.2. Build and verify version 1.1: 36 checks, Android lint, and APK signature verification.

## 6. Live schedule

- [x] 6.1. Validate exact dates and group rows, add HTTPS loading and a dated cache.
- [x] 6.2. Connect background sync, the daily screen, and the compact widget.
- [x] 6.3. Test cancellations, missing dates, remote classes, and fallback behavior.
- [ ] 6.4. Build the update and document the live feed contract.
