import os
import re

directory = 'app/src/main/java/com/yorkyang2333/claudwecho/ui'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Match any leftover parameters from the previous broken regex
            # e.g., androidx.wear.compose.material3.CircularProgressIndicator(), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
            content = re.sub(
                r'androidx\.wear\.compose\.material3\.CircularProgressIndicator\(\)(?:[^)]+)\)',
                'androidx.wear.compose.material3.CircularProgressIndicator()',
                content
            )
            
            with open(path, 'w') as f:
                f.write(content)

