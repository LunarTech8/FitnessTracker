package com.romanbrunner.apps.fitnesstracker.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseCardBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


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

        ExerciseInfoViewHolder(ExerciseCardBinding binding)
        {
            super(binding.getRoot());
            binding.setIsEditModeActive(MainActivity.isEditModeActive);
            binding.setsBoard.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
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
                            java.lang.System.out.println("DEBUG: exerciseInfo " + beforeTextChanged + " renamed to " + afterTextChanged);  // DEBUG:
                            // Remove old data:
                            exerciseInfo2SetsMap.remove(beforeTextChanged);
                            // Adjust exercise info name of linked exercise sets:
                            for (ExerciseSetEntity exerciseSet : exerciseSets)
                            {
                                java.lang.System.out.println("DEBUG: exerciseSet adjusted");  // DEBUG:
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

    List<ExerciseInfoEntity> getExerciseInfo()
    {
        return exerciseInfo;
    }

    void setExerciseInfo(@NonNull final List<ExerciseInfoEntity> exerciseInfo, @NonNull final List<ExerciseSetEntity> exerciseSets)
    {
        java.lang.System.out.println("DEBUG: setExerciseInfo is called and reset");  // DEBUG:
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
        this.exerciseInfo = exerciseInfo;
        // Create exercise set adapters:
        final int exerciseInfoCount = exerciseInfo.size();
        adapters = new ArrayList<>(exerciseInfoCount);
        for (int i = 0; i < exerciseInfoCount; i++)
        {
            adapters.add(new ExerciseSetAdapter());
        }
        // Load/Reload all views:
        notifyDataSetChanged();
    }

    void sortExerciseInfo(@NonNull final String sortedExerciseInfoNames, @NonNull final List<ExerciseInfoEntity> exerciseInfo, @NonNull final List<ExerciseSetEntity> exerciseSets)
    {
        // Get sorted list:
        final List<ExerciseInfoEntity> sortedExerciseInfo = new ArrayList<>(getItemCount());
        for (String exerciseInfoName : sortedExerciseInfoNames.split(";"))
        {
            boolean targetNotFound = true;
            for (ExerciseInfoEntity exerciseInfoEntity : exerciseInfo)
            {
                if (Objects.equals(exerciseInfoEntity.getName(), exerciseInfoName))
                {
                    sortedExerciseInfo.add(exerciseInfoEntity);
                    targetNotFound = false;
                    break;
                }
            }
            if (targetNotFound)
            {
                java.lang.System.out.println("ERROR: Could not find " + exerciseInfoName + " in exerciseInfo");
            }
        }
        // Update adapter:
        setExerciseInfo(sortedExerciseInfo, exerciseSets);
    }
    void sortExerciseInfo(@NonNull final String sortedExerciseInfoNames)
    {
        sortExerciseInfo(sortedExerciseInfoNames, getExerciseInfo(), exerciseInfo2SetsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
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
        return new ExerciseInfoViewHolder(binding);
    }

    @Override
    /* Is called when an exercise_card is loaded/reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseInfoViewHolder exerciseInfoViewHolder, int position)
    {
        // Adjust changeable values of the view fields of targeted exercise info:
        ExerciseInfoEntity exerciseInfoEntity = this.exerciseInfo.get(position);
        exerciseInfoViewHolder.binding.setExerciseInfo(exerciseInfoEntity);
        exerciseInfoViewHolder.binding.setIsEditModeActive(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.executePendingBindings();
        exerciseInfoViewHolder.isRecycled = false;

        // Adjust adapter for exercise sets of targeted exercise info:
        ExerciseSetAdapter adapter = adapters.get(position);
        List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(exerciseInfoEntity.getName());
        if (exerciseSets == null)
        {
            exerciseSets = new ArrayList<>(0);
            java.lang.System.out.println("WARNING: Empty exercise sets list for " + exerciseInfoEntity.getName());  // DEBUG: should probably never be called
        }
        adapter.setExerciseSets(exerciseSets);
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