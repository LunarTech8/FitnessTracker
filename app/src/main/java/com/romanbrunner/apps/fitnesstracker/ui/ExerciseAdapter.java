package com.romanbrunner.apps.fitnesstracker.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.model.ExerciseInfo;
import com.romanbrunner.apps.fitnesstracker.model.ExerciseSet;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseCardBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    private static Map<String, ExerciseInfoEntity> exerciseInfoMap = null;  // TODO: remove
    private static Map<ExerciseInfoEntity, List<ExerciseSetEntity>> exerciseSetsMap = null;  // TODO: implement
    private List<? extends ExerciseInfo> exerciseInfo;
    private List<? extends ExerciseSet> exerciseSets;  // TODO: remove in this class
    private ExerciseSetAdapter adapter;

    static class ExerciseViewHolder extends RecyclerView.ViewHolder
    {
        final ExerciseCardBinding binding;

        ExerciseViewHolder(ExerciseCardBinding binding)
        {
            super(binding.getRoot());
            binding.setIsEditModeActive(MainActivity.isEditModeActive);
            binding.setsBoard.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
            this.binding = binding;
        }
    }

    ExerciseAdapter()
    {
        exerciseInfo = null;
        exerciseSets = null;
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
    }  // TODO: find out how to correctly convert of entity with string as primary key

    @Override
    public @NonNull ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        ExerciseCardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.exercise_card, viewGroup, false);
        return new ExerciseViewHolder(binding);
    }

    @Override
    /* Is called when an exercise_card is reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseViewHolder exerciseViewHolder, int position)
    {
        // Adjust changeable values of the view fields by the current exercises list:
        exerciseViewHolder.binding.setExerciseInfo(exerciseInfo.get(position));
        // TODO: reimplement in sub-adapter
//        exerciseViewHolder.binding.setExerciseSet(exerciseSets.get(position));
        exerciseViewHolder.binding.executePendingBindings();

        adapter = new ExerciseSetAdapter();
        exerciseViewHolder.binding.setsBoard.setAdapter(adapter);
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

    public boolean setExerciseInfo(@NonNull final List<ExerciseInfoEntity> exerciseInfoList)  // TODO: refactor
    {
        if (exerciseInfoMap == null)
        {
            exerciseInfoMap = new HashMap<>();
            for (ExerciseInfoEntity exerciseInfo: exerciseInfoList)
            {
                exerciseInfoMap.put(exerciseInfo.getName(), exerciseInfo);
            }
            if (exerciseSets != null)
            {
                notifyItemRangeInserted(0, exerciseSets.size());
                return true;
            }
        }
        else
        {
            exerciseInfoMap.clear();
            for (ExerciseInfoEntity exerciseInfo: exerciseInfoList)
            {
                exerciseInfoMap.put(exerciseInfo.getName(), exerciseInfo);
            }
            if (exerciseSets != null)
            {
                // Update changed entries:
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
                {
                    @Override
                    public int getOldListSize()
                    {
                        return exerciseSets.size();
                    }

                    @Override
                    public int getNewListSize()
                    {
                        return exerciseSets.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
                    {
                        return exerciseSets.get(oldItemPosition).getId() == exerciseSets.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
                    {
                        ExerciseInfoEntity exerciseInfoA = exerciseInfoMap.get(exerciseSets.get(newItemPosition).getExerciseInfoName());
                        ExerciseInfoEntity exerciseInfoB = exerciseInfoMap.get(exerciseSets.get(oldItemPosition).getExerciseInfoName());
                        if (exerciseInfoA != null && exerciseInfoB != null)
                        {
                            return ExerciseInfoEntity.isContentTheSame(exerciseInfoA, exerciseInfoB);
                        }
                        else
                        {
                            return false;
                        }
                    }
                });
                result.dispatchUpdatesTo(this);
                return true;
            }
        }
        return false;
    }

    public boolean setExerciseSets(@NonNull final List<? extends ExerciseSet> exerciseSets)  // TODO: refactor
    {
        exerciseSetsMap = new HashMap<>();
        // TODO: implement
        adapter.setExerciseSets(exerciseSets);

        // TODO: remove following
        if (this.exerciseSets == null)
        {
            // Add all entries:
            this.exerciseSets = exerciseSets;
            if (exerciseInfoMap != null)
            {
                notifyItemRangeInserted(0, exerciseSets.size());
                return true;
            }
        }
        else
        {
            // Update changed entries:
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
            {
                @Override
                public int getOldListSize()
                {
                    return ExerciseAdapter.this.exerciseSets.size();
                }

                @Override
                public int getNewListSize()
                {
                    return exerciseSets.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return ExerciseAdapter.this.exerciseSets.get(oldItemPosition).getId() == exerciseSets.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return ExerciseSetEntity.isContentTheSame(exerciseSets.get(newItemPosition), ExerciseAdapter.this.exerciseSets.get(oldItemPosition));
                }
            });
            this.exerciseSets = exerciseSets;
            if (exerciseInfoMap != null)
            {
                result.dispatchUpdatesTo(this);
                return true;
            }
        }
        return false;
    }
}