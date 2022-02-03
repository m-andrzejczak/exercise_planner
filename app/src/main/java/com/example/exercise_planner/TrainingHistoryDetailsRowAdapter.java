package com.example.exercise_planner;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.exercise_planner.databinding.TrainingHistoryDetailsRowAdapterBinding;
import com.example.exercise_planner.databinding.TrainingHistoryRowAdapterBinding;
import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.TrainingStats;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TrainingHistoryDetailsRowAdapter extends RecyclerView.Adapter<TrainingHistoryDetailsRowAdapter.DataHolder> {

    private ArrayList<Exercise> list;
    private NavController navController;
    private String trainingId = "";

    public TrainingHistoryDetailsRowAdapter(ArrayList<Exercise> list, NavController navController, String trainingId) {
        this.list = list;
        this.navController = navController;
        this.trainingId = trainingId;
    }

    @Override
    public TrainingHistoryDetailsRowAdapter.DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrainingHistoryDetailsRowAdapterBinding trainingHistoryDetailsRowAdapterBinding = TrainingHistoryDetailsRowAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TrainingHistoryDetailsRowAdapter.DataHolder(trainingHistoryDetailsRowAdapterBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull TrainingHistoryDetailsRowAdapter.DataHolder holder, int position) {
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

        holder.trainingHistoryDetailsRowAdapterBinding.ExerciseNameTextView.setText(list.get(position).getName());
        holder.trainingHistoryDetailsRowAdapterBinding.ExercisePropertyTextView.setText(displayProperty);
        holder.trainingHistoryDetailsRowAdapterBinding.ExerciseNotesTextView.setText(list.get(position).getNotes());
        holder.trainingHistoryDetailsRowAdapterBinding.exercisePropertyLabel.setText(propertyLabel);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DataHolder extends  RecyclerView.ViewHolder {

        private TrainingHistoryDetailsRowAdapterBinding trainingHistoryDetailsRowAdapterBinding;

        public  DataHolder(TrainingHistoryDetailsRowAdapterBinding trainingHistoryDetailsRowAdapterBinding) {
            super(trainingHistoryDetailsRowAdapterBinding.getRoot());
            this.trainingHistoryDetailsRowAdapterBinding=trainingHistoryDetailsRowAdapterBinding;
        }
    }
}