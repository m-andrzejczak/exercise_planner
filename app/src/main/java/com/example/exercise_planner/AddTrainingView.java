package com.example.exercise_planner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.exercise_planner.api.ApiUtils;
import com.example.exercise_planner.databinding.AddTrainingViewBinding;
import com.example.exercise_planner.databinding.TrainingViewBinding;
import com.example.exercise_planner.model.Training;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTrainingView extends Fragment {
    private AddTrainingViewBinding binding;
    private Training currentTraining = null;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = AddTrainingViewBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            getTrainingApiCall();
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(this.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getArguments().getString("trainingId") != null) {
                    NavHostFragment.findNavController(AddTrainingView.this)
                            .navigate(R.id.action_addTrainingView_to_trainingView, getArguments());
                } else {
                    NavHostFragment.findNavController(AddTrainingView.this)
                            .navigate(R.id.action_addTrainingView_to_exerciseView, getArguments());
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getArguments().getString("trainingId") != null) {
                NavHostFragment.findNavController(AddTrainingView.this)
                        .navigate(R.id.action_addTrainingView_to_trainingView, getArguments());
            } else {
                NavHostFragment.findNavController(AddTrainingView.this)
                        .navigate(R.id.action_addTrainingView_to_exerciseView, getArguments());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.deleteTrainingButton.setVisibility(View.GONE);

        binding.saveTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentTraining == null) {
                    createTrainingApiCall();
                } else {
                    updateTrainingApiCall();
                }
            }
        });
    }

    private void createTrainingApiCall() {
        ApiUtils.getApiService().createTraining(binding.trainingNameTextInput.getText().toString())
                .enqueue(new Callback<Response<String>>() {
                    @Override
                    public void onResponse(Call<Response<String>> call, Response<Response<String>> response) {
                        if (ApiUtils.getResponseStatusCode(response) == 200) {
                            NavHostFragment.findNavController(AddTrainingView.this)
                                    .navigate(R.id.action_addTrainingView_to_trainingView);
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<String>> call, Throwable t) {

                    }
                });
    }

    private void updateTrainingApiCall() {
        currentTraining.setName(binding.trainingNameTextInput.getText().toString());
        ApiUtils.getApiService().updateTraining(currentTraining.getId(), currentTraining)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (ApiUtils.getResponseStatusCode(response) == 200) {
                            NavHostFragment.findNavController(AddTrainingView.this)
                                    .navigate(R.id.action_addTrainingView_to_exerciseView, getArguments());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }

    private void getTrainingApiCall() {
        ApiUtils.getApiService().getTraining(getArguments().getString("trainingId"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<Training>>() {

                    @Override
                    public void onNext(@NonNull Response<Training> trainingResponse) {
                        if (ApiUtils.getResponseStatusCode(trainingResponse) == 200) {
                            currentTraining = trainingResponse.body();

                            if (getArguments().getString("exerciseId") != null) {
                                getActivity().setTitle(currentTraining.getName());
                            } else {
                                getActivity().setTitle(R.string.add_training_view_label);
                            }

                            binding.trainingNameTextInput.setText(currentTraining.getName());
                            binding.deleteTrainingButton.setVisibility(View.VISIBLE);
                            binding.deleteTrainingButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new AlertDialog.Builder(getContext()).setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Delete training")
                                            .setMessage("Are you sure you want to delete training '" + currentTraining.getName() + "'?")
                                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    deleteTrainingApiCall();
                                                }
                                            })
                                            .setNegativeButton("Cancel", null)
                                            .show();
                                }
                            });
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

    private void deleteTrainingApiCall() {
        ApiUtils.getApiService().deleteTraining(currentTraining.getId())
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (ApiUtils.getResponseStatusCode(response) == 200) {
                            NavHostFragment.findNavController(AddTrainingView.this)
                                    .navigate(R.id.action_addTrainingView_to_trainingView);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {
                        Log.d("ERROR", e.getMessage());
                    }
                });
    }
}