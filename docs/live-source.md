# College schedule data source

Checked on November 1, 2026. This research does not change the installed app's network behavior.

## Website and endpoint

Page: https://kmt.kemobl.ru/Studentu-fx3ifzdiool1km80bra3c7/Raspisanie-zanyatij-sds6lyy3puj6e7uocr20i3/

The embedded script defines `GAS_URL`, fetches the complete response with `fetch(GAS_URL, {cache: "no-store"})`, and parses `res.json()`.

Endpoint:

https://script.google.com/macros/s/AKfycbwW8yTxVGMvsklnIscEP87grbdxHkWv9-Dvztm00rjhM2xeNFqL-HRtnDZuldpfRVtI/exec

This is a public Google Apps Script web service. Group search runs in the browser against the text of rendered cards; it does not make another server request.

The service likely reads Google Sheets: its response contains `лист` (sheet), `дата` (date), and `данные` (data). Neither the Apps Script source nor an underlying spreadsheet URL appears in the inspected page fragment, so that internal source remains unconfirmed.

## Verified response

Two ordinary GET requests without cookies, login, or an API key returned HTTP 200 and JSON. The response contains six days, each with 87 group rows. Exactly one ИС-232 row was found for each day.

Each day has `лист` (weekday name), `дата` (a `DD.MM.YYYY` string or an empty string), and `данные` (an array of rows). Each row includes `Группа`, `1 пара` through `5 пара`, and `ауд1` through `ауд5`. Class fields primarily contain teacher names or `Нет занятия` (no class), rather than subject names.

At the time of inspection, only October 30, October 31, and November 2, 2026 had dates. Tuesday, Wednesday, and Thursday contained rows but had empty dates. The website does not create date buttons for those entries. Do not assign them an upcoming date based on the weekday name.

ИС-232 on **November 2, 2026**:

| Class | Source value | Classroom |
| --- | --- | --- |
| 1 | Нет занятия | Нет |
| 2 | Нет занятия | Нет |
| 3 | Бородина Е.Н. | 106 К1 |
| 4 | Давыдова Е.В. | Тр/З К1 |
| 5 | Нет занятия | Нет |

For October 31, all five ИС-232 classrooms contain `ДО` (remote learning), consistent with the user's clarification. The October 30 response includes a fifth class with Гладких, absent from the transcribed standard Friday schedule. This establishes that the feed differs from the base PDF; it does not guarantee that every substitution is complete or current.

## Integration requirements

- Fetch JSON over HTTPS with redirect handling. Match an exact date and the exact group value `ИС-232`.
- If there is no matching date or group row, show that live data is unavailable. Distinguish this from a published row where all classes are cancelled.
- Do not treat an empty date as today or reuse a past Friday for the next Friday.
- Cache the response and its schedule date. Show the time of the last successful fetch separately. The response has no explicit last-modified timestamp.
- Handle `Нет занятия`, empty classrooms, and `ДО` separately. Exclude Saturday remote classes from on-campus classes, as requested by the user.
- Do not infer subjects from a teacher's name alone: Бородина teaches multiple subjects. This source directly supplies class numbers, teachers, and classrooms.
- Online loading is not integrated into the current APK. Integration requires the INTERNET permission, a background request, JSON validation, and clear source/cache labels.

To repeat the check:

```powershell
python -X utf8 tools/probe_schedule.py --group ИС-232
```

The script only reads the public endpoint and prints rows for the selected group. It does not change the website or application. Its `published` flag means a date is present, matching the website's date-button logic; it is not an independent freshness guarantee.
