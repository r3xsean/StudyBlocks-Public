# Suggested Commands for StudyBlocks Development

## Windows-Specific Commands
Since this is a Windows environment, use these commands:

### Basic File Operations
- `dir` - List directory contents (instead of `ls`)
- `cd <directory>` - Change directory
- `type <filename>` - Display file contents (instead of `cat`)
- `findstr <pattern> <files>` - Search in files (instead of `grep`)

### Git Commands
- `git status` - Check repository status
- `git add .` - Stage all changes
- `git commit -m "message"` - Commit changes
- `git pull` - Pull latest changes
- `git push` - Push changes to remote

### Android/Gradle Commands
- `.\gradlew clean` - Clean build artifacts
- `.\gradlew build` - Build the project
- `.\gradlew assembleDebug` - Build debug APK
- `.\gradlew installDebug` - Install debug APK on connected device
- `.\gradlew test` - Run unit tests
- `.\gradlew connectedAndroidTest` - Run instrumentation tests

### Development Workflow Commands
- `.\gradlew ktlintCheck` - Check Kotlin code style (if configured)
- `.\gradlew ktlintFormat` - Format Kotlin code (if configured)
- `.\gradlew detekt` - Run static code analysis (if configured)

### Firebase Commands
- `firebase login` - Login to Firebase CLI
- `firebase use <project-id>` - Switch Firebase project
- `firebase deploy` - Deploy Firebase functions/rules

### Useful Android Debug Commands
- `adb devices` - List connected Android devices
- `adb logcat` - View device logs
- `adb install <apk-path>` - Install APK manually
- `adb uninstall com.example.studyblocks` - Uninstall the app

## Development Setup
1. Ensure Android Studio is installed with latest SDK
2. Set up Firebase project and download `google-services.json` to `app/` directory
3. Configure Google Sign-In credentials in Firebase Console
4. Enable Firestore and Authentication in Firebase Console