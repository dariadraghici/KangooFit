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
    private ImageView imgKangarooSprite;
    private int userLevel = 1;

    // Variabilele noi pentru numărul de repetări
    private TextView tvTotalFlotari, tvTotalGenuflexiuni, tvTotalJacks, tvTotalBiceps, tvTotalUmeri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        // Găsim cardurile
        CardView itemFlotari = view.findViewById(R.id.item_flotari);
        CardView itemGenuflexiuni = view.findViewById(R.id.item_genuflexiuni);
        CardView itemJumpingJacks = view.findViewById(R.id.item_jumping_jacks);
        CardView itemBiceps = view.findViewById(R.id.item_biceps);
        CardView itemShoulder = view.findViewById(R.id.item_shoulder);

        tvKangarooStateDesc = view.findViewById(R.id.tv_kangaroo_state_desc);
        imgKangarooSprite = view.findViewById(R.id.img_kangaroo_sprite);

        // Găsim TextView-urile pentru totaluri
        tvTotalFlotari = view.findViewById(R.id.tv_total_flotari);
        tvTotalGenuflexiuni = view.findViewById(R.id.tv_total_genuflexiuni);
        tvTotalJacks = view.findViewById(R.id.tv_total_jacks);
        tvTotalBiceps = view.findViewById(R.id.tv_total_biceps);
        tvTotalUmeri = view.findViewById(R.id.tv_total_umeri);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {
                    // 1. Actualizăm starea și progresul Cangurului
                    userLevel = user.nivel_kangaroo > 0 ? user.nivel_kangaroo : 1;
                    int[] stats = new int[]{user.genoflexiuni, user.flotari, user.pasi};
                    float overallProgress = KangarooLevel.getOverallProgress(userLevel, stats);
                    updateKangarooStateUI(overallProgress, userLevel);

                    // 2. Actualizăm textele cu numărul total de repetări pe fiecare card
                    tvTotalFlotari.setText(String.valueOf(user.flotari));
                    tvTotalGenuflexiuni.setText(String.valueOf(user.genoflexiuni));
                    tvTotalJacks.setText(String.valueOf(user.jumping_jacks));
                    tvTotalBiceps.setText(String.valueOf(user.biceps));
                    tvTotalUmeri.setText(String.valueOf(user.umeri));
                }
            });
        }

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

        itemJumpingJacks.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", "JUMPING_JACKS");
            startActivity(intent);
        });

        itemBiceps.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", "BICEP_CURLS");
            startActivity(intent);
        });

        itemShoulder.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", "SHOULDER_PRESS");
            startActivity(intent);
        });

        return view;
    }

    private void setupClickListeners(View... views) {
        String[] types = {"PUSHUPS", "SQUATS", "JUMPING_JACKS", "BICEP_CURLS", "SHOULDER_PRESS"};
        for (int i = 0; i < views.length; i++) {
            final String type = types[i];
            views[i].setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
                intent.putExtra("EXERCISE_TYPE", type);
                startActivity(intent);
            });
        }
    }

    private void updateKangarooStateUI(float progress, int level) {
        if (!isAdded()) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {
                    long currentTime = System.currentTimeMillis();
                    long fiveMinutesInMillis = 5 * 60 * 1000; // 300.000 ms

                    String mood;
                    String stareText;

                    if (currentTime - user.lastExerciseTimestamp <= fiveMinutesInMillis) {
                        mood = "HAPPY";
                    } else {
                        mood = "SAD";
                    }

                    user.stare_kangaroo = mood;

                    String description = mood.toUpperCase().equals("HAPPY") ? "Your kangaroo is HAPPY!\nKeep it up!" : "Your kangaroo is " + mood.toUpperCase() + ".\nTime to change that!";

                    tvKangarooStateDesc.setText(description);
                    String imageName = "kangaroo_" + mood + "_" + level;
                    int resId = getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());
                    imgKangarooSprite.setImageResource(resId != 0 ? resId : R.drawable.kangaroo_neutral);
                }
            });
        }
    }
}