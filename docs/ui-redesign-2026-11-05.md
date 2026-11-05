# Native interface redesign · November 5, 2026

Version 1.4 uses the shared system in [DESIGN.md](../DESIGN.md). The daily screen,
settings and home widget remain native Android views. Schedule retrieval and
exact-date selection are unchanged.

## Changes

- Bundled Manrope with Cyrillic, shared semantic colors and flat pressed controls.
- Weekly date picker, current/next class summary and grouped agenda with a time column.
- Expandable source details; source, fallback and network failure remain visible.
- Grouped settings, styled time inputs, inline validation and modern back handling.
- Restyled 2 × 2 RemoteViews widget, preserving compact time and classroom details.

## Verification

Android 16 AOSP x86_64 emulator, installed release APK, package `ru.is232.lessons`,
versionCode 5. Native instrumentation checks all measured text for vertical
clipping, overflowing lines and ellipsis; touch controls have a 48 dp minimum.
The weekly strip intentionally scrolls horizontally on narrow screens.

| Check | Result |
| --- | --- |
| Main screen at 320 / 375 / 414 / 768 dp | Passed |
| Settings at 320 dp | Passed |
| Main/settings at 320 dp, font scale 1.3 | Passed |
| PDF fallback with long subject names, 320 dp, font scale 1.3 | Passed |
| 90 × 90 dp widget at font scale 1.3 | Time and room fit; long title intentionally shortened |
| Automatic 30-second screen update | Scroll retained: 400 → 400 px |
| Activity recreation while editing | Draft 09:00 retained; settings remain open |
| Refresh in flight | Refresh control disabled |
| Invalid bell time | Rejected with inline explanation |
| Valid bell times | Saved and returned to agenda |
| All bell times cleared | Explicit missing-time state |
| System Back from settings | Returns to agenda |
| Next week / Today | November 12 / November 5 selected correctly |
| Existing schedule checks | 36 calendar checks + 25 JUnit tests passed |
| Release build / lint / signature | Passed |

Evidence: [layout matrix](ui-redesign-2026-11-05/layout-matrix.json),
[settings validation](ui-redesign-2026-11-05/settings-validation.json),
[navigation](ui-redesign-2026-11-05/navigation.json),
[widget measurements](ui-redesign-2026-11-05/widget-font13.json).
Behavioral probes ran before the final source-label/calendar simplification;
layout probes were repeated after the relevant visual fixes.

## Screenshots

[Daily screen](ui-redesign-2026-11-05/app.png) ·
[Settings, large font](ui-redesign-2026-11-05/settings-font13.png) ·
[PDF fallback, large font](ui-redesign-2026-11-05/baseline-font13.png) ·
[Launcher widget](ui-redesign-2026-11-05/launcher-widget.png) ·
[Minimum-size widget](ui-redesign-2026-11-05/widget-font13.png)

App captures use the emulator's dated website snapshot; the fallback capture
selects December 1 to exercise long PDF subject names. The minimum-size widget
uses controlled fixture content and does not represent a live class.

No physical phone or OEM launcher was available. These checks do not establish
background update timing or widget sizing on every device.
