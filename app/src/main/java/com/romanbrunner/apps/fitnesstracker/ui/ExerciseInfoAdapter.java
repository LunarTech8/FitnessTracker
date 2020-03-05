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
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseInfoCardBinding;

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

    private static Map<String, List<ExerciseSetEntity>> exerciseInfo2SetsMap = null;
    private List<? extends ExerciseInfo> exerciseInfo;
    private ExerciseSetAdapter adapter;

    static class ExerciseInfoViewHolder extends RecyclerView.ViewHolder
    {
        final ExerciseInfoCardBinding binding;

        ExerciseInfoViewHolder(ExerciseInfoCardBinding binding)
        {
            super(binding.getRoot());
            binding.setIsEditModeActive(MainActivity.isEditModeActive);
            binding.setsBoard.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
            this.binding = binding;
        }
    }

    ExerciseInfoAdapter()
    {
        exerciseInfo = null;
        adapter = new ExerciseSetAdapter();
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
    public @NonNull
    ExerciseInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        ExerciseInfoCardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.exercise_card, viewGroup, false);
        return new ExerciseInfoViewHolder(binding);
    }

    @Override
    /* Is called when an exercise_card is reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseInfoViewHolder exerciseInfoViewHolder, int position)
    {
        // Adjust changeable values of the view fields by the current exercises list:
        exerciseInfoViewHolder.binding.setExerciseInfo(exerciseInfo.get(position));
        exerciseInfoViewHolder.binding.executePendingBindings();

        exerciseInfoViewHolder.binding.setsBoard.setAdapter(adapter);
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

    public boolean setExerciseInfo(@NonNull final List<? extends ExerciseInfo> exerciseInfo)
    {
        if (this.exerciseInfo == null)
        {
            // Add all entries:
            this.exerciseInfo = exerciseInfo;
            if (exerciseInfo2SetsMap != null)
            {
                notifyItemRangeInserted(0, exerciseInfo.size());
                return true;
            }
        }
        else if (exerciseInfo2SetsMap != null)
        {
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
            result.dispatchUpdatesTo(this);
            return true;
        }
        return false;
    }

    public boolean setExerciseSets(@NonNull final List<ExerciseSetEntity> exerciseSets)
    {
        if (exerciseInfo2SetsMap == null)
        {
            exerciseInfo2SetsMap = new HashMap<>();
            if (this.exerciseInfo != null)
            {
                notifyItemRangeInserted(0, exerciseInfo.size());
            }
        }
        else
        {
            exerciseInfo2SetsMap.clear();
        }
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
                exerciseInfo2SetsMap.put(exerciseInfoName, new LinkedList<>(exerciseSets));
            }
        }
        adapter.setExerciseSets(exerciseSets);
        return (this.exerciseInfo != null);
    }
}