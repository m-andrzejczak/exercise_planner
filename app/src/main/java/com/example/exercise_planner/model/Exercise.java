package com.example.exercise_planner.model;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Exercise {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("reps")
    @Expose
    private int reps;
    @SerializedName("time")
    @Expose
    private int time;
    @SerializedName("notes")
    @Expose
    private String notes;
    @SerializedName("autostart")
    @Expose
    private Boolean autostart;

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

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getAutostart() {
        return autostart;
    }

    public void setAutostart(Boolean autostart) {
        this.autostart = autostart;
    }

    public void printAll() {
        Log.d("EXERCISE_ARGS", String.format("id: %s\nname: %s\ntime: %d\nreps: %d\nnotes: %s\nautostart: %b\n", id, name, time, reps, notes, autostart));
    }
}
