import os
import re

directory = 'app/src/main/java/com/yorkyang2333/claudwecho/ui'

pattern = re.compile(r'androidx\.compose\.material3\.CircularProgressIndicator\(')

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if pattern.search(content):
                new_content = pattern.sub(r'androidx.wear.compose.material3.CircularProgressIndicator(', content)
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Fixed {path}")

