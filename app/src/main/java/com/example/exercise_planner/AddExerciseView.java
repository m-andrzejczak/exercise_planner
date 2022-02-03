package com.example.exercise_planner;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.example.exercise_planner.api.ApiUtils;
import com.example.exercise_planner.databinding.AddExerciseViewBinding;
import com.example.exercise_planner.databinding.AddTrainingViewBinding;
import com.example.exercise_planner.databinding.TrainingViewBinding;
import com.example.exercise_planner.model.Exercise;
import com.example.exercise_planner.model.Training;

import java.math.BigInteger;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Formatter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddExerciseView extends Fragment {
    private AddExerciseViewBinding binding;
    private ArrayList<Exercise> allExercisesList = new ArrayList<>();
    private Exercise selectedExercise = new Exercise();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        setHasOptionsMenu(true);
        binding = AddExerciseViewBinding.inflate(inflater, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(this.getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(AddExerciseView.this)
                        .navigate(R.id.action_addExerciseView_to_exerciseView, getArguments());
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (getArguments().getString("exerciseId") != null) menu.add("Delete exercise type");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        CharSequence title = item.getTitle();
        if (title != null) {
            if (title.equals("Delete exercise type")) {
                new AlertDialog.Builder(getContext()).setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete training")
                        .setMessage("Are you sure you want to delete '" + binding.exerciseNameTextInput.getText().toString() + "'?\n" +
                                "It will disappear from all your trainings.")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteExerciseApiCall();
                                NavHostFragment.findNavController(AddExerciseView.this)
                                        .navigate(R.id.action_addExerciseView_to_exerciseView, getArguments());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.timeHoursInput.setFilters(new InputFilter[]{new InputFilterMinMax("0", "24")});
        binding.timeMinutesInput.setFilters(new InputFilter[]{new InputFilterMinMax("0", "59")});
        binding.timeSecondsInput.setFilters(new InputFilter[]{new InputFilterMinMax("0", "59")});
        binding.numberOfRepsInput.setFilters(new InputFilter[]{new InputFilterMinMax("0", "1000")});
        binding.timeHoursInput.setText("0");
        binding.timeMinutesInput.setText("0");
        binding.timeSecondsInput.setText("0");
        binding.numberOfRepsInput.setText("0");
        binding.removeFromTrainingExerciseButton.setVisibility(View.GONE);

        binding.saveExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.radioButtonExisting.isChecked()) {
                    addNewExerciseToTrainingApiCall();
                } else {
                    createExerciseApiCall();
                }
            }
        });

        binding.newOrExistingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (binding.radioButtonNew.isChecked()) {
                    binding.newExerciseLayout.setVisibility(View.VISIBLE);
                    binding.existingExercisesSpinnerLayout.setVisibility(View.GONE);
                } else {
                    binding.newExerciseLayout.setVisibility(View.GONE);
                    binding.existingExercisesSpinnerLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.timeOrRepsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (binding.radioButtonTime.isChecked()) {
                    binding.timeInputLayout.setVisibility(View.VISIBLE);
                    binding.repsInputLayout.setVisibility(View.GONE);
                    binding.numberOfRepsInput.setText("0");
                } else {
                    binding.timeInputLayout.setVisibility(View.GONE);
                    binding.repsInputLayout.setVisibility(View.VISIBLE);
                    binding.timeHoursInput.setText("0");
                    binding.timeMinutesInput.setText("0");
                    binding.timeSecondsInput.setText("0");
                }
            }
        });

        binding.radioButtonReps.setChecked(true);
        binding.radioButtonExisting.setChecked(true);
        getExercisesApiCall();
    }

    private Exercise getExerciseFormData() {
        Exercise exercise = new Exercise();
        exercise.setId(getArguments().getString("exerciseId"));
        exercise.setName(binding.exerciseNameTextInput.getText().toString());
        exercise.setReps(Integer.parseInt(binding.numberOfRepsInput.getText().toString()));
        exercise.setTime(Integer.parseInt(binding.timeHoursInput.getText().toString())*60*60+
                Integer.parseInt(binding.timeMinutesInput.getText().toString())*60+
                Integer.parseInt(binding.timeSecondsInput.getText().toString()));
        exercise.setNotes(binding.multilineNotesTextInput.getText().toString());
        exercise.setAutostart(Boolean.parseBoolean(binding.autostartSwitch.getText().toString()));
        return exercise;
    }

    private void createExerciseApiCall() {
        String trainingId = getArguments().getString("trainingId");
        Exercise exercise = getExerciseFormData();

        ApiUtils.getApiService().createExercise(trainingId, exercise.getName(), exercise.getTime(), exercise.getReps(), exercise.getNotes(), exercise.getAutostart())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (ApiUtils.getResponseStatusCode(response) == 200) {
                            NavHostFragment.findNavController(AddExerciseView.this)
                                    .navigate(R.id.action_addExerciseView_to_exerciseView, getArguments());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }

    private void setupForEditing() {
        Exercise currentExercise = null;

        for (Exercise ex : allExercisesList) {
            if (ex.getId().equals(getArguments().getString("exerciseId"))) {
                currentExercise = ex;
                break;
            }
        }

        if (getArguments().getString("exerciseId") != null) {
            getActivity().setTitle(currentExercise.getName());
        } else {
            getActivity().setTitle(R.string.add_exercise_view_label);
        }

        binding.removeFromTrainingExerciseButton.setVisibility(View.VISIBLE);
        binding.newOrExistingRadioGroup.setVisibility(View.GONE);
        binding.existingExercisesSpinnerLayout.setVisibility(View.GONE);
        binding.newExerciseLayout.setVisibility(View.VISIBLE);
        binding.exerciseNameTextInput.setText(currentExercise.getName());
        binding.autostartSwitch.setChecked(currentExercise.getAutostart());
        binding.multilineNotesTextInput.setText(currentExercise.getNotes());

        if (currentExercise.getTime() != 0) {
            binding.radioButtonTime.setChecked(true);
            binding.timeHoursInput.setText(String.valueOf(currentExercise.getTime() / 60 / 60));
            binding.timeMinutesInput.setText(String.valueOf(currentExercise.getTime() / 60 % 60));
            binding.timeSecondsInput.setText(String.valueOf(currentExercise.getTime() % 60 % 60));
        } else {
            binding.radioButtonReps.setChecked(true);
            binding.numberOfRepsInput.setText(String.valueOf(currentExercise.getReps()));
        }

        binding.saveExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateExerciseApiCall();
            }
        });

        binding.removeFromTrainingExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTrainingsExerciseListApiCall();
            }
        });
    }

    private void updateExerciseApiCall() {
        Exercise exercise = getExerciseFormData();
        ApiUtils.getApiService().updateExercise(exercise.getId(), exercise)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (ApiUtils.getResponseStatusCode(response) == 200) {
                            NavHostFragment.findNavController(AddExerciseView.this)
                                    .navigate(R.id.action_addExerciseView_to_exerciseView, getArguments());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }

    private void deleteExerciseApiCall() {
        ApiUtils.getApiService().deleteExercise(getArguments().getString("exerciseId"))
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (ApiUtils.getResponseStatusCode(response) == 200) {
                            NavHostFragment.findNavController(AddExerciseView.this)
                                    .navigate(R.id.action_addExerciseView_to_exerciseView, getArguments());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {
                        Log.d("ERROR", e.getMessage());
                    }
                });
    }

    private void getExercisesApiCall() {
        ApiUtils.getApiService().getExercises()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<ArrayList<Exercise>>>() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onNext(@NonNull Response<ArrayList<Exercise>> exercisesList) {
                        if (ApiUtils.getResponseStatusCode(exercisesList) == 200) {
                            ArrayList<String> choices = new ArrayList<>();
                            allExercisesList = exercisesList.body();
                            for (Exercise ex: allExercisesList) {
                                String displayProperty = String.format("x%d", ex.getReps());
                                Integer time = ex.getTime();
                                if (time != 0) {
                                    LocalTime localTime = LocalTime.of(time / 60 / 60, time / 60 % 60, time % 60 % 60);
                                    String timeFormatted = DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
                                    displayProperty = String.format("%s", timeFormatted);
                                }
                                choices.add(ex.getName() + " - " + displayProperty);
                            }
                            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_expandable_list_item_1, choices);
                            binding.existingExercisesSpinner.setAdapter(spinnerAdapter);
                            binding.existingExercisesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selectedExercise = allExercisesList.get(i);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                    binding.existingExercisesSpinner.setSelection(0);
                                }
                            });
                            if (getArguments().getString("exerciseId") != null) {
                                setupForEditing();
                            }
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

    private void addNewExerciseToTrainingApiCall() {
        ApiUtils.getApiService().getTraining(getArguments().getString("trainingId"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<Training>>() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onNext(@NonNull Response<Training> trainingResponse) {
                        if (ApiUtils.getResponseStatusCode(trainingResponse) == 200) {
                            Training training = trainingResponse.body();
                            BigInteger newExerciseId = new BigInteger(selectedExercise.getId());
                            ArrayList<BigInteger> exerciseIds = training.getExercises();
                            exerciseIds.add(newExerciseId);
                            training.setExercises(exerciseIds);

                            ApiUtils.getApiService().updateTraining(training.getId(), training)
                                    .enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (ApiUtils.getResponseStatusCode(response) == 200) {
                                                NavHostFragment.findNavController(AddExerciseView.this)
                                                        .navigate(R.id.action_addExerciseView_to_exerciseView, getArguments());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

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

    private void updateTrainingsExerciseListApiCall() {
        ApiUtils.getApiService().getTraining(getArguments().getString("trainingId"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<Training>>() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onNext(@NonNull Response<Training> trainingResponse) {
                        if (ApiUtils.getResponseStatusCode(trainingResponse) == 200) {
                            Training training = trainingResponse.body();
                            ArrayList<BigInteger> exerciseIds = training.getExercises();
                            exerciseIds.remove(getArguments().getInt("exerciseIdx"));
                            training.setExercises(exerciseIds);

                            ApiUtils.getApiService().updateTraining(training.getId(), training)
                                    .enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (ApiUtils.getResponseStatusCode(response) == 200) {
                                                NavHostFragment.findNavController(AddExerciseView.this)
                                                        .navigate(R.id.action_addExerciseView_to_exerciseView, getArguments());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

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

    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

}