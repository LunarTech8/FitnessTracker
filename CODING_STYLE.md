# Coding Style Guidelines

## Formatting Rules

✅ **No empty lines within functions** — Functions should be compact with no blank lines inside

✅ **Exactly ONE empty line between functions** — Consistent spacing between method declarations

✅ **Opening braces on new line (Allman style)** — Applies to classes, methods, control structures, and anonymous classes. Exception: short single-statement bodies can use inline braces
```java
// Good:
private void finishWorkout()
{
    final List<ExerciseInfoEntity> exerciseInfo = adapter.getExerciseInfo();
    viewModel.storeExerciseInfo(exerciseInfo);
}

// Good (anonymous class):
binding.studioSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
{
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        // ...
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
});

// Bad (K&R style):
private void finishWorkout() {
    final List<ExerciseInfoEntity> exerciseInfo = adapter.getExerciseInfo();
}
```

✅ **Section markers for code regions** — Use decorated comment blocks to separate data/functional sections within classes
```java
// --------------------
// Data code
// --------------------

public final static boolean DEBUG_MODE_ACTIVE = true;

// --------------------
// Functional code
// --------------------

private ExerciseInfoAdapter adapter;
```

✅ **Section comments end with ":"** — Comments that introduce a code section should have a colon at the end and apply to multiple lines below
```java
// Setup spinner adapters:
studioSpinnerAdapter = new ArrayAdapter<>(...);
workoutSpinnerAdapter = new ArrayAdapter<>(...);
```

✅ **Inline comments for single-line context** — Use trailing or preceding single-line comments for context that applies to only one line
```java
workoutStudios.add(workout.getStudio());  // Duplicate names will automatically be ignored in a Set
```

✅ **No redundant comments for self-explanatory function calls** — Don't add comments that just repeat what the function name already says
```java
// Good — no comment needed, function name is clear:
updateFinishedExercises();
updateEditMode();

// Good — trailing comment adds useful context:
observableWorkoutUnit.postValue(newWorkoutUnit);  // Update after DB operations to prevent race conditions

// Bad — comment just repeats function name:
// Update finished exercises:
updateFinishedExercises();
```

✅ **No magic numbers** — Use named constants instead of literal numbers
```java
// Good:
public static final int WEIGHTED_EXERCISE_REPEATS_MIN = 15;
private static final int WEIGHTED_EXERCISE_REPEATS_MAX = 20;
private static final float WEIGHTED_EXERCISE_WEIGHT_INCREMENT = 5F;

// Bad:
if (repeats > 20 && weight > 0F) { repeats = 15; }
```

✅ **No spaces in casts** — Type casts should be compact
```java
(WorkoutUnitEntity)binding.getWorkoutUnit()   // Good
(WorkoutUnitEntity) binding.getWorkoutUnit()  // Bad
```

✅ **Common abbreviations are acceptable but else try to avoid them**
```java
sharedPrefs     // Good — common abbreviation
PREFS_NAME      // Good — well-known abbreviation
exerciseStatusCb// Good — common callback abbreviation
btn             // Bad — unclear abbreviation
```

✅ **Use `var` for local variables** — When the type is clear from context
```java
final var exercisesDone = adapter.getExerciseSets().stream()...;
final var editor = getApplicationContext().getSharedPreferences(...).edit();
```

✅ **Small lambdas as one-liners** — Simple lambda expressions (1-2 statements) should be on a single line
```java
// Good — single statement:
binding.optionsButton.setOnClickListener((View view) -> binding.setIsTopBoxMinimized(!binding.getIsTopBoxMinimized()));
binding.debugPrintLogButton.setOnClickListener((View view) -> viewModel.printDebugLog());

// Good — multi-line for complex bodies:
binding.editModeButton.setOnClickListener((View view) ->
{
    isEditModeActive = !isEditModeActive;
    if (!isEditModeActive)
    {
        // ...
    }
    updateEditMode();
});
```

✅ **Keep statements on single lines** — Don't break up short expressions across multiple lines
```java
// Good:
ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
if (exercisesDone > exercisesTotal) { Log.e(...); }

// Good — multi-line for long method chains or many parameters:
final var exercisesDone = adapter.getExerciseSets().stream()
    .collect(Collectors.groupingBy(ExerciseSetEntity::getExerciseInfoName))
    .values().stream()
    .filter(sets -> sets.stream().allMatch(ExerciseSetEntity::isDone))
    .count();
```

✅ **Avoid single-use variables** — If a variable is only used once, prefer using the expression directly unless it improves readability
```java
// Good:
if (dialog.getListView().pointToPosition((int) event.getX(), (int) event.getY()) != longPressItemPosition[0]) { ... }

// Bad:
int movedPosition = dialog.getListView().pointToPosition((int) event.getX(), (int) event.getY());
if (movedPosition != longPressItemPosition[0]) { ... }
```

✅ **Typed lambda parameters** — Always specify the type in lambda parameters for click listeners
```java
// Good:
binding.optionsButton.setOnClickListener((View view) -> ...);
binding.exerciseIncrementButton.setOnClickListener((View view) -> { ... });

// Bad:
binding.optionsButton.setOnClickListener(view -> ...);
```

## Naming Conventions

✅ **Constants** — `UPPER_SNAKE_CASE` for `static final` fields
```java
public static final String EXERCISE_NAMES_SEPARATOR = ",";
private static final int WEIGHTED_EXERCISE_REPEATS_MAX = 20;
```

✅ **Fields** — `camelCase` for instance and local variables
```java
private ExerciseInfoAdapter adapter;
private WorkoutScreenBinding binding;
private MainViewModel viewModel;
```

✅ **Callback interfaces** — suffix with `Cb` for callback fields, descriptive names for interface types
```java
public interface CallbackStatus { void update(); }
public interface CallbackFocus { void set(View view, boolean hasFocus); }
private final CallbackStatus exerciseStatusCb;
```

✅ **Entity pattern** — Entities named `<Name>Entity`, implementing model interface `<Name>`, with DAO named `<Name>Dao`
```java
public class WorkoutUnitEntity implements WorkoutUnit { ... }
public interface WorkoutUnitDao { ... }
```

## Structural Conventions

✅ **Assertions for non-null guarantees** — Use assert with message for values that must not be null
```java
assert exerciseSetList != null : "object cannot be null";
assert baseWorkoutUnit != null : "object cannot be null";
```

✅ **`final` for variables that won't be reassigned**
```java
final List<ExerciseInfoEntity> exerciseInfo = adapter.getExerciseInfo();
final var currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
```

✅ **Prefer streams and lambdas for collection operations**
```java
final var exercisesTotal = adapter.getExerciseSets().stream()
    .map(ExerciseSetEntity::getExerciseInfoName).distinct().count();
```

✅ **Static utility methods on entities** — Entity classes contain their own conversion/comparison logic
```java
WorkoutUnitEntity.exerciseSets2exerciseNames(exerciseSets);
WorkoutUnitEntity.exerciseNames2NameSet(exerciseNames);
ExerciseSetEntity.isContentTheSame(setA, setB);
```

✅ **Copy constructors for cloning** — Entities provide `@Ignore` constructors that accept the model interface plus overrides
```java
@Ignore
public WorkoutUnitEntity(@NonNull WorkoutUnit workoutUnit, @NonNull Date date)
{
    this(workoutUnit.getStudio(), workoutUnit.getName(), workoutUnit.getDescription(), workoutUnit.getExerciseNames(), date);
}
```

## XML Layout Conventions

✅ **Data binding layouts** — All layouts use `<layout>` root with `<data>` variables
```xml
<layout xmlns:android="..." xmlns:app="...">
    <data>
        <variable name="isEditModeActive" type="boolean" />
        <variable name="workoutUnit" type="com.romanbrunner.apps.fitnesstracker.model.WorkoutUnit" />
    </data>
    <!-- ... -->
</layout>
```

✅ **Custom visibility binding** — Use `app:visibleGone` binding adapter instead of raw `android:visibility`
```xml
app:visibleGone="@{!isWorkoutLoading ? !isEditModeActive : false}"
```

✅ **Dimension references** — Use `@dimen/` resources for all spacing, sizes, and text sizes; no hardcoded dp/sp values in layouts
```xml
android:layout_marginStart="@dimen/item_margin_large"
android:textSize="@dimen/comment_text_size"
android:layout_width="@dimen/button_size"
```

✅ **String references** — Use `@string/` resources for all user-visible text
```xml
android:text="@string/t_editButton"
android:hint="@string/h_emptyHint"
```

✅ **Styles for reusable widget configuration** — Define styles in `styles.xml` for shared widget appearances (e.g. spinner styling)
```xml
<style name="SpinnerStyle" parent="Widget.AppCompat.Spinner">
    <item name="android:textSize">@dimen/comment_text_size</item>
    <item name="android:background">@drawable/spinner_background_with_arrow</item>
</style>
```
