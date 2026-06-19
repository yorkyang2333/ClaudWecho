import os

directory = 'app/src/main/java/com/example/claudwecho/ui/'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            if 'ScalingLazyColumn(' in content and 'autoCentering' not in content:
                content = content.replace(
                    'ScalingLazyColumn(',
                    'ScalingLazyColumn(\n        autoCentering = androidx.wear.compose.foundation.lazy.AutoCenteringParams(itemIndex = 1),'
                )
                with open(path, 'w') as f:
                    f.write(content)
                print(f"Fixed {path}")
