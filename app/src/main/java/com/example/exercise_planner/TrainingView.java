package com.example.exercise_planner;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exercise_planner.api.ApiClient;
import com.example.exercise_planner.api.ApiUtils;
import com.example.exercise_planner.databinding.TrainingViewBinding;
import com.example.exercise_planner.model.Training;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class TrainingView extends Fragment {

    private TrainingViewBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = TrainingViewBinding.inflate(inflater, container, false);

        getTrainingsApiCall();

        getActivity().setTitle(R.string.trainings_view_label);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString("tabTitle", "Add training");
                NavHostFragment.findNavController(TrainingView.this)
                        .navigate(R.id.action_trainingView_to_addTrainingView, bundleArgs);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initTrainingsView(ArrayList<Training> trainings) {
        RecyclerView trainingsRecyclerView =  binding.trainingsList;

        if (trainingsRecyclerView == null) {
            return;
        }
        if (trainings == null) {
            return;
        }

        trainingsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        TrainingRowAdapter trainingRowAdapter = new TrainingRowAdapter(trainings, NavHostFragment.findNavController(TrainingView.this));
        trainingsRecyclerView.setAdapter(trainingRowAdapter);
    }

    private void getTrainingsApiCall() {
        ApiUtils.getApiService().getTrainings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<ArrayList<Training>>>() {

                    @Override
                    public void onNext(@NonNull Response<ArrayList<Training>> listResponse) {
                        if (ApiUtils.getResponseStatusCode(listResponse) == 200) {
                            initTrainingsView(listResponse.body());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("ERROR", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}