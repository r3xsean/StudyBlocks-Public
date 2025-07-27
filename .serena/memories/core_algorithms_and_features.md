# Core Algorithms and Features

## XP Calculation System
```kotlin
// Time-based scaling: 100 XP per hour base rate
val BASE_XP_PER_HOUR = 100
val totalScheduleXP = (totalScheduleTimeMinutes / 60.0 * BASE_XP_PER_HOUR)

// Equal distribution among subjects
val xpPerSubject = totalScheduleXP / totalSubjectCount
val subjectXpRate = xpPerSubject / totalSubjectTimeMinutes
val blockXP = (blockDurationMinutes * subjectXpRate).roundToInt()
```

## Confidence Weighting System
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

## Level Progression Formulas
```kotlin
// Subject level calculation
val xpForLevel = (100 * ((level - 1) * 1.5).pow(1.2)).toInt()

// Global level calculation  
val globalXpForLevel = (200 * ((level - 1) * 1.8).pow(1.3)).toInt()
```

## Daily Distribution Logic
- **Weekday capacity**: 3 blocks per day
- **Weekend capacity**: 2 blocks per day
- **Schedule horizon**: 30 days past to 60 days future
- **Block redistribution**: Missed blocks are intelligently rescheduled

## Key Features
- **AI-Powered Scheduling**: Confidence-based algorithm for optimal study distribution
- **Gamification**: XP system with subject and global leveling
- **Real-time Sync**: Firebase Firestore integration
- **Modern UI**: Material Design 3 with Jetpack Compose
- **Notifications**: WorkManager for study reminders
- **Analytics**: Interactive charts with Vico library