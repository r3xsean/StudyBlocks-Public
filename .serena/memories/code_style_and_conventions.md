# Code Style and Conventions

## Kotlin Conventions
- **Package Naming**: `com.example.studyblocks` with domain-based subpackages
- **Class Naming**: PascalCase (e.g., `StudyBlocksApplication`, `AuthViewModel`)
- **Function Naming**: camelCase (e.g., `onCreate`, `collectAsState`)
- **Variable Naming**: camelCase (e.g., `authState`, `isFirstTimeLogin`)
- **Constant Naming**: UPPER_SNAKE_CASE for top-level constants

## File Organization
- **ViewModels**: End with `ViewModel` suffix (e.g., `AuthViewModel`)
- **Screens**: End with `Screen` suffix (e.g., `TodayScreen`)
- **Repositories**: End with `Repository` suffix (e.g., `StudyRepository`)
- **DAOs**: End with `Dao` suffix (e.g., `SubjectDao`)
- **Entities**: Plain class names (e.g., `Subject`, `StudyBlock`)

## Compose Conventions
- **Composable Functions**: PascalCase function names
- **State Management**: Use `collectAsState()` for StateFlow observation
- **ViewModels**: Injected using `hiltViewModel()`
- **Navigation**: Use Jetpack Navigation Compose with type safety

## Data Validation
- **Input Validation**: Use `require()` for parameter validation in data classes
- **Range Validation**: Confidence (1-10), block duration (15-120 minutes)
- **Null Safety**: Leverage Kotlin's null safety features

## Architecture Patterns
- **MVVM**: Strict separation between UI, ViewModel, and Repository
- **Repository Pattern**: Single source of truth for data access
- **Dependency Injection**: Use Hilt with proper scoping
- **Reactive Programming**: Kotlin Flows and StateFlow for data streams

## Error Handling
- **Validation**: Use `require()` and `check()` for preconditions
- **Null Safety**: Prefer non-null types, use safe calls when necessary
- **Exception Handling**: Try-catch for Firebase operations and database access