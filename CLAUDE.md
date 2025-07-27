# StudyBlocks - AI-Powered Study Schedule Management

## Overview

StudyBlocks is a comprehensive Android application that helps students organize their study time using AI-generated study blocks distributed over multiple days based on confidence-weighted algorithms. The app features a sophisticated XP (Experience Points) gamification system with leveling mechanics for enhanced user engagement.

## Key Features

### 🤖 AI-Powered Scheduling
- **Confidence-Based Distribution**: Adjusts study frequency based on subject confidence levels (1-10 scale) with exponential weighting
- **Smart Daily Distribution**: Automatically spreads study blocks across weekdays (3 blocks) and weekends (2 blocks)
- **Missed Block Rescheduling**: Intelligently redistributes overdue blocks across future dates
- **Custom Block Support**: Allows users to add custom study blocks to any date

### 🎮 Gamification System
- **XP System**: Time-based XP rewards with scaling 100 XP per hour base rate and equal subject distribution
- **Subject Leveling**: Individual subject progression with exponential XP requirements
- **Global Level**: Overall user level calculated from all subject XP combined
- **Confidence Weighting**: XP multipliers based on subject confidence (4.5x for confidence 1, 0.4x for confidence 10)
- **Animated XP Feedback**: Visual XP gain/loss animations with tap-based positioning and cooldown protection

### 📱 Modern Android Features
- **Material Design 3**: Modern UI with dynamic theming and extended icon support
- **Jetpack Compose**: Fully declarative UI with smooth animations and state management
- **Edge-to-edge Display**: Immersive full-screen experience
- **Local Data Storage**: Room database with offline-first architecture (Cloud sync removed for open source)

### 🎯 Core Functionality

#### 📅 Today Tab
- **Extended Date Picker**: Scrollable timeline (30 days past to 60 days future)
- **Block Status Management**: Available, pending, completed, and overdue states with visual indicators
- **Real-time Statistics**: Completion percentages and progress tracking with live updates
- **Interactive XP Animations**: Tap-based XP feedback with cooldown protection (300ms)
- **Custom Block Creation**: Add study blocks for any subject on any date
- **Missed Block Detection**: Automatic identification and rescheduling of overdue blocks

#### 📚 Subjects Tab
- **CRUD Operations**: Complete subject management with input validation
- **Confidence Rating**: 1-10 scale with exponential scheduling impact visualization
- **XP and Level Display**: Real-time progression tracking with level progress bars
- **Custom Block Durations**: 15-120 minute range per subject with validation
- **Schedule Regeneration**: Complete schedule rebuild with block redistribution

#### 📊 Analytics Tab
- **Progress Tracking**: Interactive charts with MPAndroidChart integration
- **Subject Breakdown**: Individual subject analysis with XP and completion metrics
- **XP Statistics**: Level progression tracking and XP distribution visualization
- **Historical Data**: Long-term progress trends and performance insights

#### ⏱️ Timer Tab
- **Study Sessions**: Pomodoro-style timer with customizable durations
- **Focus Scoring**: 1-10 scale session quality rating with persistence
- **Break Management**: Structured break intervals with automatic transitions
- **Session History**: Comprehensive tracking with Room database persistence

#### 👤 Profile Tab
- **Authentication**: Email/password authentication (Firebase removed for open source)
- **Global Statistics**: Overall XP, level, and achievement tracking
- **Theme Settings**: Light/Dark/System mode preferences with Material You support
- **Data Management**: Local data export and account settings

## Technical Architecture

### 🏗️ Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Clean separation with StateFlow and reactive data binding
- **Repository Pattern**: Centralized data access with caching and offline support
- **Dependency Injection**: Hilt for modular dependency management with scoped providers
- **Reactive Architecture**: Kotlin Flows for real-time data updates and state management

### 🛠️ Technology Stack
- **UI Framework**: Jetpack Compose with Material Design 3 and extended icons
- **Database**: Room with SQLite, type converters, and optimized queries
- **Authentication**: Local authentication (Firebase dependencies removed)
- **Navigation**: Jetpack Navigation Compose with type safety
- **Reactive Programming**: Kotlin Flows, StateFlow, and Coroutines
- **Background Work**: WorkManager with Hilt integration for notifications
- **Charts**: Vico charts for analytics visualization
- **Build System**: Kotlin DSL with version catalogs

### 🧮 XP and Scheduling Algorithms

#### XP Calculation System
```kotlin
// Time-based scaling: 100 XP per hour base rate
val BASE_XP_PER_HOUR = 100
val totalScheduleXP = (totalScheduleTimeMinutes / 60.0 * BASE_XP_PER_HOUR)

// Equal distribution among subjects
val xpPerSubject = totalScheduleXP / totalSubjectCount
val subjectXpRate = xpPerSubject / totalSubjectTimeMinutes
val blockXP = (blockDurationMinutes * subjectXpRate).roundToInt()

// Custom blocks: 100 XP per hour
val customXP = (durationMinutes / 60.0 * BASE_XP_PER_HOUR).roundToInt()
```

#### Confidence Weighting System
```kotlin
val confidenceWeight = when (confidence) {
    1 -> 4.5   // Very low confidence gets 4.5x blocks
    2 -> 3.7   // Low confidence gets 3.7x blocks
    3 -> 2.8   // Still struggling gets 2.8x blocks
    4 -> 2.3   // Below average gets 2.3x blocks
    5 -> 1.9   // Average gets 1.9x blocks
    6 -> 1.4   // Above average gets 1.4x blocks
    7 -> 1.0   // Good confidence gets normal blocks
    8 -> 0.8   // Very good gets 0.8x blocks
    9 -> 0.6   // Excellent gets 0.6x blocks
    10 -> 0.4  // Perfect confidence gets 0.4x blocks
}
```

#### Level Progression Formula
```kotlin
// Subject level calculation
val xpForLevel = (100 * ((level - 1) * 1.5).pow(1.2)).toInt()

// Global level calculation  
val globalXpForLevel = (200 * ((level - 1) * 1.8).pow(1.3)).toInt()
```

#### Daily Distribution Logic
```kotlin
// Weekday capacity: 3 blocks per day
// Weekend capacity: 2 blocks per day
val dailyCapacity = when (date.dayOfWeek) {
    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> blocksPerWeekend
    else -> blocksPerWeekday
}
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

### Authentication Note
**Note for Open Source Version**: Firebase authentication and Google Sign-In have been removed from this version to eliminate API key dependencies. The app currently uses email/password authentication through Firebase Auth, but the Firebase dependencies are commented out in the build files. You can either:
1. Re-enable Firebase by uncommenting the dependencies and adding your own `google-services.json`
2. Implement an alternative authentication backend
3. Use the app in offline-only mode (requires removing authentication screens)

### Development Environment
- **Android Studio**: Latest stable version
- **Compile SDK**: 36
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Kotlin**: 2.0.21
- **Android Gradle Plugin**: 8.11.1
- **KSP**: 2.0.21-1.0.28 for annotation processing

## File Structure

```
app/src/main/java/com/example/studyblocks/
├── auth/                    # Firebase authentication logic
├── data/
│   ├── local/              # Room database, DAOs, converters
│   └── model/              # Data models, entities, XPManager
├── di/                     # Hilt dependency injection modules
├── navigation/             # Navigation components and routing
├── notifications/          # WorkManager and notification service
├── repository/             # Data access layer with caching
├── scheduling/             # Scheduling algorithms and logic
├── sync/                   # Firebase synchronization
├── ui/
│   ├── animations/         # Custom animations and transitions
│   ├── components/         # Reusable UI components
│   ├── screens/            # Screen composables (today, subjects, etc.)
│   └── theme/              # Material Design 3 theming
├── MainActivity.kt         # Entry point
└── StudyBlocksApplication.kt # Application class with Hilt
```

## Scheduling Logic

The core scheduling algorithm focuses on confidence-based distribution:

1. **Confidence Weighting**: Lower confidence subjects receive exponentially more study blocks
2. **Time-Based XP**: XP rewards based on actual time invested, not block count
3. **Daily Capacity**: Respects weekday (3 blocks) and weekend (2 blocks) preferences
4. **Block Redistribution**: Missed blocks are intelligently rescheduled across future dates
5. **Custom Integration**: User-added blocks seamlessly integrate with generated schedules

## Data Models

### Subject
- Confidence level (1-10) with validation
- XP and level tracking with exponential progression
- Custom block duration (15-120 minutes)
- Icon selection from extended Material icons
- Created/updated timestamps with user association

### StudyBlock
- Scheduled date with completion status tracking
- Duration and block numbering within subject
- Custom block flag for user-added blocks
- XP calculation integration
- Overdue detection with automatic rescheduling

### StudySession
- Timer tracking with start/end timestamps
- Focus score rating (1-10) with persistence
- Break management and session analytics
- Integration with subject XP system

### User
- Global XP and level tracking
- Onboarding completion status
- Firebase authentication integration
- Sync timestamps for conflict resolution

### XPManager
- Time-based XP calculation (100 XP/hour base)
- Subject and global level progression
- Confidence-weighted XP multipliers
- Custom block XP integration

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

## Current XP System Features

- ✅ **Time-Based XP**: Scaling 100 XP per hour with schedule length
- ✅ **Equal Subject XP**: All subjects receive equal XP distribution regardless of confidence
- ✅ **Precision Rounding**: Proper rounding eliminates XP loss from truncation
- ✅ **Confidence Multipliers**: XP adjusted by confidence levels (0.4x to 4.5x)
- ✅ **Custom Block Integration**: Custom blocks use same XP rate as scheduled blocks
- ✅ **Level Progression**: Exponential XP requirements for meaningful progression
- ✅ **Visual Feedback**: Animated XP gains with tap-based positioning
- ✅ **Global Tracking**: Combined XP from all subjects for overall progression

## Future Enhancements

- [ ] Achievement system with milestone rewards
- [ ] Calendar integration for automatic scheduling
- [ ] Study group collaboration features
- [ ] Advanced analytics with predictive insights
- [ ] Offline mode with enhanced sync capabilities
- [ ] Widget support for quick access
- [ ] Wear OS companion app
- [ ] Machine learning for personalized recommendations

---

## Generated Information

🤖 **Generated with [Claude Code](https://claude.ai/code)**

This application was developed using AI assistance from Claude, incorporating modern Android development best practices, Material Design 3 principles, and advanced algorithms for optimal study scheduling.

**Co-Authored-By**: Claude <noreply@anthropic.com>

---

*StudyBlocks v1.0.0 - AI-Powered Study Block Scheduler*