import os

path = 'app/src/main/java/com/yorkyang2333/claudwecho/ui/player/PlayerScreen.kt'

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Revert to wear material CircularProgressIndicator which supports indicatorColor, trackColor and progress as Float
content = content.replace('androidx.compose.material3.CircularProgressIndicator', 'androidx.wear.compose.material.CircularProgressIndicator')
content = content.replace('androidx.wear.compose.material3.CircularProgressIndicator', 'androidx.wear.compose.material.CircularProgressIndicator')
content = content.replace('color = MaterialTheme.colorScheme.primary', 'indicatorColor = MaterialTheme.colorScheme.primary')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"Fixed {path}")

