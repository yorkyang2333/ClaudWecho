import os

path = 'app/src/main/java/com/yorkyang2333/claudwecho/ui/about/AboutScreen.kt'

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports if they don't exist
if 'import androidx.compose.ui.draw.clip' not in content:
    content = content.replace('import androidx.compose.ui.Alignment', 'import androidx.compose.ui.Alignment\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.ui.text.style.TextOverflow')

content = content.replace('.androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)', '.clip(CircleShape)')
content = content.replace('androidx.compose.ui.text.style.TextOverflow.Ellipsis', 'TextOverflow.Ellipsis')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"Fixed {path}")

