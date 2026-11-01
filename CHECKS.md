# Verification

- 36 calendar and time checks cover class starts and ends, breaks, empty morning slots, remote Saturdays, Sundays, anchor weeks, year transitions, and midnight in Novosibirsk.
- 25 live-feed and repository tests cover exact-date selection, missing groups/dates, authoritative cancellations, remote/mixed days, changed/missing classrooms, malformed dates and payloads, duplicate rows, sanitized caching, stale/failed snapshots, clock changes, and visible source labels.
- The production Java HTTPS client was run against the live endpoint and parsed three dated ИС-232 rows: November 2, October 30, and October 31, 2026.
- The transcribed schedule was checked against the image of PDF page 23. Class times come from the user's screenshot.
- `assembleRelease` and `lintRelease` pass. Non-blocking lint warnings cover newer tool versions, Android 12 attributes, and backup transfer rules.
- The APK signature was verified with `apksigner verify`. Version 1.2 requests internet and network-state access for the public schedule feed.

## Verification limits

No Android phone or configured emulator was available. Activity startup, rendering on a real screen, widget pinning, and scheduled updates on a specific device have not been verified. A successful build and logic checks do not establish those results.

## Device checks

1. Install the APK and open **Пары · ИС-232**.
2. Check the class times in Settings against the supplied screenshot.
3. Open November 2, 2026: the dated website row should show classes 3/4 with Бородина and Давыдова. Browse an unpublished date and verify the **Базовое** warning. On an offline first run, the baseline has Monday 3/4, Tuesday 3/4, Wednesday 3/4/5, Thursday 1/2/3/4, Friday 2/3/4, Saturday remote, Sunday free.
4. Add the 2 × 2 widget and compare its current or next class, time, and classroom with the app. Long names are shortened; tapping opens the full schedule. After upgrading, remove and re-add the old widget.
5. Allow exact updates in Settings if Android offers the option. Check the next scheduled class transition and the manual **↻** button.
6. Check the widget after a reboot and a time zone change.
7. Fetch a dated day, go offline, and refresh again. Check the **Кэш сайта** label, retained classes, and last successful fetch time. Reconnect and refresh to recover.
8. Verify background sync on the phone with the app closed. Check that **↻** eventually refreshes the website data as well as the widget display.

Updates depend on the college publishing exact dates and Android allowing background work. Undated rows and stale weekday names never establish a current schedule.
