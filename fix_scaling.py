import os
import re

directory = 'app/src/main/java/com/yorkyang2333/claudwecho/ui'
count = 0

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            original_content = content
            
            # Remove scalingParams block
            content = re.sub(r'\s*scalingParams\s*=\s*androidx\.wear\.compose\.foundation\.lazy\.ScalingLazyColumnDefaults\.scalingParams\s*\([^)]*\),?', '', content)
            
            # Adjust verticalArrangement
            content = content.replace("Arrangement.spacedBy(2.dp)", "Arrangement.spacedBy(6.dp)")

            if content != original_content:
                with open(path, 'w') as f:
                    f.write(content)
                count += 1
                print(f"Updated {path}")

print(f"Updated {count} files.")
