package com.example.kangoofit.ui.exercises;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.kangoofit.R;

public class ExercisesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        Button btnFlotari = view.findViewById(R.id.btn_flotari);
        Button btnGenuflexiuni = view.findViewById(R.id.btn_genuflexiuni);

        btnFlotari.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", "PUSHUPS");
            startActivity(intent);
        });

        btnGenuflexiuni.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", "SQUATS");
            startActivity(intent);
        });

        return view;
    }
}