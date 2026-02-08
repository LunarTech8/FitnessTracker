# Fitness Tracker

Compact fitness tracker app for Android smartphones. All exercises for a workout are displayed in one scrollable list on a single screen, where every exercise set can be completed with one button press. Supports multiple studios and workout routines, switchable via dropdown spinners.

More in-depth data, settings and edit options can be accessed via an expandable top box menu.

<table>
  <tr>
    <td>Regular view</td>
    <td>Expanded top box</td>
  </tr>
  <tr>
    <td><img src="app/doc/readme/Showcase01.jpg" width=384></td>
    <td><img src="app/doc/readme/Showcase02.jpg" width=384></td>
  </tr>
</table>

## Features

- **Single-screen workout tracking** — all exercises visible in one scrollable list
- **One-tap set completion** — checkbox + auto-increment button for reps/weight progression
- **Studio & workout switching** — dropdown spinners to change between studios and workout routines
- **Automatic workout cloning** — finishing a workout creates a new session pre-filled with the previous structure
- **Edit mode** — toggle to rename exercises, reorder them, add/remove sets, and edit studio/workout names
- **Exercise management** — add new or existing exercises via dropdown menu; long-press (5 seconds) on existing exercises to remove them from all workouts
- **Workout statistics** — total workout count, average interval between sessions, last workout date
- **Light/dark theme support** — three-way toggle (system/light/dark) with persistent preference
- **Debug tools** — log inspection and data management buttons (debug builds only)

## Architecture

Single-activity MVVM architecture using Android Architecture Components.

```
MainActivity → MainViewModel → DataRepository → AppDatabase (Room)
```

| Layer | Package | Key Classes |
|-------|---------|-------------|
| UI | `ui/` | `MainActivity`, `ExerciseInfoAdapter`, `ExerciseSetAdapter`, `BindingAdapters` |
| ViewModels | `viewmodels/` | `MainViewModel` |
| Repository | root | `DataRepository` (singleton, executor-based async) |
| Database | `database/` | `AppDatabase`, `WorkoutUnitEntity/Dao`, `ExerciseSetEntity/Dao`, `ExerciseInfoEntity/Dao`, `DateConverter` |
| Models | `model/` | `WorkoutUnit`, `ExerciseSet`, `ExerciseInfo` (interfaces) |
| Application | root | `BasicApp` (dependency injection root) |

### Data Model

- **WorkoutUnitEntity** — a workout session (primary key: `Date`), stores studio, name, description, and exercise order as a delimited string
- **ExerciseSetEntity** — a single set within a workout (auto-generated ID), linked via foreign keys to `WorkoutUnitEntity` (CASCADE) and `ExerciseInfoEntity` (RESTRICT)
- **ExerciseInfoEntity** — exercise metadata (primary key: `name`), stores token, remarks, and default values

### Key Patterns

- All layouts use Android Data Binding with `<layout>` root tags
- Card-based UI: `exercise_card.xml` and `exercise_set_card.xml`
- Repository uses callback-based async with `CallbackAction<T>` / `CallbackCondition<T>` and `executeOnceForLiveData()` utility
- Room database version 5 with full migration chain from version 1
- Entities implement model interfaces; copy constructors enable workout cloning

## Build & Development

| Setting | Value |
|---------|-------|
| Compile SDK | 36 |
| Target SDK | 36 |
| Min SDK | 28 |
| Java | 11 (source/target compatibility) |
| Room | 2.3.0 |
| Lifecycle | 2.3.1 |

### Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Project Structure

```
app/src/main/
├── java/.../fitnesstracker/
│   ├── BasicApp.java              # Application class
│   ├── AppExecutors.java          # Thread pool helper
│   ├── DataRepository.java        # Centralized data access (singleton)
│   ├── database/
│   │   ├── AppDatabase.java       # Room database + migrations
│   │   ├── DateConverter.java     # Room type converter
│   │   ├── WorkoutUnitEntity.java # Workout session entity
│   │   ├── WorkoutUnitDao.java    # Workout DAO
│   │   ├── ExerciseSetEntity.java # Exercise set entity
│   │   ├── ExerciseSetDao.java    # Exercise set DAO
│   │   ├── ExerciseInfoEntity.java# Exercise metadata entity
│   │   └── ExerciseInfoDao.java   # Exercise metadata DAO
│   ├── model/
│   │   ├── WorkoutUnit.java       # Interface
│   │   ├── ExerciseSet.java       # Interface
│   │   └── ExerciseInfo.java      # Interface
│   ├── ui/
│   │   ├── MainActivity.java      # Single activity
│   │   ├── ExerciseInfoAdapter.java# RecyclerView adapter (exercises)
│   │   ├── ExerciseSetAdapter.java # Nested RecyclerView adapter (sets)
│   │   └── BindingAdapters.java   # Custom data binding adapters
│   └── viewmodels/
│       └── MainViewModel.java     # MVVM bridge
├── res/
│   ├── layout/
│   │   ├── workout_screen.xml     # Main screen layout
│   │   ├── exercise_card.xml      # Exercise card layout
│   │   └── exercise_set_card.xml  # Exercise set card layout
│   ├── drawable/                  # Borders, icons
│   └── values/                    # Colors, dimens, strings, styles
└── schemas/                       # Room schema exports
```

## License

See [LICENSE](LICENSE) for details.
