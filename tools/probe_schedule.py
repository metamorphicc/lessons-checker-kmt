"""Inspect the public schedule feed without changing the Android application."""
import argparse
import json
import urllib.request

URL = 'https://script.google.com/macros/s/AKfycbwW8yTxVGMvsklnIscEP87grbdxHkWv9-Dvztm00rjhM2xeNFqL-HRtnDZuldpfRVtI/exec'

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--group', default='ИС-232')
    args = parser.parse_args()
    request = urllib.request.Request(URL, headers={'Accept': 'application/json'})
    with urllib.request.urlopen(request, timeout=30) as response:
        payload = json.load(response)
        status = response.status
    if not isinstance(payload, list):
        raise ValueError('Expected a list of days')
    result = []
    for day in payload:
        rows = day.get('данные', [])
        matches = [row for row in rows if str(row.get('Группа', '')).strip().upper() == args.group.strip().upper()]
        result.append({
            'day': day.get('лист'),
            'date': day.get('дата'),
            'published': bool(str(day.get('дата') or '').strip()),
            'groupRows': matches,
        })
    print(json.dumps({'httpStatus': status, 'days': result}, ensure_ascii=False, indent=2))

if __name__ == '__main__':
    main()
