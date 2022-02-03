package com.example.exercise_planner.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.util.ArrayList;

public class Training {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("exerciseIds")
    @Expose
    private ArrayList<BigInteger> exerciseIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<BigInteger> getExercises() {
        return exerciseIds;
    }

    public void setExercises(ArrayList<BigInteger> exercises) {
        this.exerciseIds = exercises;
    }
}
