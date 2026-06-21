import os

directory = 'app/src/main/java/com/yorkyang2333/claudwecho/ui'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Replace parameterless CircularProgressIndicator with one that has Modifier.fillMaxSize()
            content = content.replace("androidx.wear.compose.material3.CircularProgressIndicator()", "androidx.wear.compose.material3.CircularProgressIndicator(modifier = Modifier.fillMaxSize())")
            
            with open(path, 'w') as f:
                f.write(content)

