package com.example.exercise_planner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.exercise_planner.api.ApiUtils;
import com.example.exercise_planner.databinding.ExerciseViewBinding;
import com.example.exercise_planner.databinding.RunTrainingViewBinding;
import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.Training;
import com.example.exercise_planner.model.TrainingLog;

import java.math.BigInteger;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RunTrainingView extends Fragment {

    private ArrayList<Exercise> exercises;
    private Integer currentIndex = 0;
    private Exercise currentExercise = null;
    private CountDownTimer currentAutostartTimer = null;
    private CountDownTimer currentExerciseTimer = null;
    private Integer currentTimeInSeconds = 0;
    private RunTrainingViewBinding binding;
    private ArrayList<Exercise> trainingHistory = new ArrayList<>();
    private long trainingStartDateTime = 0;
    private long trainingEndDateTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            interruptTraining();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        trainingStartDateTime = System.currentTimeMillis();

        binding = RunTrainingViewBinding.inflate(inflater, container, false);

        getExercisesApiCall(getArguments().getString("trainingId"));

        getActivity().setTitle(getArguments().getString("trainingName"));

        requireActivity().getOnBackPressedDispatcher().addCallback(this.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                interruptTraining();
            }
        });

        binding.exerciseCtrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonState = binding.exerciseCtrlButton.getText().toString();

                if (buttonState.equals("START")) {
                    if (currentAutostartTimer != null) currentAutostartTimer.cancel();
                    binding.exerciseAutostartTimerTextView.setVisibility(View.GONE);
                    binding.exerciseAutostartInfoTextView.setVisibility(View.GONE);
                    binding.exerciseCtrlButton.setText("PAUSE");
                    currentExerciseTimer.start();
                } else if (buttonState.equals("PAUSE")) {
                    pauseExercise();
                } else if (buttonState.equals("DONE")) {
                    nextExercise();
                } else if (buttonState.equals("RESUME")) {
                    resumeExercise();
                }
            }
        });

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentExerciseTimer != null) pauseExercise();

                new AlertDialog.Builder(getContext())
                        .setTitle("Skip exercise")
                        .setMessage("Do you want to skip this exercise?")
                        .setPositiveButton("Skip", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                nextExercise();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentExerciseTimer != null) pauseExercise();

                new AlertDialog.Builder(getContext())
                        .setTitle("To previous exercise")
                        .setMessage("Do you want to go back to the previous exercise?")
                        .setPositiveButton("Go back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                previousExercise();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.addRepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentExercise.getReps() < 1000) {
                    currentExercise.setReps(currentExercise.getReps()+1);
                    binding.exerciseParameterTextView.setText(String.valueOf(currentExercise.getReps()));
                };
            }
        });

        binding.substractRepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentExercise.getReps() > 0) {
                    currentExercise.setReps(currentExercise.getReps()-1);
                    binding.exerciseParameterTextView.setText(String.valueOf(currentExercise.getReps()));
                };
            }
        });

        return binding.getRoot();
    }

    public void createExerciseTimer(Integer time) {
        currentExerciseTimer = new CountDownTimer(time, 1000) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTick(long l) {
                Integer timeInSeconds = Math.toIntExact(l) / 1000;
                LocalTime localTime = LocalTime.of(timeInSeconds / 60 / 60, timeInSeconds / 60 % 60, timeInSeconds % 60 % 60);
                String timeFormatted = DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
                currentTimeInSeconds = timeInSeconds;
                binding.exerciseParameterTextView.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                nextExercise();
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateView() {
        binding.exerciseTitleTextView.setText(currentExercise.getName());

        if (currentIndex <= 0) {
            binding.previousButton.setVisibility(View.GONE);
        } else {
            binding.previousButton.setVisibility(View.VISIBLE);
        }

        if (currentIndex >= exercises.size()-1) {
            binding.nextButton.setVisibility(View.GONE);
        } else {
            binding.nextButton.setVisibility(View.VISIBLE);
        }

        if (currentExercise.getTime() != 0) {
            binding.addRepButton.setVisibility(View.GONE);
            binding.substractRepButton.setVisibility(View.GONE);
            binding.exerciseParameterInfoTextView.setText("Time remaining:");
            binding.exerciseCtrlButton.setText("START");
            LocalTime initialLocalTime = LocalTime.of(currentExercise.getTime() / 60 / 60, currentExercise.getTime() / 60 % 60, currentExercise.getTime() % 60 % 60);
            String initialTimeFormatted = DateTimeFormatter.ISO_LOCAL_TIME.format(initialLocalTime);
            binding.exerciseParameterTextView.setText(initialTimeFormatted);
            createExerciseTimer(currentExercise.getTime()*1000);
        } else {
            binding.exerciseParameterInfoTextView.setText("Reps:");
            binding.exerciseParameterTextView.setText(String.valueOf(currentExercise.getReps()));
            binding.exerciseCtrlButton.setText("DONE");
            binding.addRepButton.setVisibility(View.VISIBLE);
            binding.substractRepButton.setVisibility(View.VISIBLE);
        }

        if (currentExercise.getAutostart()) {
            binding.exerciseAutostartInfoTextView.setVisibility(View.VISIBLE);
            binding.exerciseAutostartTimerTextView.setVisibility(View.VISIBLE);
            currentAutostartTimer = new CountDownTimer(15*1000, 1000) {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onTick(long l) {
                    Integer timeInSeconds = Math.toIntExact(l) / 1000;
                    binding.exerciseParameterTextView.setText(timeInSeconds.toString());
                }

                @Override
                public void onFinish() {
                    binding.exerciseAutostartInfoTextView.setVisibility(View.GONE);
                    binding.exerciseAutostartTimerTextView.setVisibility(View.GONE);
                    binding.exerciseCtrlButton.setText("PAUSE");
                    currentExerciseTimer.start();
                }
            };
        } else {
            binding.exerciseAutostartInfoTextView.setVisibility(View.GONE);
            binding.exerciseAutostartTimerTextView.setVisibility(View.GONE);
        }
    }

    private void pauseExercise() {
        currentExerciseTimer.cancel();
        currentExerciseTimer = null;
        binding.exerciseCtrlButton.setText("RESUME");
    }

    private void resumeExercise() {
        createExerciseTimer(currentTimeInSeconds*1000);
        currentExerciseTimer.start();
        binding.exerciseCtrlButton.setText("PAUSE");
    }

    private void finishTraining() {
        binding.exerciseTitleTextView.setVisibility(View.GONE);
        binding.mainExerciseLayout.setVisibility(View.GONE);
        binding.nextButtonLayout.setVisibility(View.GONE);
        binding.previousButtonLayout.setVisibility(View.GONE);
        binding.exerciseAutostartInfoTextView.setVisibility(View.GONE);
        binding.exerciseAutostartTimerTextView.setVisibility(View.GONE);
        binding.congratulationsLayout.setVisibility(View.VISIBLE);
        trainingEndDateTime = System.currentTimeMillis();
        saveTrainingToHistoryApiCall();
        binding.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(RunTrainingView.this)
                        .navigate(R.id.action_runTrainingView_to_exerciseView, getArguments());
            }
        });
    }

    private void interruptTraining() {
        if (currentExerciseTimer != null) pauseExercise();
        new AlertDialog.Builder(getContext())
                .setTitle("Exit training")
                .setMessage("Do you want to exit this training?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (currentTimeInSeconds != 0) {
                            currentExercise.setTime(currentExercise.getTime()-currentTimeInSeconds);
                        }
                        trainingHistory.add(currentExercise);
                        trainingEndDateTime = System.currentTimeMillis();
                        saveTrainingToHistoryApiCall();
                        NavHostFragment.findNavController(RunTrainingView.this)
                                .navigate(R.id.action_runTrainingView_to_exerciseView, getArguments());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("NewApi")
    private void nextExercise() {
        if (currentTimeInSeconds != 0) {
            currentExercise.setTime(currentExercise.getTime()-currentTimeInSeconds);
        }

        trainingHistory.add(currentExercise);

        if (currentIndex == exercises.size()-1) {
            finishTraining();
            return;
        }

        currentIndex++;
        currentExercise = exercises.get(currentIndex);
        currentExerciseTimer = null;
        currentAutostartTimer = null;
        currentTimeInSeconds = 0;
        updateView();
    }

    @SuppressLint("NewApi")
    private void previousExercise() {
        if (currentTimeInSeconds != 0) {
            currentExercise.setTime(currentExercise.getTime()-currentTimeInSeconds);
        }

        trainingHistory.add(currentExercise);

        if (currentIndex == 0) {
            return;
        }

        currentIndex--;
        currentExercise = exercises.get(currentIndex);
        currentExerciseTimer = null;
        currentAutostartTimer = null;
        currentTimeInSeconds = 0;
        updateView();
    }

    private void getExercisesApiCall(String trainingId) {
        ApiUtils.getApiService().getExercisesByTrainingId(trainingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<ArrayList<Exercise>>>() {

                    @SuppressLint("NewApi")
                    @Override
                    public void onNext(@NonNull Response<ArrayList<Exercise>> listResponse) {
                        if (ApiUtils.getResponseStatusCode(listResponse) == 200) {
                            exercises = listResponse.body();
                            currentExercise = exercises.get(currentIndex);
                            updateView();
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

    private void saveTrainingToHistoryApiCall() {
        TrainingLog trainingLog = new TrainingLog(trainingStartDateTime, trainingEndDateTime, trainingHistory);
        ApiUtils.getApiService().saveTrainingToHistory(getArguments().getString("trainingId"), trainingLog)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (ApiUtils.getResponseStatusCode(response) == 200) {

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }
}