package com.example.exercise_planner;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exercise_planner.databinding.ExerciseRowBinding;
import com.example.exercise_planner.databinding.TrainingRowBinding;
import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.Training;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ExerciseRowAdapter extends RecyclerView.Adapter<ExerciseRowAdapter.DataHolder> {
    private ArrayList<Exercise> list;
    private NavController navController;
    private String trainingId = "";
    private String trainingName = "";

    public ExerciseRowAdapter(ArrayList<Exercise> list, NavController navController, String trainingId, String trainingName) {
        this.list = list;
        this.navController = navController;
        this.trainingId = trainingId;
        this.trainingName = trainingName;
    }

    @Override
    public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ExerciseRowBinding exerciseRowBinding = ExerciseRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DataHolder(exerciseRowBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull DataHolder holder, int position) {
        if (getItemCount() == 0) return;

        String displayProperty = String.format("%d reps", list.get(position).getReps());
        String propertyLabel = "Reps";
        Integer time = list.get(position).getTime();
        if (time != 0) {
            propertyLabel = "Duration";
            LocalTime localTime = LocalTime.of(time / 60 / 60, time / 60 % 60, time % 60 % 60);
            String timeFormatted = DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
            displayProperty = timeFormatted;
        }

        holder.exerciseRowBinding.ExerciseNameTextView.setText(list.get(position).getName());
        holder.exerciseRowBinding.ExercisePropertyTextView.setText(displayProperty);
        holder.exerciseRowBinding.ExerciseNotesTextView.setText(list.get(position).getNotes());
        holder.exerciseRowBinding.exercisePropertyLabel.setText(propertyLabel);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString("trainingId", trainingId);
                bundleArgs.putString("trainingName", trainingName);
                bundleArgs.putString("exerciseId", list.get(holder.getAdapterPosition()).getId());
                bundleArgs.putInt("exerciseIdx", holder.getAdapterPosition());
                bundleArgs.putString("tabTitle", list.get(holder.getAdapterPosition()).getName());
                navController.navigate(R.id.action_exerciseView_to_addExerciseView, bundleArgs);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DataHolder extends  RecyclerView.ViewHolder {

        private ExerciseRowBinding exerciseRowBinding;

        public  DataHolder(ExerciseRowBinding exerciseRowBinding) {
            super(exerciseRowBinding.getRoot());
            this.exerciseRowBinding=exerciseRowBinding;
        }
    }
}
