package com.example.exercise_planner;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.example.exercise_planner.databinding.TrainingHistoryDetailsViewBinding;
import com.example.exercise_planner.databinding.TrainingHistoryViewBinding;
import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.TrainingStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class TrainingHistoryDetailsView extends Fragment {

    private TrainingHistoryDetailsViewBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = TrainingHistoryDetailsViewBinding.inflate(inflater, container, false);

        getTrainingDetailsApiCall(getArguments().getString("trainingId"));

        getActivity().setTitle(getArguments().getString("trainingName"));
        Date date = new Date(getArguments().getLong("trainingDate"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        LocalDateTime localDateTime = LocalDateTime.of(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE));
        String formattedDate = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
        binding.entryDateTextView.setText(formattedDate);

        requireActivity().getOnBackPressedDispatcher().addCallback(this.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(TrainingHistoryDetailsView.this)
                        .navigate(R.id.action_trainingHistoryDetailsView_to_trainingHistoryView, getArguments());
            }
        });

        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavHostFragment.findNavController(TrainingHistoryDetailsView.this)
                    .navigate(R.id.action_trainingHistoryDetailsView_to_trainingHistoryView, getArguments());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initTrainingDetailsView(ArrayList<Exercise> trainingDetails) {
        RecyclerView trainingHistoryDetailsRecyclerView =  binding.trainingHistoryDetailsRecyclerView;

        if (trainingHistoryDetailsRecyclerView == null) {
            return;
        }
        if (trainingDetails == null) {
            return;
        }

        trainingHistoryDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        TrainingHistoryDetailsRowAdapter trainingHistoryDetailsRowAdapter = new TrainingHistoryDetailsRowAdapter(trainingDetails, NavHostFragment.findNavController(TrainingHistoryDetailsView.this), getArguments().getString("trainingId"));
        trainingHistoryDetailsRecyclerView.setAdapter(trainingHistoryDetailsRowAdapter);
    }

    private void getTrainingDetailsApiCall(String trainingId) {
        ApiUtils.getApiService().getTrainingHistory(trainingId, getArguments().getLong("trainingDate"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<ArrayList<Exercise>>>() {

                    @Override
                    public void onNext(@NonNull Response<ArrayList<Exercise>> listResponse) {
                        if (ApiUtils.getResponseStatusCode(listResponse) == 200) {
                            initTrainingDetailsView(listResponse.body());
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