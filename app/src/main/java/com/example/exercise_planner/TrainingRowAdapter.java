package com.example.exercise_planner;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exercise_planner.databinding.TrainingRowBinding;
import com.example.exercise_planner.model.Training;

import java.util.ArrayList;

public class TrainingRowAdapter extends RecyclerView.Adapter<TrainingRowAdapter.DataHolder> {
    private ArrayList<Training> list;
    private NavController navController;

    public TrainingRowAdapter(ArrayList<Training> list, NavController navController) {
        this.list = list;
        this.navController = navController;
    }

    @Override
    public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrainingRowBinding trainingRowBinding = TrainingRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DataHolder(trainingRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DataHolder holder, int position) {
        if (getItemCount() == 0) return;

        holder.trainingRowBinding.trainingRowTextView.setText(list.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString("trainingId", list.get(holder.getAdapterPosition()).getId());
                bundleArgs.putString("trainingName", list.get(holder.getAdapterPosition()).getName());
                navController.navigate(R.id.action_trainingView_to_exerciseView, bundleArgs);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DataHolder extends  RecyclerView.ViewHolder {

        private TrainingRowBinding trainingRowBinding;

        public  DataHolder(TrainingRowBinding trainingRowBinding) {
            super(trainingRowBinding.getRoot());
            this.trainingRowBinding=trainingRowBinding;
        }
    }
}
