# Verification

- 36 calendar and time checks cover class starts and ends, breaks, empty morning slots, remote Saturdays, Sundays, anchor weeks, year transitions, and midnight in Novosibirsk.
- The transcribed schedule was checked against the image of PDF page 23. Class times come from the user's screenshot.
- `assembleRelease` and `lintRelease` pass. Non-blocking lint warnings cover newer tool versions, Android 12 attributes, and backup transfer rules.
- The APK signature was verified with `apksigner verify`. The app does not request internet access.

## Verification limits

No Android phone or configured emulator was available. Activity startup, rendering on a real screen, widget pinning, and scheduled updates on a specific device have not been verified. A successful build and logic checks do not establish those results.

## Device checks

1. Install the APK and open **Пары · ИС-232**.
2. Check the class times in Settings against the supplied screenshot.
3. Browse the week: Monday 3/4, Tuesday 3/4, Wednesday 3/4/5, Thursday 1/2/3/4, Friday 2/3/4, Saturday remote, Sunday free.
4. Add the 2 × 2 widget and compare its current or next class, time, and classroom with the app. Long names are shortened; tapping opens the full schedule. After upgrading, remove and re-add the old widget.
5. Allow exact updates in Settings if Android offers the option. Check the next scheduled class transition and the manual **↻** button.
6. Check the widget after a reboot and a time zone change.

The schedule is static. Holidays and substitutions are not detected automatically.
