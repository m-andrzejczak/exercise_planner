package com.example.exercise_planner.api;

import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.Training;
import com.example.exercise_planner.model.TrainingLog;
import com.example.exercise_planner.model.TrainingStats;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/trainings")
    Observable<Response<ArrayList<Training>>> getTrainings();

    @GET("/training/{id}")
    Observable<Response<Training>> getTraining(@Path("id") String id);

    @FormUrlEncoded
    @POST("/training")
    Call<Response<String>> createTraining(@Field("name") String name);

    @PUT("/training/update/{id}")
    Call<ResponseBody> updateTraining(@Path("id") String id, @Body Training training);

    @DELETE("/training/{id}")
    Call<ResponseBody> deleteTraining(@Path("id") String id);

    @GET("/exercises")
    Observable<Response<ArrayList<Exercise>>> getExercises();

    @GET("/exercises/{trainingId}")
    Observable<Response<ArrayList<Exercise>>> getExercisesByTrainingId(@Path("trainingId") String trainingId);

    @GET("/exercise/{id}")
    Observable<Response<Exercise>> getExercise(@Path("id") String id);

    @FormUrlEncoded
    @POST("/exercise/{trainingId}")
    Call<ResponseBody> createExercise(@Path("trainingId") String trainingId,
                                          @Field("name") String name,
                                          @Field("time") Integer time,
                                          @Field("reps") Integer reps,
                                          @Field("notes") String notes,
                                          @Field("autostart") Boolean autostart);

    @PUT("/exercise/update/{id}")
    Call<ResponseBody> updateExercise(@Path("id") String id, @Body Exercise exercise);

    @DELETE("/exercise/{id}")
    Call<ResponseBody> deleteExercise(@Path("id") String id);

    @GET("/history/{trainingId}")
    Observable<Response<ArrayList<TrainingStats>>> getTrainingHistory(@Path("trainingId") String trainingId);

    @GET("/history/{trainingId}")
    Observable<Response<ArrayList<Exercise>>> getTrainingHistory(@Path("trainingId") String trainingId, @Query("trainingDate") long trainingDate);

    @POST("/history/{trainingId}")
    Call<ResponseBody> saveTrainingToHistory(@Path("trainingId") String trainingId, @Body TrainingLog trainingLog);
}
