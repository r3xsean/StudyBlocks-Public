# Tech Stack and Dependencies

## Core Android
- **Compile SDK**: 36
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Kotlin**: 2.0.21
- **AGP**: 8.11.1
- **Java Version**: 11

## Key Dependencies
- **Jetpack Compose**: Modern declarative UI framework
- **Material Design 3**: Latest Material Design components
- **Navigation Compose**: Type-safe navigation
- **Jetpack ViewModel**: Lifecycle-aware data holders
- **Room**: Local database with SQLite
- **Firebase BOM**: 33.6.0
  - Firebase Auth
  - Firebase Firestore
  - Google Play Services Auth
- **Hilt**: 2.52 - Dependency injection
- **DataStore Preferences**: Modern SharedPreferences replacement
- **Vico Charts**: 1.13.1 - Modern charting library
- **Coroutines**: 1.9.0 - Asynchronous programming
- **WorkManager**: Background task scheduling
- **KSP**: 2.0.21-1.0.28 - Kotlin Symbol Processing

## Project Structure
```
app/src/main/java/com/example/studyblocks/
├── auth/                    # Firebase authentication
├── data/
│   ├── local/              # Room database, DAOs
│   └── model/              # Data models, entities
├── di/                     # Hilt dependency injection
├── navigation/             # Navigation components
├── notifications/          # WorkManager notifications
├── repository/             # Data access layer
├── scheduling/             # Scheduling algorithms
├── sync/                   # Firebase synchronization
├── ui/
│   ├── components/         # Reusable UI components
│   ├── screens/            # Screen composables
│   └── theme/              # Material Design 3 theming
├── MainActivity.kt
└── StudyBlocksApplication.kt
```