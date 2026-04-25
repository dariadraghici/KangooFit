package com.example.kangoofit.ui.exercises;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.kangoofit.R;

public class ExercisesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        // Găsim item-urile din listă
        CardView itemFlotari = view.findViewById(R.id.item_flotari);
        CardView itemTractiuni = view.findViewById(R.id.item_tractiuni);
        CardView itemGenuflexiuni = view.findViewById(R.id.item_genuflexiuni);

        // Opțional: Găsim butonul mare rotund dacă vrei să facă și el ceva
        // ImageView btnBigCamera = view.findViewById(R.id.btn_big_camera);

        // Setăm click-urile
        itemFlotari.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", "PUSHUPS");
            startActivity(intent);
        });

        itemGenuflexiuni.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", "SQUATS");
            startActivity(intent);
        });

        itemTractiuni.setOnClickListener(v -> {
            // Nu ai implementat încă logica AI pentru tracțiuni în CameraExerciseActivity,
            // așa că momentan punem doar un mesaj.
            Toast.makeText(getContext(), "Logica AI pentru tracțiuni urmează!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}