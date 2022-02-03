package com.example.exercise_planner;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.exercise_planner.api.ApiUtils;
import com.example.exercise_planner.databinding.ExerciseViewBinding;
import com.example.exercise_planner.databinding.TrainingHistoryViewBinding;
import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.TrainingStats;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class TrainingHistoryView extends Fragment {

    private TrainingHistoryViewBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = TrainingHistoryViewBinding.inflate(inflater, container, false);

        getTrainingHistoryApiCall(getArguments().getString("trainingId"));

        getActivity().setTitle(getArguments().getString("trainingName"));

        requireActivity().getOnBackPressedDispatcher().addCallback(this.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(TrainingHistoryView.this)
                        .navigate(R.id.action_trainingHistoryView_to_exerciseView, getArguments());
            }
        });

        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavHostFragment.findNavController(TrainingHistoryView.this)
                    .navigate(R.id.action_trainingHistoryView_to_exerciseView, getArguments());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initTrainingHistoryView(ArrayList<TrainingStats> trainingStats) {
        RecyclerView trainingHistoryRecyclerView =  binding.trainingHistoryRecyclerView;

        if (trainingHistoryRecyclerView == null) {
            return;
        }
        if (trainingStats == null) {
            return;
        }

        trainingHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        TrainingHistoryRowAdapter trainingHistoryRowAdapter = new TrainingHistoryRowAdapter(trainingStats,
                NavHostFragment.findNavController(TrainingHistoryView.this),
                getArguments().getString("trainingId"),
                getArguments().getString("trainingName"));
        trainingHistoryRecyclerView.setAdapter(trainingHistoryRowAdapter);
    }

    private void getTrainingHistoryApiCall(String trainingId) {
        ApiUtils.getApiService().getTrainingHistory(trainingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<ArrayList<TrainingStats>>>() {

                    @Override
                    public void onNext(@NonNull Response<ArrayList<TrainingStats>> listResponse) {
                        if (ApiUtils.getResponseStatusCode(listResponse) == 200) {
                            initTrainingHistoryView(listResponse.body());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("ERROR", e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}