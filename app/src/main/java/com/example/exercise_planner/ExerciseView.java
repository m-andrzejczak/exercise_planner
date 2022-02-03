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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.exercise_planner.api.ApiUtils;
import com.example.exercise_planner.databinding.ExerciseViewBinding;
import com.example.exercise_planner.databinding.TrainingViewBinding;
import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.Training;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ExerciseView extends Fragment {

    private ExerciseViewBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        binding = ExerciseViewBinding.inflate(inflater, container, false);

        getExercisesApiCall(getArguments().getString("trainingId"));

        requireActivity().getOnBackPressedDispatcher().addCallback(this.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(ExerciseView.this)
                        .navigate(R.id.action_exerciseView_to_trainingView, getArguments());
            }
        });

        getActivity().setTitle(getArguments().getString("trainingName"));

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(R.string.action_run_training);
        menu.add(R.string.action_edit_training);
        menu.add(R.string.action_training_history);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        CharSequence title = item.getTitle();
        if (title != null) {
            if (title.equals("Run training")) {
                Bundle bundleArgs = getArguments();
                bundleArgs.putString("tabTitle", getArguments().getString("trainingName"));
                NavHostFragment.findNavController(ExerciseView.this)
                        .navigate(R.id.action_exerciseView_to_runTrainingView, bundleArgs);
            } else if (title.equals("Edit training")) {
                Bundle bundleArgs = getArguments();
                bundleArgs.putString("tabTitle", getArguments().getString("trainingName"));
                NavHostFragment.findNavController(ExerciseView.this)
                        .navigate(R.id.action_exerciseView_to_addTrainingView, getArguments());
            } else if (title.equals("Training history")) {
                NavHostFragment.findNavController(ExerciseView.this)
                        .navigate(R.id.action_exerciseView_to_trainingHistoryView, getArguments());
            }
        }

        if (item.getItemId() == android.R.id.home) {
            NavHostFragment.findNavController(ExerciseView.this)
                    .navigate(R.id.action_exerciseView_to_trainingView, getArguments());
        }

        return super.onOptionsItemSelected(item);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundleArgs = getArguments();
                bundleArgs.putString("tabTitle", "Add exercise");
                NavHostFragment.findNavController(ExerciseView.this)
                        .navigate(R.id.action_exerciseView_to_addExerciseView, getArguments());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initExercisesView(ArrayList<Exercise> exercises) {
        RecyclerView exercisesRecyclerView =  binding.exerciseList;

        if (exercisesRecyclerView == null) {
            return;
        }
        if (exercises == null) {
            return;
        }

        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        ExerciseRowAdapter exerciseRowAdapter = new ExerciseRowAdapter(exercises,
                NavHostFragment.findNavController(ExerciseView.this),
                getArguments().getString("trainingId"),
                getArguments().getString("trainingName"));
        exercisesRecyclerView.setAdapter(exerciseRowAdapter);
    }

    private void getExercisesApiCall(String trainingId) {
        ApiUtils.getApiService().getExercisesByTrainingId(trainingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<ArrayList<Exercise>>>() {

                    @Override
                    public void onNext(@NonNull Response<ArrayList<Exercise>> listResponse) {
                        if (ApiUtils.getResponseStatusCode(listResponse) == 200) {
                            initExercisesView(listResponse.body());
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