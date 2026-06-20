# Custom Agent Rules

## Git and Deployment Workflow
When completing a task or milestone, automatically:
1. Install the application on the local emulator (`./gradlew installDebug` or similar appropriate command).
2. If the build and installation are successful, commit the changes to git.
3. Push the changes to the remote repository.
4. Launch the application on the emulator so the user can immediately see the changes.

## ClaudWecho Wear OS UI Design Guidelines
When creating or updating pages with lists (`ScalingLazyColumn`), ALWAYS adhere to the following strict design standards to ensure pixel-perfect consistency across the app:

1. **Header Component**:
   - MUST use the shared `com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "...")`.
   - The overall page wrapper MUST be a `Box(modifier = Modifier.fillMaxSize())` with the `PinnedHeader` placed *after* the `ScalingLazyColumn` so it stays pinned at the top.

2. **ScalingLazyColumn Layout Parameters**:
   - `scalingParams`: MUST use `ScalingLazyColumnDefaults.scalingParams(edgeScale = 0.3f, minTransitionArea = 0.4f)` to ensure strong edge scaling/shrinking.
   - `contentPadding`: MUST use exactly `PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)`.
   - `verticalArrangement`: MUST use exactly `Arrangement.spacedBy(2.dp)` for spacing between buttons.

3. **Top Spacer**:
   - The first `item` in the `ScalingLazyColumn` MUST be a `Spacer(modifier = Modifier.height(48.dp))`. This prevents the first button from hiding behind the PinnedHeader.

4. **Buttons**:
   - Buttons MUST use `colors = ButtonDefaults.filledTonalButtonColors()` for consistent background styling.
   - Icons MUST use the primary theme color: `tint = MaterialTheme.colorScheme.primary`.
   - The label `Text` inside the Button MUST use `style = MaterialTheme.typography.titleMedium`, `maxLines = 1`, and `overflow = TextOverflow.Ellipsis`.

5. **PinnedHeader Action Icons**:
   - When providing an `actionIcon` to `PinnedHeader`, do NOT use `CompactButton` or other components with large default padding/height. This will inflate the header height and cover the list items below.
   - Instead, use a lightweight clickable `Box` to contain the icon. Example:
     ```kotlin
     Box(
         modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF2D2D2D)).clickable { /* ... */ },
         contentAlignment = Alignment.Center
     ) {
         Icon(imageVector = ..., modifier = Modifier.size(18.dp), tint = Color.White)
     }
     ```

6. **Error Color**: ALWAYS use `Color(0xFFC64545)` for error states, text, and icons, as defined in the brand guidelines.
