# Fitness Tracker - AI Coding Agent Instructions

## Architecture Overview

This is an Android fitness tracking app with a single-activity architecture using MVVM pattern. The app provides a compact interface where all exercises for a workout are displayed in one scrollable list, with each exercise set completable with one button press.

### Core Components

- **Single Activity**: `MainActivity` handles the entire UI with data binding (`WorkoutScreenBinding`)
- **Database Layer**: Room database with entities for `WorkoutUnit`, `ExerciseSet`, and `ExerciseInfo`
- **Repository Pattern**: `DataRepository` provides centralized data access with LiveData
- **MVVM**: `MainViewModel` bridges UI and repository, following Android Architecture Components

### Data Flow Architecture

```
MainActivity -> MainViewModel -> DataRepository -> AppDatabase -> Room Entities
```

Key architectural decisions:
- **Date-based primary keys**: `WorkoutUnitEntity` uses `Date` as primary key for workout sessions
- **Automatic workout cloning**: New workouts are created by cloning the most recent workout's structure
- **String-encoded exercise names**: `WorkoutUnitEntity.exerciseNames` stores exercise order as delimited strings (`EXERCISE_NAMES_SEPARATOR = ","`, `EXERCISE_NAMES_DELIMITER = ";"`)

## Development Patterns

### Database Schema Evolution
- Room database version 5 with migration support from version 1
- Schema files stored in `app/schemas/` - always check latest version before entity changes
- Foreign key relationships: `ExerciseSetEntity` -> `WorkoutUnitEntity` (CASCADE) and `ExerciseInfoEntity` (RESTRICT)

### Data Binding Conventions
- All layouts use Android Data Binding with `<layout>` root tags
- Main binding: `WorkoutScreenBinding` in `MainActivity`
- Card-based UI: `exercise_card.xml`, `exercise_set_card.xml`

### Code Section Organization
- Classes use "Data code" and "Functional code" section markers
- Constants (`static final` fields) always go in the "Data code" section
- Instance fields, methods, and constructors go in the "Functional code" section

### Repository Pattern Implementation
- Singleton pattern: `DataRepository.getInstance(AppDatabase)`
- Callback-based async operations: `CallbackAction<T>` and `CallbackCondition<T>` interfaces
- `executeOnceForLiveData()` utility for one-time LiveData operations
- Thread management via `Executor executor = Executors.newSingleThreadExecutor()`

### Entity Design Patterns
- All entities implement corresponding model interfaces (`WorkoutUnit`, `ExerciseSet`, `ExerciseInfo`)
- Copy constructors for workout cloning: `new WorkoutUnitEntity(oldWorkoutUnit, newDate)`
- Room annotations: `@Entity`, `@PrimaryKey`, `@ForeignKey`, `@Index`, `@Ignore`

## Build & Development Workflow

### Gradle Configuration
- **Target SDK**: 36, **Min SDK**: 28, **Compile SDK**: 36
- **Java Version**: 11 (source/target compatibility)
- **Key Features**: Data Binding, View Binding enabled
- **Room Version**: 2.3.0 with RxJava2 and Guava extensions

### Debug Configuration
- `MainActivity.DEBUG_MODE_ACTIVE = true` enables debug logging
- Build variants: `debug` builds include `buildConfigField "boolean", "ENABLE_ASSERTIONS", "true"`
- Room schema export configured: `arguments = ["room.schemaLocation": "$projectDir/schemas"]`

### Testing Structure
- Unit tests: `app/src/test/java/`
- Android tests: `app/src/androidTest/java/`
- Room testing dependency included for database testing

## Critical Implementation Details

### Workout Data Management

- **Primary Key Strategy**: `WorkoutUnitEntity` uses `Date` as primary key—be careful with timezone/duplicate handling
- **Exercise Order Persistence**: `exerciseNames` field stores comma-separated exercise names with counts
- **Automatic Cloning**: New workouts are created by copying previous workout structure with new date
- **Race Condition Fix**: When switching or finishing workouts, database operations (delete/insert) must complete before updating the observable LiveData. The observable is now updated only after DB operations finish (using `postValue` inside the executor), preventing UI from loading stale or incomplete data. See `replaceCurrentWorkoutUnit` and `finishWorkout` in `DataRepository.java` for the correct pattern.
- **Immediate Workout Storage**: When users edit studio or workout names in the UI, the current workout unit is immediately stored in the database (via `storeCurrentWorkoutUnit`) making new studios/workouts available for switching before finishing. This prevents crashes when switching to newly created but unfinished workouts.

### Known Issues (from GlobalTODOs.text)
- New exercise info may not persist properly when adding exercises
- Exercise removal logic has edge cases with first exercise deletion

### UI State Management
- Single screen app with expandable top box menu
- RecyclerView with `LinearLayoutManager` for exercise list
- Data binding updates UI automatically via LiveData observations

## File Organization
- **Entities**: `database/` package - Room entities with DAO interfaces
- **Models**: `model/` package - Interface definitions
- **UI**: `ui/` package - Currently only `MainActivity`
- **ViewModels**: `viewmodels/` package - MVVM pattern implementation
- **Application**: `BasicApp` extends `Application` - dependency injection root

## Development Commands
- **Build**: Use Android Studio or `./gradlew assembleDebug`
- **Tests**: `./gradlew test` for unit tests, `./gradlew connectedAndroidTest` for instrumented tests
- **Schema Export**: Automatically generated in `app/schemas/` when Room entities change

When making changes:
1. Check Room schema version in `AppDatabase` if modifying entities
2. Update migration logic for database changes
3. Test workout cloning behavior after entity modifications
4. Verify data binding updates in layouts match entity changes

## Git Commit Message Task

When asked to generate a git commit message (e.g., "generate git message", "git commit", "what changed"):

**Rules:**
- Start with a brief header summarizing what was done
- Use bullet point format with main points and sub-points where absolutely needed
- Start each point with a verb (Refactor, Add, Fix, Update, Remove, Improve, etc.)
- Keep points concise and general - no specific values, examples, or implementation details
- Only use sub-points if absolutely necessary to distinguish between unrelated changes under same category
- Order by significance: architectural changes first, then features, then fixes, then documentation
- Omit obvious implications and redundant details
- Do not list implementation fixes or adjustments (theme support, styling, padding) as separate items when they are part of implementing the main feature
- Do not list meta changes like updating documentation, instructions, or comments

**Format:**
```
[Brief header summarizing changes]

- Main change category
  - Specific change 1
  - Specific change 2
- Another main change
- Fix something
- Update documentation
```
