package com.example.kangoofit.ui.exercises;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.kangoofit.R;
import com.example.kangoofit.database.UserManager;
import com.example.kangoofit.model.KangarooLevel;
import com.google.firebase.auth.FirebaseAuth;

public class ExercisesFragment extends Fragment {

    private TextView tvKangarooStateDesc;
    private ImageView imgKangarooState;
    private int userLevel = 1; // Nivelul default

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        CardView itemFlotari = view.findViewById(R.id.item_flotari);
        CardView itemGenuflexiuni = view.findViewById(R.id.item_genuflexiuni);
        tvKangarooStateDesc = view.findViewById(R.id.tv_kangaroo_state_desc);
        imgKangarooState = view.findViewById(R.id.img_kangaroo_state);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {
                    userLevel = user.nivel_kangaroo > 0 ? user.nivel_kangaroo : 1;
                    int[] stats = new int[]{user.genoflexiuni, user.flotari, user.pasi};

                    float overallProgress = KangarooLevel.getOverallProgress(userLevel, stats);
                    updateKangarooStateUI(overallProgress, userLevel);
                }
            });
        }

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

        return view;
    }

    private void updateKangarooStateUI(float progress, int level) {
        if (!isAdded()) return;

        String mood;
        String description;

        if (progress < 0.3f) {
            mood = "sad";
            description = "Your kangaroo is SAD.\nTime to change that!";
        } else if (progress < 0.7f) {
            mood = "neutral";
            description = "Your kangaroo is NEUTRAL.\nTime to change that!";
        } else {
            mood = "happy";
            description = "Your kangaroo is HAPPY!\nKeep it up!";
        }

        tvKangarooStateDesc.setText(description);

        // Construim numele resursei dinamic: kangaroo_mood_level (ex: kangaroo_happy_1)
        String imageName = "kangaroo_" + mood + "_" + level;
        int resId = getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());

        if (resId != 0) {
            imgKangarooState.setImageResource(resId);
        } else {
            // Fallback în cazul în care imaginea specifică lipsește
            imgKangarooState.setImageResource(R.drawable.kangaroo_neutral);
        }
    }
}