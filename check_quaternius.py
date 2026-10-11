import urllib.request, re
url = 'https://quaternius.com/packs/ultimatemodularcharacters.html'
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req) as resp:
        html = resp.read().decode('utf-8', errors='ignore')
        for m in re.finditer(r'href=["\']([^"\']+)["\']', html):
            link = m.group(1)
            if 'drive' in link or 'mega' in link or 'download' in link or '.zip' in link or '.7z' in link:
                print('Found link:', link)
except Exception as e:
    print('Error:', e)
