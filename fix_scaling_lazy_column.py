import os
import re

directory = 'app/src/main/java/com/yorkyang2333/claudwecho/ui'

import_pattern = re.compile(r'^import\s+androidx\.wear\.compose\.foundation\.lazy\.ScalingLazyColumn$', re.MULTILINE)
usage_pattern = re.compile(r'\bScalingLazyColumn\s*\(')

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt') and file != 'RotaryScalingLazyColumn.kt':
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if 'ScalingLazyColumn' in content:
                # Replace import
                new_content = import_pattern.sub('import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn', content)
                
                # If there are ScalingLazyColumn usages (excluding imports or defaults), replace them
                new_content = usage_pattern.sub('RotaryScalingLazyColumn(', new_content)
                
                if new_content != content:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f"Updated {path}")

