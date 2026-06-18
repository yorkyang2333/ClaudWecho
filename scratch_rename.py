import os

replacements = {
    'yesplaymusicwear': 'claudwecho',
    'YesPlayMusicWear': 'ClaudWecho',
    'YesPlayMusic Wear': 'ClaudWecho',
    'yesplaymusic-wear': 'ClaudWecho'
}

for root, dirs, files in os.walk('.'):
    if '.git' in root or '.gradle' in root or 'build' in root:
        continue
    for file in files:
        if file.endswith('.py') or file.endswith('.jar'):
            continue
        filepath = os.path.join(root, file)
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            new_content = content
            for k, v in replacements.items():
                new_content = new_content.replace(k, v)
            
            # Also fix the HttpUrl error while we're at it
            if 'PersistentCookieJar.kt' in file:
                new_content = new_content.replace(
                    'HttpUrl.parse("http://$key")!!',
                    'import okhttp3.HttpUrl.Companion.toHttpUrlOrNull\n                "http://$key".toHttpUrlOrNull()!!'
                )

            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
        except Exception as e:
            pass

# Rename directories
def rename_dirs(path):
    for root, dirs, files in os.walk(path, topdown=False):
        for d in dirs:
            if d == 'yesplaymusicwear':
                old_dir = os.path.join(root, d)
                new_dir = os.path.join(root, 'claudwecho')
                os.rename(old_dir, new_dir)
                print(f"Renamed {old_dir} to {new_dir}")

rename_dirs('.')
