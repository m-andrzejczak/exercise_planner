package com.example.exercise_planner.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class TrainingLog {
    @SerializedName("startDate")
    @Expose
    private long startDate;
    @SerializedName("endDate")
    @Expose
    private long endDate;
    @SerializedName("exercises")
    @Expose
    private ArrayList<Exercise> exercises;

    public TrainingLog() {
    }

    public TrainingLog(long startDate, long endDate, ArrayList<Exercise> exercises) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.exercises = exercises;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }
}
