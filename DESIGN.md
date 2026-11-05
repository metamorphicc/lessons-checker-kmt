# Schedule interface

<!-- Hallmark · modern-minimal · daily agenda · P5 H5 E4 S5 R5 V4 -->

The app is a native daily agenda, inspired by the clarity of iOS grouped lists.
Keep the existing Activity, settings, data repository and RemoteViews widget.

## Shared system

- Colors: `app/src/main/res/values/colors.xml` is the source of truth.
- Background: cool gray. Surfaces: white. Text: graphite. Accent: restrained blue.
- Typeface: bundled Manrope, including Cyrillic, under the SIL Open Font License.
  Use 450 for body text, 650 for emphasis, and 700 for the main heading.
- Spacing: 4, 8, 12, 16, 20, 24 and 32 dp. Main content has 20 dp gutters
  and a maximum width of 560 dp on tablets.
- Type: 32 sp page heading, 24 sp focus title, 16 sp lesson title,
  14 sp body, and 12 sp supporting text. System font scaling stays enabled.
- Rounded surfaces: 20 dp. Controls: 12 dp. All tap targets are at least 48 dp.
- Motion: native pressed feedback only. No entrance effects or decorative motion.
- Accessibility: text labels, descriptive icon buttons, visible keyboard focus,
  sufficient contrast and no fixed-height text containers.

## Main screen

Compact header, large date, weekly date picker, current/next lesson, grouped agenda.
Place lesson times in a separate left column. Group the day's lessons on one white
surface with inset separators. Expand source details on demand; always keep the
source and stale/failure state visible in the collapsed label.

## Settings

Grouped sections for widget installation, update permissions, timezone and bells.
Show labels for both bell fields, inline validation, and a clear save action.
Preserve drafts and scroll position across Activity recreation.

## Widget

Keep the 2 x 2 launcher footprint and existing resizing behavior. Share the app's
colors and type hierarchy. Prioritize class number, title, full time interval and room. Narrow cards must
not stretch vertically to fill unusually tall launcher cells. Use equal 12 dp
insets (4 dp only for the 90 dp minimum size). Preserve
source labels and the expanded refresh control.

## Boundaries

No fake phone chrome, gradients, glass panels, promotional copy or ornamental
illustrations. Source data remains authoritative; never invent a missing subject
or date. Native Android system permission and widget dialogs retain their platform UI.
