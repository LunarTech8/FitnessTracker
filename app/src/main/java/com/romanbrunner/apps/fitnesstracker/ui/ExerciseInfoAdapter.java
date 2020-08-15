package com.romanbrunner.apps.fitnesstracker.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseCardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


class ExerciseInfoAdapter extends RecyclerView.Adapter<ExerciseInfoAdapter.ExerciseInfoViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    private static Map<String, List<ExerciseSetEntity>> exerciseInfo2SetsMap;
    private List<ExerciseInfoEntity> exerciseInfo;
    private List<ExerciseSetAdapter> adapters;

    static class ExerciseInfoViewHolder extends RecyclerView.ViewHolder
    {
        private final ExerciseCardBinding binding;
        private boolean isRecycled = true;

        ExerciseInfoViewHolder(ExerciseCardBinding binding, ExerciseInfoAdapter exerciseInfoAdapter)
        {
            super(binding.getRoot());
            binding.setIsEditModeActive(MainActivity.isEditModeActive);
            binding.setsBoard.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
            binding.addSetButton.setOnClickListener((View view) ->
            {
                final String exerciseInfoName = binding.getExerciseInfo().getName();
                final List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(exerciseInfoName);
                if (exerciseSets == null) throw new AssertionError("object cannot be null");
                final int lastExerciseSetIndex = exerciseSets.size() - 1;
                if (lastExerciseSetIndex < 0)
                {
                    Log.e("ExerciseInfoViewHolder", "No exercise sets for " + exerciseInfoName + " stored");
                }
                final ExerciseSetEntity lastExerciseSet = exerciseSets.get(lastExerciseSetIndex);
                exerciseSets.add(new ExerciseSetEntity(lastExerciseSet.getWorkoutUnitId(), exerciseInfoName, lastExerciseSet.getRepeats(), lastExerciseSet.getWeight()));
                exerciseInfo2SetsMap.put(exerciseInfoName, exerciseSets);
                exerciseInfoAdapter.notifyItemChanged(getAdapterPosition());
            });
            this.binding = binding;

            // Create and add a text watcher to the name field:
            binding.exerciseNameField.addTextChangedListener(new TextWatcher()
            {
                private String beforeTextChanged = null;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                    beforeTextChanged = s.toString();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    if (!isRecycled)
                    {
                        final List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(beforeTextChanged);
                        if (exerciseSets != null)
                        {
                            final String afterTextChanged = s.toString();
                            // Remove old data:
                            exerciseInfo2SetsMap.remove(beforeTextChanged);
                            // Adjust exercise info name of linked exercise sets:
                            for (ExerciseSetEntity exerciseSet : exerciseSets)
                            {
                                exerciseSet.setExerciseInfoName(afterTextChanged);
                            }
                            // Merge exercise sets list if new exercise info name already existed:
                            final List<ExerciseSetEntity> exerciseSetsWithNewName = exerciseInfo2SetsMap.getOrDefault(afterTextChanged, null);
                            if (exerciseSetsWithNewName != null)
                            {
                                exerciseSets.addAll(exerciseSetsWithNewName);
                            }
                            // Add adjusted data:
                            exerciseInfo2SetsMap.put(afterTextChanged, exerciseSets);
                        }
                    }
                    beforeTextChanged = null;
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    ExerciseInfoAdapter()
    {
        exerciseInfo = null;
        adapters = null;
    }

    private void checkForEmptyExercises()
    {
        Log.d("removeEmptyExercise", "removeEmptyExercise called");
        Set<Integer> removableExercisePositions = new HashSet<>();
        for (int i = 0; i < exerciseInfo.size(); i++)
        {
            if (Objects.requireNonNull(exerciseInfo2SetsMap.get(exerciseInfo.get(i).getName())).size() <= 0)
            {
                removableExercisePositions.add(i);
            }
        }
        for (int position : removableExercisePositions)
        {
            Log.d("removeEmptyExercise", "Empty exerciseSets in position " + position);
            exerciseInfo2SetsMap.remove(exerciseInfo.get(position).getName());
            exerciseInfo.remove(position);
            adapters.remove(position);
            notifyItemRemoved(position);
        }
    }

    List<ExerciseInfoEntity> getExerciseInfo()
    {
        return new ArrayList<>(exerciseInfo);
    }

    List<ExerciseSetEntity> getExerciseSets()
    {
        List<ExerciseSetEntity> orderedExerciseSets = new LinkedList<>();
        for (ExerciseInfoEntity exerciseInfoEntity : exerciseInfo)
        {
            orderedExerciseSets.addAll(Objects.requireNonNull(exerciseInfo2SetsMap.get(exerciseInfoEntity.getName())));
        }
        return orderedExerciseSets;
    }

    void setExercise(@NonNull final String exerciseInfoNames, @NonNull final List<ExerciseInfoEntity> exerciseInfo, @NonNull final List<ExerciseSetEntity> exerciseSets)
    {
        // Get ordered list:
        final List<ExerciseInfoEntity> orderedExerciseInfo = new ArrayList<>(getItemCount());
        for (String exerciseName : WorkoutInfoEntity.exerciseNames2NameSet(exerciseInfoNames))
        {
            boolean targetNotFound = true;
            for (ExerciseInfoEntity exerciseInfoEntity : exerciseInfo)
            {
                if (Objects.equals(exerciseInfoEntity.getName(), exerciseName))
                {
                    orderedExerciseInfo.add(exerciseInfoEntity);
                    targetNotFound = false;
                    break;
                }
            }
            if (targetNotFound)
            {
                Log.e("setExercise", "Could not find " + exerciseName + " in exerciseInfo");
            }
        }
        // Set exerciseInfo2SetsMap:
        exerciseInfo2SetsMap = new HashMap<>();
        for (ExerciseSetEntity exerciseSet: exerciseSets)
        {
            final String exerciseInfoName = exerciseSet.getExerciseInfoName();
            final List<ExerciseSetEntity> exerciseSetList = exerciseInfo2SetsMap.getOrDefault(exerciseInfoName, null);
            if (exerciseSetList != null)
            {
                exerciseSetList.add(exerciseSet);
            }
            else
            {
                exerciseInfo2SetsMap.put(exerciseInfoName, new LinkedList<>(Collections.singletonList(exerciseSet)));
            }
        }
        // Set exerciseInfo:
        this.exerciseInfo = orderedExerciseInfo;
        // Create exercise set adapters:
        final int exerciseInfoCount = orderedExerciseInfo.size();
        adapters = new ArrayList<>(exerciseInfoCount);
        for (int i = 0; i < exerciseInfoCount; i++)
        {
            adapters.add(new ExerciseSetAdapter(this::checkForEmptyExercises));
        }
        // Load/Reload all views:
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount()
    {
        return exerciseInfo == null ? 0 : exerciseInfo.size();
    }

    @Override
    public long getItemId(int position)
    {
        return exerciseInfo.get(position).getName().hashCode();
    }

    @Override
    public @NonNull ExerciseInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        ExerciseCardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.exercise_card, viewGroup, false);
        return new ExerciseInfoViewHolder(binding, this);
    }

    @Override
    /* Is called when an exercise_card is loaded/reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseInfoViewHolder exerciseInfoViewHolder, int position)
    {
        // Adjust changeable values of the view fields of targeted exercise info:
        ExerciseInfoEntity exerciseInfoEntity = this.exerciseInfo.get(position);
        exerciseInfoViewHolder.binding.setExerciseInfo(exerciseInfoEntity);
        exerciseInfoViewHolder.binding.setIsEditModeActive(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.exerciseNameField.setFocusable(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.exerciseNameField.setEnabled(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.exerciseNameField.setFocusableInTouchMode(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.executePendingBindings();
        exerciseInfoViewHolder.isRecycled = false;

        // Adjust adapter for exercise sets of targeted exercise info:
        ExerciseSetAdapter adapter = adapters.get(position);
        adapter.setExerciseSets(Objects.requireNonNull(exerciseInfo2SetsMap.get(exerciseInfoEntity.getName())));
        exerciseInfoViewHolder.binding.setsBoard.setAdapter(adapter);
    }

    @Override
    public void onViewRecycled(@NonNull ExerciseInfoViewHolder exerciseInfoViewHolder)
    {
        super.onViewRecycled(exerciseInfoViewHolder);
        exerciseInfoViewHolder.isRecycled = true;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}