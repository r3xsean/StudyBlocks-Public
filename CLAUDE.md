# StudyBlocks - AI-Powered Study Schedule Management

## Overview

StudyBlocks is a comprehensive Android application that helps students organize their study time using AI-generated study blocks distributed over multiple days based on spaced repetition principles and confidence-weighted algorithms.

## Key Features

### 🤖 AI-Powered Scheduling
- **Spaced Repetition Algorithm**: Uses the Ebbinghaus forgetting curve to optimize learning retention
- **Confidence Weighting**: Adjusts study frequency based on subject confidence levels (1-10 scale)
- **Smart Distribution**: Automatically spreads study blocks across multiple days for optimal learning

### 📱 Modern Android Features
- **Material Design 3**: Modern UI with dynamic theming and dark mode support
- **Edge-to-edge Display**: Immersive full-screen experience
- **Smooth Animations**: Polished transitions and micro-interactions
- **Cross-device Sync**: Firebase-powered data synchronization

### 🎯 Core Functionality

#### 📅 Today Tab
- Week view with scrollable date picker
- Study block cards with completion tracking
- Progress visualization and statistics
- Smooth animations for block completion

#### 📚 Subjects Tab
- Subject management with CRUD operations
- Confidence rating system (1-10 scale)
- Icon selection with auto-suggestions
- Schedule regeneration functionality

#### 📊 Analytics Tab
- Progress tracking with interactive charts
- Subject breakdown analysis
- Study streak calculations
- Performance insights and trends

#### ⏱️ Timer Tab
- Pomodoro timer with customizable durations
- Focus scoring system (1-10 scale)
- Break management (short/long breaks)
- Session tracking and statistics

#### 👤 Profile Tab
- User account management
- Theme preferences (Light/Dark/System)
- Notification settings
- Data export and synchronization

## Technical Architecture

### 🏗️ Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Clean separation of concerns
- **Repository Pattern**: Centralized data access layer
- **Dependency Injection**: Hilt for dependency management

### 🛠️ Technology Stack
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room with SQLite
- **Authentication**: Firebase Auth with Google Sign-In
- **Cloud Storage**: Firebase Firestore for cross-device sync
- **Navigation**: Jetpack Navigation Compose
- **Reactive Programming**: Kotlin Flows and StateFlow

### 🧮 Algorithms

#### Spaced Repetition Formula
- **Initial Interval**: 1 day
- **Subsequent Intervals**: Previous interval × (2.5 + (confidence/10))
- **Maximum Interval**: 30 days

#### Confidence Weighting
```kotlin
Block Frequency = Base Frequency × (11 - Confidence Level) / 10
```

#### Exam Proximity Urgency
```kotlin
Urgency Multiplier = max(1.0, 5.0 / days_until_exam)
```

## Development Commands

### Building the Project
```bash
./gradlew clean build
```

### Running Tests
```bash
./gradlew test
```

### Running on Device/Emulator
```bash
./gradlew installDebug
```

## Configuration

### Firebase Setup
1. Create a new Firebase project
2. Add Android app with package name: `com.example.studyblocks`
3. Download `google-services.json` to `app/` directory
4. Enable Authentication and Firestore in Firebase Console

### Development Environment
- **Android Studio**: Arctic Fox or later
- **Compile SDK**: 34
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34
- **Kotlin**: 1.9.10

## File Structure

```
app/src/main/java/com/example/studyblocks/
├── auth/                    # Authentication logic
├── data/
│   ├── local/              # Room database components
│   └── model/              # Data models and entities
├── navigation/             # Navigation components
├── repository/             # Data access layer
├── scheduling/             # AI scheduling algorithms
├── sync/                   # Firebase synchronization
├── ui/
│   ├── animations/         # Custom animations
│   ├── components/         # Reusable UI components
│   ├── screens/            # Screen composables
│   └── theme/              # Material Design theming
└── MainActivity.kt         # Entry point
```

## AI Scheduling Logic

The core scheduling algorithm considers multiple factors:

1. **Subject Confidence**: Lower confidence subjects get more frequent study blocks
2. **Spaced Repetition**: Intervals increase based on successful completions
3. **Exam Proximity**: Subjects with approaching exams get priority
4. **Daily Distribution**: Blocks are evenly distributed across days
5. **Duration Preferences**: Respects user-set block durations per subject

## Data Models

### Subject
- Confidence level (1-10)
- Exam date
- Custom block duration
- Icon and color customization

### StudyBlock
- Scheduled date and time
- Duration and completion status
- Spaced repetition interval
- Associated study sessions

### StudySession
- Timer tracking with start/end times
- Focus score rating (1-10)
- Break tracking
- Performance metrics

## Performance Optimizations

- **Lazy Loading**: Efficient list rendering with LazyColumn/LazyRow
- **State Management**: Optimized state flow and composition
- **Database Indexing**: Proper Room database indexes for queries
- **Image Optimization**: Vector drawables for scalable icons
- **Animation Performance**: Hardware-accelerated transitions

## Accessibility Features

- **Content Descriptions**: Comprehensive screen reader support
- **Touch Targets**: Minimum 48dp touch targets
- **Color Contrast**: WCAG AA compliant color schemes
- **Dynamic Type**: Supports system font size preferences

## Security & Privacy

- **Data Encryption**: Firebase security rules and encryption
- **Local Storage**: Secure Room database storage
- **Authentication**: Firebase Auth with secure token management
- **Privacy Controls**: User data export and deletion options

## Future Enhancements

- [ ] Machine learning for personalized study recommendations
- [ ] Calendar integration for automatic scheduling
- [ ] Study group collaboration features
- [ ] Advanced analytics with predictive insights
- [ ] Offline mode with sync when online
- [ ] Widget support for quick access
- [ ] Wear OS companion app

---

## Generated Information

🤖 **Generated with [Claude Code](https://claude.ai/code)**

This application was developed using AI assistance from Claude, incorporating modern Android development best practices, Material Design 3 principles, and advanced algorithms for optimal study scheduling.

**Co-Authored-By**: Claude <noreply@anthropic.com>

---

*StudyBlocks v1.0.0 - AI-Powered Study Block Scheduler*