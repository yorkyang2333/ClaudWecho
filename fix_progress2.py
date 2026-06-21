import os
import re

directory = 'app/src/main/java/com/yorkyang2333/claudwecho/ui'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Fix broken syntax: androidx.wear.compose.material3.CircularProgressIndicator().dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
            content = content.replace("androidx.wear.compose.material3.CircularProgressIndicator().dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)", "androidx.wear.compose.material3.CircularProgressIndicator()")
            
            with open(path, 'w') as f:
                f.write(content)

