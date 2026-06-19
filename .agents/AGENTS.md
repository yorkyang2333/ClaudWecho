# Custom Agent Rules

## Git and Deployment Workflow
When completing a task or milestone, automatically:
1. Install the application on the local emulator (`./gradlew installDebug` or similar appropriate command).
2. If the build and installation are successful, commit the changes to git.
3. Push the changes to the remote repository.
4. Launch the application on the emulator so the user can immediately see the changes.
