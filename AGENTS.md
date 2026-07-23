# Custom Agent Rules

## Git and Deployment Workflow
When completing a task or milestone, automatically:
1. Install the application on the local emulator or connected device (`./gradlew installDebug` or similar appropriate command). Do NOT automatically launch the application after installation unless explicitly asked by the user.
2. If the build and installation are successful, commit the changes to git. All git commit messages MUST be written in English.
3. Push the changes to the remote repository.

## ClaudWecho Wear OS UI Design Guidelines
When creating or updating pages with lists (`ScalingLazyColumn`), ALWAYS adhere to the following strict design standards to ensure pixel-perfect consistency across the app:

1. **Header Component**:
   - MUST use the shared `com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "...")`.
   - The overall page wrapper MUST be a `Box(modifier = Modifier.fillMaxSize())` with the `PinnedHeader` placed *after* the `ScalingLazyColumn` so it stays pinned at the top.

2. **ScalingLazyColumn Layout Parameters**:
   - `scalingParams`: Do not use custom `scalingParams` so it defaults to standard Wear OS scaling parameters (wider items, less edge shrinking).
   - `contentPadding`: MUST use exactly `PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)`.
   - `verticalArrangement`: MUST use exactly `Arrangement.spacedBy(6.dp)` for spacing between buttons.

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

6. **Progress Indicators**:
   - MUST use the native `androidx.wear.compose.material3.CircularProgressIndicator` instead of the non-wear one for indeterminate loading states, as this preserves the fluid shape-shifting spinner animation expected on Wear OS and correctly uses the Wear Material 3 theme colors. Do NOT use `modifier = Modifier.fillMaxSize()` on it unless you want to stretch it into a full-screen arc.

7. **Dialog Action Buttons**:
   - For bottom confirm/cancel actions (like in input fallback dialogs or settings), MUST use the shared `com.yorkyang2333.claudwecho.ui.components.DialogActionButtons` component.
   - This ensures the standard Wear OS Material 3 pattern (squircle dark cancel button on the left, circular primary confirm button on the right).

## Project Identity Memory
- This project is a third-party client for Netease Cloud Music, but you MUST NEVER write the explicit name of this service ("网易云", "网易云音乐", "Netease Cloud Music") in any documents (like README, commit messages, code comments). Always use "某知名音乐软件" (a well-known music software) instead to avoid legal/trademark issues.

## OPPO Watch Crown Adaptation Guidelines
OPPO Watch (ColorOS Watch) simulates generic Android mouse scroll events (`MotionEvent.ACTION_SCROLL` with `MotionEvent.AXIS_VSCROLL`) when rotating the crown, rather than Wear OS `onRotaryScrollEvent`.
1. **List Scrolling (`RotaryScalingLazyColumn`)**:
   - MUST attach `pointerInteropFilter` handling `MotionEvent.ACTION_SCROLL`.
   - Calculate pixel scroll using system `scaledVerticalScrollFactor`: `deltaPx = -vScroll * scrollFactor`.
2. **Progress Bar Seeking (`PlayerScreen`)**:
   - MUST support both Wear OS (`onRotaryScrollEvent`) and OPPO Watch (`pointerInteropFilter` `AXIS_VSCROLL`).
   - For OPPO Watch crown seeking, MUST accumulate `vScroll` across continuous events (`accumulatedOppoScroll`) with a threshold (`threshold = 3.5f`) and reset after 300ms inactivity to avoid over-sensitivity from accidental touches.
   - When accumulated scroll exceeds threshold, adjust progress in discrete steps of `1000ms` (`1s`) per step.
