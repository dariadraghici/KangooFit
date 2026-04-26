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
import com.example.kangoofit.model.User;
import com.google.firebase.auth.FirebaseAuth;

public class ExercisesFragment extends Fragment {

    private TextView tvKangarooStateDesc;
    private ImageView imgKangarooSprite;
    private TextView tvTotalFlotari, tvTotalGenuflexiuni, tvTotalJacks, tvTotalBiceps, tvTotalUmeri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        // Inițializare UI
        imgKangarooSprite = view.findViewById(R.id.img_kangaroo_sprite);
        tvKangarooStateDesc = view.findViewById(R.id.tv_kangaroo_state_desc);

        tvTotalFlotari = view.findViewById(R.id.tv_total_flotari);
        tvTotalGenuflexiuni = view.findViewById(R.id.tv_total_genuflexiuni);
        tvTotalJacks = view.findViewById(R.id.tv_total_jacks);
        tvTotalBiceps = view.findViewById(R.id.tv_total_biceps);
        tvTotalUmeri = view.findViewById(R.id.tv_total_umeri);

        // Carduri
        CardView itemFlotari = view.findViewById(R.id.item_flotari);
        CardView itemGenuflexiuni = view.findViewById(R.id.item_genuflexiuni);
        CardView itemJacks = view.findViewById(R.id.item_jumping_jacks);
        CardView itemBiceps = view.findViewById(R.id.item_biceps);
        CardView itemShoulder = view.findViewById(R.id.item_shoulder);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {
                    updateUI(user);
                }
            });
        }

        // Click Listeners (am refolosit logica ta)
        itemFlotari.setOnClickListener(v -> startExercise("PUSHUPS"));
        itemGenuflexiuni.setOnClickListener(v -> startExercise("SQUATS"));
        itemJacks.setOnClickListener(v -> startExercise("JUMPING_JACKS"));
        itemBiceps.setOnClickListener(v -> startExercise("BICEP_CURLS"));
        itemShoulder.setOnClickListener(v -> startExercise("SHOULDER_PRESS"));

        return view;
    }

    private void updateUI(User user) {
        // 1. Calculăm Mood-ul (Bazat pe ultimele 5 minute)
        long currentTime = System.currentTimeMillis();
        long fiveMinutes = 5 * 60 * 1000;

        long diff = currentTime - user.lastExerciseTimestamp;
        android.util.Log.d("DEBUG_KANGAROO", "Current: " + currentTime + " | Last: " + user.lastExerciseTimestamp + " | Diff: " + diff);

        // Decidem starea: HAPPY dacă s-a antrenat recent, altfel SAD
        String mood = (currentTime - user.lastExerciseTimestamp <= fiveMinutes) ? "happy" : "sad";

        // 2. Limităm nivelul între 1 și 6 (pentru că atâtea poze ai)
        int level = Math.max(1, Math.min(user.nivel_kangaroo, 6));

        // 3. Actualizăm Imaginea din Banner
        // Caută resurse de tip: kangaroo_happy_1, kangaroo_sad_2, etc.
        String resourceName = "kangaroo_" + mood + "_" + level;
        int resId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());

        if (resId != 0) {
            imgKangarooSprite.setImageResource(resId);
        } else {
            imgKangarooSprite.setImageResource(R.drawable.kangaroo_neutral);
        }

        // 4. Actualizăm Descrierea
        String desc = mood.equals("happy") ?
                "Your kangaroo is HAPPY!\nKeep training!" :
                "Your kangaroo is SAD...\nTime to move!";
        tvKangarooStateDesc.setText(desc);

        // 5. Actualizăm Contoarele
        tvTotalFlotari.setText(String.valueOf(user.flotari));
        tvTotalGenuflexiuni.setText(String.valueOf(user.genoflexiuni));
        tvTotalJacks.setText(String.valueOf(user.jumping_jacks));
        tvTotalBiceps.setText(String.valueOf(user.biceps));
        tvTotalUmeri.setText(String.valueOf(user.umeri));
    }

    private void startExercise(String type) {
        Intent intent = new Intent(getActivity(), CameraExerciseActivity.class);
        intent.putExtra("EXERCISE_TYPE", type);
        startActivity(intent);
    }
}