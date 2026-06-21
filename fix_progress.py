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
            
            # Replace compose material3 with wear compose material3
            content = content.replace("import androidx.compose.material3.CircularProgressIndicator", "import androidx.wear.compose.material3.CircularProgressIndicator")
            
            # Replace the specific usage pattern
            # Pattern: androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
            content = re.sub(
                r'androidx\.compose\.material3\.CircularProgressIndicator\([^)]+\)',
                'androidx.wear.compose.material3.CircularProgressIndicator()',
                content
            )
            
            # Replace if it was imported directly
            # CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
            content = re.sub(
                r'CircularProgressIndicator\(\s*modifier\s*=\s*Modifier\.size\([^)]+\),\s*strokeWidth\s*=[^,]+,\s*color\s*=[^)]+\s*\)',
                'CircularProgressIndicator()',
                content
            )
            
            if content != original_content:
                with open(path, 'w') as f:
                    f.write(content)
                count += 1
                print(f"Updated {path}")

print(f"Updated {count} files.")
