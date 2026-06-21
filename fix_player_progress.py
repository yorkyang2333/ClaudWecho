import os
import re

path = 'app/src/main/java/com/yorkyang2333/claudwecho/ui/player/PlayerScreen.kt'

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('indicatorColor = MaterialTheme.colorScheme.primary', 'color = MaterialTheme.colorScheme.primary')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"Fixed {path}")

