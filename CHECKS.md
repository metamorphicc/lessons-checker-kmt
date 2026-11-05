# Verification

- 36 calendar and time checks cover class starts and ends, breaks, empty morning slots, remote Saturdays, Sundays, anchor weeks, year transitions, and midnight in Novosibirsk.
- 25 live-feed and repository tests cover exact-date selection, missing groups/dates, authoritative cancellations, remote/mixed days, changed/missing classrooms, malformed dates and payloads, duplicate rows, sanitized caching, stale/failed snapshots, clock changes, and visible source labels.
- The production Java HTTPS client was run against the live endpoint and parsed three dated ИС-232 rows: November 2, October 30, and October 31, 2026.
- The transcribed schedule was checked against the image of PDF page 23. Class times come from the user's screenshot.
- `assembleRelease` and `lintRelease` pass. Non-blocking lint warnings cover newer tool versions, Android 12 attributes, and backup transfer rules.
- The APK signature was verified with `apksigner verify`. Version 1.2 requests internet and network-state access for the public schedule feed.

## Verification limits

Version 1.3 was installed and launched on an Android 16 x86_64 emulator. The launcher pin dialog, native layouts at 320/375/414/768 dp, compact RemoteViews, scroll retention, draft restoration, and loading state were checked. Evidence is linked from [the UI audit](docs/ui-audit-2026-11-02.md).

No physical Android phone was available. OEM launcher sizing and background delivery on a physical device remain unverified. The visual tests use controlled local data rather than claiming a live college response.

## Device checks

1. Install the APK and open **Пары · ИС-232**.
2. Check the class times in Settings against the supplied screenshot.
3. Open November 2, 2026: the dated website row should show classes 3/4 with Бородина and Давыдова. Browse an unpublished date and verify the **База** warning. On an offline first run, the baseline has Monday 3/4, Tuesday 3/4, Wednesday 3/4/5, Thursday 1/2/3/4, Friday 2/3/4, Saturday remote, Sunday free.
4. Add the 2 × 2 widget and compare its current or next class, time, and classroom with the app. Long names are shortened; tapping opens the full schedule. After upgrading, remove and re-add the old widget.
5. Allow exact updates in Settings if Android offers the option. Check the next scheduled class transition and the app's refresh button. Expanded widgets also expose **↻**.
6. Check the widget after a reboot and a time zone change.
7. Fetch a dated day, go offline, and refresh again. Check the **Кэш сайта** label, retained classes, and last successful fetch time. Reconnect and refresh to recover.
8. Verify background sync on the phone with the app closed. Check that the refresh action updates website data as well as the display.

Updates depend on the college publishing exact dates and Android allowing background work. Undated rows and stale weekday names never establish a current schedule.

## Version 1.4 redesign

The release APK was installed on Android 16. Native layouts at 320/375/414/768 dp,
font scale 1.3, minimum-size widget text, settings validation, system back and week
navigation were verified. Scroll retention, draft restoration and refresh loading
checks passed. All 61 existing schedule checks, release lint and APK signature
verification passed. See [redesign evidence](docs/ui-redesign-2026-11-05.md).

## Version 1.4.1 widget correction

The prior 90 dp XML-only fixture missed the launcher's tall 131 × 244 dp host.
The revised native probe invokes the production provider renderer and asserts
card dimensions, vertical text fit and a complete time interval. Verified at
131 × 244 dp with font scales 1.0/1.3, 90 × 90 dp at 1.3, and 245 × 122/244 dp.
The real launcher card was also inspected. See [geometry evidence](docs/widget-geometry-2026-11-05.md).
