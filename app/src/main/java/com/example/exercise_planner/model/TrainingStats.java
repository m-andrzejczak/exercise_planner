package com.example.exercise_planner.model;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TrainingStats {
    @SerializedName("date")
    @Expose
    private long date;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("exerciseCount")
    @Expose
    private Integer exerciseCount;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(Integer exerciseCount) {
        this.exerciseCount = exerciseCount;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getFormattedDate() {
        Date date = new Date(this.date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        LocalDateTime localDateTime = LocalDateTime.of(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE));
        String formattedDate = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
        return formattedDate;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getFormattedDuration() {
        Integer durationInSeconds = this.duration / 1000;
        LocalTime localTime = LocalTime.of( durationInSeconds / 60 / 60, durationInSeconds / 60 % 60, durationInSeconds % 60 % 60);
        String timeFormatted = DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
        return timeFormatted;
    }
}
