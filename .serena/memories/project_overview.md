# StudyBlocks Project Overview

## Purpose
StudyBlocks is a comprehensive Android application that helps students organize their study time using AI-generated study blocks distributed over multiple days. It features a sophisticated XP (Experience Points) gamification system with leveling mechanics based on confidence-weighted algorithms.

## Key Features
- **AI-Powered Scheduling**: Confidence-based distribution that adjusts study frequency based on subject confidence levels (1-10 scale)
- **Smart Daily Distribution**: Automatically spreads study blocks across weekdays (3 blocks) and weekends (2 blocks)
- **Gamification System**: Time-based XP rewards with scaling 100 XP per hour base rate and individual subject progression
- **Cross-device Sync**: Firebase-powered data synchronization
- **Material Design 3**: Modern UI with dynamic theming and extended icon support

## Tech Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room with SQLite
- **Authentication**: Firebase Auth with Google Sign-In
- **Cloud Storage**: Firebase Firestore
- **Architecture**: MVVM with Repository Pattern
- **Dependency Injection**: Hilt
- **Build System**: Kotlin DSL with version catalogs
- **Charts**: Vico (modern charts library)
- **Background Work**: WorkManager with Hilt integration

## Architecture
- **MVVM Pattern**: Clean separation with StateFlow and reactive data binding
- **Repository Pattern**: Centralized data access with caching and offline support
- **Reactive Architecture**: Kotlin Flows for real-time data updates