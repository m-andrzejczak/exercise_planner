package com.example.exercise_planner;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.exercise_planner.databinding.TrainingHistoryRowAdapterBinding;
import com.example.exercise_planner.model.TrainingStats;

import java.util.ArrayList;

public class TrainingHistoryRowAdapter extends RecyclerView.Adapter<TrainingHistoryRowAdapter.DataHolder> {

    private ArrayList<TrainingStats> list;
    private NavController navController;
    private String trainingId = "";
    private String trainingName = "";

    public TrainingHistoryRowAdapter(ArrayList<TrainingStats> list, NavController navController, String trainingId, String trainingName) {
        this.list = list;
        this.navController = navController;
        this.trainingId = trainingId;
        this.trainingName = trainingName;
    }

    @Override
    public TrainingHistoryRowAdapter.DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrainingHistoryRowAdapterBinding trainingHistoryRowAdapterBinding = TrainingHistoryRowAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TrainingHistoryRowAdapter.DataHolder(trainingHistoryRowAdapterBinding);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull TrainingHistoryRowAdapter.DataHolder holder, int position) {
        if (getItemCount() == 0) return;

        holder.trainingHistoryRowAdapterBinding.DateTextView.setText(list.get(position).getFormattedDate());
        holder.trainingHistoryRowAdapterBinding.DurationTextView.setText(list.get(position).getFormattedDuration());
        holder.trainingHistoryRowAdapterBinding.ExerciseCountTextView.setText(String.valueOf(list.get(position).getExerciseCount()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString("trainingId", trainingId);
                bundleArgs.putString("trainingName", trainingName);
                bundleArgs.putLong("trainingDate", list.get(holder.getAdapterPosition()).getDate());
                navController.navigate(R.id.action_trainingHistoryView_to_trainingHistoryDetailsView, bundleArgs);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DataHolder extends  RecyclerView.ViewHolder {

        private TrainingHistoryRowAdapterBinding trainingHistoryRowAdapterBinding;

        public  DataHolder(TrainingHistoryRowAdapterBinding trainingHistoryRowAdapterBinding) {
            super(trainingHistoryRowAdapterBinding.getRoot());
            this.trainingHistoryRowAdapterBinding=trainingHistoryRowAdapterBinding;
        }
    }
}