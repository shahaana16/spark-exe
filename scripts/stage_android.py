"""Build-time staging. Converts the existing GitHub Pages source to bundled offline APK assets."""
from pathlib import Path
import shutil

root = Path(__file__).resolve().parent.parent
assets = root / 'android' / 'app' / 'src' / 'main' / 'assets'
assets.mkdir(parents=True, exist_ok=True)
for name in ('index.html', 'app.js', 'styles.css', 'manifest.webmanifest', 'sw.js'):
    if not (root / name).is_file():
        raise FileNotFoundError(f'Repository root needs {name}. See README_BUILD_APPS.md')
    shutil.copyfile(root / name, assets / name)

icons = assets / 'icons'
icons.mkdir(exist_ok=True)
for name in ('icon-192.png', 'icon-512.png'):
    shutil.copyfile(root / 'icons' / name, icons / name)

# A WebView cannot reliably download blob: URLs. Instead use Android's document
# save dialog when the user presses the app's own Export backup button.
js_file = assets / 'app.js'
s = js_file.read_text(encoding='utf-8')
needle = "const json=JSON.stringify({...state,exportedAt:new Date().toISOString()},null,2);"
injection = ("if(window.SparkAndroid && typeof window.SparkAndroid.exportBackup==='function'){"
             "window.SparkAndroid.exportBackup(json,`spark-exe-backup-${dayKey()}.json`);return;}")
if s.count(needle) != 1:
    raise RuntimeError('Unexpected backup export source; update staging before building')
s = s.replace(needle, needle + injection, 1)
# Assets are already bundled with the app, so no network service-worker is needed.
s = s.replace("if('serviceWorker' in navigator&&['http:','https:'].includes(location.protocol))",
              "if(!window.SparkAndroid&&'serviceWorker' in navigator&&['http:','https:'].includes(location.protocol))")
js_file.write_text(s, encoding='utf-8')

(root / 'android' / 'app' / 'src' / 'main' / 'res' / 'drawable').mkdir(parents=True, exist_ok=True)
  
shutil.copyfile(root / 'icons' / 'icon-512.png',
                root / 'android' / 'app' / 'src' / 'main' / 'res' / 'drawable' / 'spark_icon.png')
print('Android offline assets prepared; backup export wired to Android file save dialog.')
