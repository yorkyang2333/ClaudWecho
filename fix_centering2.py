import os

directory = 'app/src/main/java/com/example/claudwecho/ui/'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            if 'autoCentering = androidx.wear.compose.foundation.lazy.AutoCenteringParams(itemIndex = 1),' in content:
                content = content.replace(
                    'autoCentering = androidx.wear.compose.foundation.lazy.AutoCenteringParams(itemIndex = 1),',
                    'autoCentering = null,'
                )
                with open(path, 'w') as f:
                    f.write(content)
                print(f"Fixed {path}")
