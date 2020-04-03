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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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
                            java.lang.System.out.println("INFO: exerciseInfo " + beforeTextChanged + " renamed to " + afterTextChanged);  // DEBUG:
                            // Remove old data:
                            exerciseInfo2SetsMap.remove(beforeTextChanged);
                            // Adjust exercise info name of linked exercise sets:
                            for (ExerciseSetEntity exerciseSet : exerciseSets)
                            {
                                java.lang.System.out.println("INFO: exerciseSet adjusted");  // DEBUG:
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
        java.lang.System.out.println("INFO: setExerciseInfo is called");  // DEBUG:
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

//    void setExerciseInfo(@NonNull final List<ExerciseInfoEntity> exerciseInfo, @NonNull final List<ExerciseSetEntity> exerciseSets)
//    {
//        // Add exerciseSets to newExerciseInfo2SetsMap:
//        Map<String, List<ExerciseSetEntity>> newExerciseInfo2SetsMap = new HashMap<>();
//        for (ExerciseSetEntity exerciseSet: exerciseSets)
//        {
//            final String exerciseInfoName = exerciseSet.getExerciseInfoName();
//            final List<ExerciseSetEntity> exerciseSetList = newExerciseInfo2SetsMap.getOrDefault(exerciseInfoName, null);
//            if (exerciseSetList != null)
//            {
//                exerciseSetList.add(exerciseSet);
//            }
//            else
//            {
//                newExerciseInfo2SetsMap.put(exerciseInfoName, new LinkedList<>(Collections.singletonList(exerciseSet)));
//            }
//        }
//        // Set exerciseInfo:
//        if (this.exerciseInfo == null)
//        {
//            java.lang.System.out.println("INFO: exerciseInfo was set");  // DEBUG:
//            // Set data:
//            this.exerciseInfo = exerciseInfo;
//            exerciseInfo2SetsMap = newExerciseInfo2SetsMap;
//            // Create exercise set adapters:
//            final int exerciseInfoCount = exerciseInfo.size();
//            adapters = new ArrayList<>(exerciseInfoCount);
//            for (int i = 0; i < exerciseInfoCount; i++)
//            {
//                adapters.add(new ExerciseSetAdapter());
//            }
//            // Load all views:
//            notifyItemRangeInserted(0, exerciseInfoCount);
//        }
//        else
//        {
//            java.lang.System.out.println("INFO: exerciseInfo was changed");  // DEBUG:
//            // Determine changes:
//            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
//            {
//                @Override
//                public int getOldListSize()
//                {
//                    return ExerciseInfoAdapter.this.exerciseInfo.size();
//                }
//
//                @Override
//                public int getNewListSize()
//                {
//                    return exerciseInfo.size();
//                }
//
//                @Override
//                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
//                {
//                    final String oldExerciseInfoName = ExerciseInfoAdapter.this.exerciseInfo.get(oldItemPosition).getName();
//                    final String newExerciseInfoName = exerciseInfo.get(newItemPosition).getName();
//                    return Objects.equals(oldExerciseInfoName, newExerciseInfoName) && Objects.requireNonNull(exerciseInfo2SetsMap.get(oldExerciseInfoName)).size() == Objects.requireNonNull(newExerciseInfo2SetsMap.get(newExerciseInfoName)).size();
//                }
//
//                @Override
//                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
//                {
//                    final ExerciseInfoEntity oldExerciseInfoEntity = ExerciseInfoAdapter.this.exerciseInfo.get(oldItemPosition);
//                    final ExerciseInfoEntity newExerciseInfoEntity = exerciseInfo.get(newItemPosition);
//                    return ExerciseInfoEntity.isContentTheSame(oldExerciseInfoEntity, newExerciseInfoEntity) && ExerciseSetEntity.isContentTheSame(Objects.requireNonNull(exerciseInfo2SetsMap.get(oldExerciseInfoEntity.getName())), Objects.requireNonNull(newExerciseInfo2SetsMap.get(newExerciseInfoEntity.getName())));
//                }
//            });
//            // Set data:
//            this.exerciseInfo = exerciseInfo;
//            exerciseInfo2SetsMap = newExerciseInfo2SetsMap;
//            // Adjust exercise set adapters:
//            // TODO: think if adapters can be replaced only where changed or else if it's ok to completely replace them with new ones
//            result.dispatchUpdatesTo(new ListUpdateCallback()
//            {
//                @Override
//                public void onInserted(int position, int count)
//                {
//                    java.lang.System.out.println("INFO: onInserted " + position + " - " + count);  // DEBUG:
//                    adapters.set(position, new ExerciseSetAdapter());
//                }
//
//                @Override
//                public void onRemoved(int position, int count)
//                {
//                    java.lang.System.out.println("INFO: onRemoved " + position + " - " + count);  // DEBUG:
////                    adapters.set(position, null);
//                }
//
//                @Override
//                public void onMoved(int fromPosition, int toPosition)
//                {
//                    java.lang.System.out.println("INFO: onMoved " + fromPosition + " / " + toPosition);  // DEBUG:
//                    adapters.set(toPosition, adapters.get(fromPosition));
//                }
//
//                @Override
//                public void onChanged(int position, int count, @Nullable Object payload)
//                {
//                    java.lang.System.out.println("INFO: onChanged " + position + " - " + count);  // DEBUG:
//                    adapters.set(position, new ExerciseSetAdapter());
//                }
//            });
//            // Reload updated views:
//            result.dispatchUpdatesTo(this);
//        }
//    }

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
        if (exerciseInfo2SetsMap != null)
        {
            List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(exerciseInfoEntity.getName());
            if (exerciseSets == null)
            {
                // DEBUG: exerciseSets is null when exercise info name changed -> link change of exercise info name to current exercise sets
                exerciseSets = new ArrayList<>(0);
                java.lang.System.out.println("WARNING: Empty exercise sets list for " + exerciseInfoEntity.getName());
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
}