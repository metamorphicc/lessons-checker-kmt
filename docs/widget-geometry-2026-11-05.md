# Widget geometry correction · November 5, 2026

The original native fixture used a 90 × 90 dp root. It did not reveal the stretched
card in the real launcher's 131 × 244 dp host. Treat the version 1.4 launcher capture
as evidence of that defect, not a successful geometry check.

## Implementation

On Android 12+, use the launcher's `OPTION_APPWIDGET_SIZES` list and supply one
RemoteViews layout per exact host size. Narrow cards use a height no greater than
their width. The launcher continues to reserve its own 2 × 2 cells; unused host
space is transparent. The app cannot redefine a launcher's grid cells.

The visible card has equal 12 dp insets, a top-aligned title, a full start/end
interval and classroom. The 90 dp variant uses equal 4 dp insets to retain readable
time and room at font scale 1.3. Live teacher labels stay on one line when possible.
Long titles may be shortened; tapping opens the full app.

For Android 8–11, portrait/landscape RemoteViews use content-sized card layouts,
so the white background no longer stretches to the full host height. This legacy
path has not been run on an older emulator.

The platform's [exact layout API](https://developer.android.com/develop/ui/views/appwidgets/layouts)
is used directly, without changing launcher settings.

## Native measurements

The probe invokes `LessonsWidget.render` and lays out its returned RemoteViews in
the reported host size, rather than constructing a separate imitation of the UI.

| Host (dp) | Font scale | Visible card (dp) | Insets (dp) | Time/room |
| --- | --- | --- | --- | --- |
| 131 × 244 | 1.0 | 131 × 131 | 12 | Fit |
| 131 × 244 | 1.3 | 131 × 131 | 12 | Fit |
| 90 × 90 | 1.3 | 90 × 90 | 4 | Fit |
| 245 × 122 | 1.0 | 245 × 122 | 12 | Fit |
| 245 × 244 | 1.0 | 245 × 244 | 12 | Fit |

Every visible text layout fits vertically, and the full `12:20–13:50` interval has
zero ellipsized characters at every tested size. The smallest title intentionally
ellipsizes. Each JSON file records actual native card and text measurements.

[Real launcher](widget-geometry-2026-11-05/launcher.png) ·
[131 dp card](widget-geometry-2026-11-05/widget-131x244-font1.0.png) ·
[Large font](widget-geometry-2026-11-05/widget-131x244-font1.3.png) ·
[Minimum size](widget-geometry-2026-11-05/widget-90x90-font1.3.png)

Standalone captures use controlled lesson content; the launcher uses the app's
dated website snapshot. No physical OEM launcher was tested.
