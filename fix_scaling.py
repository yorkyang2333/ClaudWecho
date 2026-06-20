import os
import glob

directory = 'app/src/main/java/com/example/claudwecho/ui'
for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            modified = False
            if '72.dp' in content:
                content = content.replace('72.dp', '48.dp')
                modified = True
            
            if 'ScalingLazyColumn(' in content and 'scalingParams = ' not in content:
                content = content.replace(
                    'ScalingLazyColumn(\n',
                    'ScalingLazyColumn(\n            scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(\n                edgeScale = 0.3f,\n                minTransitionArea = 0.4f\n            ),\n'
                )
                modified = True
                
            if modified:
                with open(filepath, 'w') as f:
                    f.write(content)
                print(f"Updated {filepath}")
