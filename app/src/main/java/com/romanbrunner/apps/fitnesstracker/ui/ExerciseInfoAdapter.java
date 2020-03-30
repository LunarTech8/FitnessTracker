package com.romanbrunner.apps.fitnesstracker.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseCardBinding;
import com.romanbrunner.apps.fitnesstracker.model.ExerciseInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


class ExerciseInfoAdapter extends RecyclerView.Adapter<ExerciseInfoAdapter.ExerciseInfoViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    // TODO: maybe have ExerciseInfo of exerciseInfo instead of String in exerciseInfo2SetsMap and get rid of exerciseInfo
    // TODO: -> requires ExerciseInfo to be inserted before ExerciseSetEntity
    private static Map<String, List<ExerciseSetEntity>> exerciseInfo2SetsMap = null;  // Initialised with null to mark that exercise sets haven't been set yet
    private List<? extends ExerciseInfo> exerciseInfo;
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
                        final List<ExerciseSetEntity> exerciseSetsOldName = exerciseInfo2SetsMap.get(beforeTextChanged);
                        if (exerciseSetsOldName != null)
                        {
                            final String afterTextChanged = s.toString();
                            java.lang.System.out.println("INFO: exerciseInfo " + beforeTextChanged + " renamed to " + afterTextChanged);  // DEBUG:
                            // Remove old data:
                            exerciseInfo2SetsMap.remove(beforeTextChanged);
                            // Adjust exercise info name of linked exercise sets:
                            for (ExerciseSetEntity exerciseSet : exerciseSetsOldName)
                            {
                                java.lang.System.out.println("INFO: exerciseSet adjusted");  // DEBUG:
                                exerciseSet.setExerciseInfoName(afterTextChanged);
                            }
                            // Merge exercise sets list if new exercise info name already existed:
                            final List<ExerciseSetEntity> exerciseSetsNewName = exerciseInfo2SetsMap.getOrDefault(afterTextChanged, null);
                            if (exerciseSetsNewName != null)
                            {
                                exerciseSetsOldName.addAll(exerciseSetsNewName);
                            }
                            // Add adjusted data:
                            exerciseInfo2SetsMap.put(afterTextChanged, exerciseSetsOldName);
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

    void reloadViews()
    {
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
        return new ExerciseInfoViewHolder(binding);
    }

    @Override
    /* Is called when an exercise_card is loaded/reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseInfoViewHolder exerciseInfoViewHolder, int position)
    {
        // Adjust changeable values of the view fields of targeted exercise info:
        ExerciseInfo exerciseInfo = this.exerciseInfo.get(position);
        exerciseInfoViewHolder.binding.setExerciseInfo(exerciseInfo);
        exerciseInfoViewHolder.binding.setIsEditModeActive(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.executePendingBindings();
        exerciseInfoViewHolder.isRecycled = false;

        // Adjust adapter for exercise sets of targeted exercise info:
        ExerciseSetAdapter adapter = adapters.get(position);
        if (exerciseInfo2SetsMap != null)
        {
            List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(exerciseInfo.getName());
            if (exerciseSets == null)
            {
                // DEBUG: exerciseSets is null when exercise info name changed -> link change of exercise info name to current exercise sets
                exerciseSets = new ArrayList<>(0);
                java.lang.System.out.println("WARNING: Empty exercise sets list for " + exerciseInfo.getName());
            }
            adapter.setExerciseSets(exerciseSets);
        }
        else
        {
            java.lang.System.out.println("ERROR: exerciseInfo2SetsMap is null, cannot set exercise sets");
        }
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

//    public List<ExerciseInfoEntity> getUpdatedExerciseInfo()  // TODO: refactor
//    {
//        // Find changed names and update map:
//        Map<String, String> changedKeyMap = new LinkedHashMap<>();
//        final Set<String> keys = exerciseInfoMap.keySet();
//        for (String oldKey: keys)  // DEBUG: fix ConcurrentModificationException
//        {
//            ExerciseInfoEntity exerciseInfo = exerciseInfoMap.get(oldKey);
//            if (exerciseInfo != null && !Objects.equals(exerciseInfo.getName(), oldKey))
//            {
//                String newKey = exerciseInfo.getName();
//                changedKeyMap.put(oldKey, newKey);
//                exerciseInfoMap.remove(oldKey);
//                exerciseInfoMap.put(newKey, exerciseInfo);
//            }
//        }
//        // Update changed entries:
//        final Set<String> changedKeys = changedKeyMap.keySet();
//        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
//        {
//            @Override
//            public int getOldListSize()
//            {
//                return ExerciseAdapter.this.exerciseSets.size();
//            }
//
//            @Override
//            public int getNewListSize()
//            {
//                return exerciseSets.size();
//            }
//
//            @Override
//            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
//            {
//                return exerciseSets.get(oldItemPosition).getId() == exerciseSets.get(newItemPosition).getId();
//            }
//
//            @Override
//            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
//            {
//                ExerciseSet exerciseSet = exerciseSets.get(newItemPosition);
//                if (changedKeys.contains(exerciseSet.getExerciseInfoName()))
//                {
//                    exerciseSet.setExerciseInfoName(changedKeyMap.get(exerciseSet.getExerciseInfoName()));
//                    return false;
//                }
//                return true;
//            }
//        });
//        result.dispatchUpdatesTo(this);
//        // Return updated exercise info list:
//        return new ArrayList<>(exerciseInfoMap.values());
//    }

    public void setExerciseInfo(@NonNull final List<? extends ExerciseInfo> exerciseInfo)
    {
        if (this.exerciseInfo == null)
        {
            // Add all entries:
            this.exerciseInfo = exerciseInfo;
            adapters = new ArrayList<>(exerciseInfo.size());
            for (int i = 0; i < exerciseInfo.size(); i++)
            {
                adapters.add(new ExerciseSetAdapter());
            }
            // Load all views if complete data is available:
            if (exerciseInfo2SetsMap != null)
            {
                notifyItemRangeInserted(0, exerciseInfo.size());
            }
        }
        else if (exerciseInfo2SetsMap != null)
        {
            java.lang.System.out.println("INFO: exerciseInfo2SetsMap changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            // Update changed entries:
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
            {
                @Override
                public int getOldListSize()
                {
                    return ExerciseInfoAdapter.this.exerciseInfo.size();
                }

                @Override
                public int getNewListSize()
                {
                    return exerciseInfo.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return Objects.equals(ExerciseInfoAdapter.this.exerciseInfo.get(oldItemPosition).getName(), exerciseInfo.get(newItemPosition).getName());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return ExerciseInfoEntity.isContentTheSame(exerciseInfo.get(newItemPosition), ExerciseInfoAdapter.this.exerciseInfo.get(oldItemPosition));
                }
            });
            // Reload updated views:
            result.dispatchUpdatesTo(this);
        }
    }

    public void setExerciseSets(@NonNull final List<ExerciseSetEntity> exerciseSets)
    {
        if (exerciseInfo2SetsMap == null)
        {
            exerciseInfo2SetsMap = new HashMap<>();
        }
        else
        {
            java.lang.System.out.println("INFO: setExerciseSets was cleared");  // DEBUG:
            exerciseInfo2SetsMap.clear();
        }
        // Add exerciseSets to exerciseInfo2SetsMap:
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
        // Load/Reload all views if complete data is available:
        if (this.exerciseInfo != null)
        {
            notifyDataSetChanged();
        }
    }
}