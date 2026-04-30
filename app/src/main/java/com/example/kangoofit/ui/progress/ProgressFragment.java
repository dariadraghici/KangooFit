package com.example.kangoofit.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kangoofit.R;
import com.example.kangoofit.database.UserManager;
import com.example.kangoofit.model.KangarooLevel;
import com.example.kangoofit.model.User;
import com.google.firebase.auth.FirebaseAuth;

public class ProgressFragment extends Fragment {

    private ProgressBar progressBarPasi, progressBarCalorii;
    private TextView txtPasi, txtCalorii;
    private ImageView imgMascota;
    private LinearLayout containerLeaderboard;

    // Variabile pentru Pași
    private int pasiActuali = 0;
    private int targetPasiActual = 0;
    private boolean isShowingStepDetail = false;

    // Variabile pentru Calorii
    private int caloriiActuale = 0;
    private final int targetCaloriiStatic = 500; // Target-ul cerut de tine
    private boolean isShowingCalorieDetail = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // 1. Inițializăm elementele din UI
        progressBarPasi = view.findViewById(R.id.progressPasi);
        txtPasi = view.findViewById(R.id.txtPasi);
        progressBarCalorii = view.findViewById(R.id.progressCalorii);
        txtCalorii = view.findViewById(R.id.txtCalorii);
        imgMascota = view.findViewById(R.id.imgMascota);
        containerLeaderboard = view.findViewById(R.id.container_leaderboard);

        // 2. Setăm Click Listeners pentru efectul de "Peek"
        txtPasi.setOnClickListener(v -> showStepDetails());
        txtCalorii.setOnClickListener(v -> showCalorieDetails());

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {
                    int nivel = user.nivel_kangaroo > 0 ? user.nivel_kangaroo : 1;

                    // --- LOGICA PAȘI ---
                    pasiActuali = user.pasi;
                    targetPasiActual = KangarooLevel.getRequirement(nivel, 2);
                    progressBarPasi.setMax(targetPasiActual);
                    progressBarPasi.setProgress(pasiActuali);

                    if (!isShowingStepDetail) {
                        txtPasi.setText(String.valueOf(pasiActuali));
                    }

                    // --- LOGICA CALORII ---
                    // Formula actualizată cu toate exercițiile
                    caloriiActuale = (int) (
                            (user.pasi * 0.04) +
                                    (user.flotari * 0.5) +
                                    (user.genoflexiuni * 0.4) +
                                    (user.jumping_jacks * 0.2) +
                                    (user.biceps * 0.15) +
                                    (user.umeri * 0.15)
                    );

                    progressBarCalorii.setMax(targetCaloriiStatic);
                    progressBarCalorii.setProgress(caloriiActuale);

                    if (!isShowingCalorieDetail) {
                        txtCalorii.setText(String.valueOf(caloriiActuale));
                    }

                    // Actualizăm restul UI-ului
                    imgMascota.setImageResource(KangarooLevel.getDrawableResId(nivel));
                    loadLeaderboard();
                }
            });
        }

        loadLeaderboard();
        return view;
    }

    // Funcția pentru afișare detalii Pași (2 secunde)
    private void showStepDetails() {
        if (isShowingStepDetail) return;
        isShowingStepDetail = true;
        txtPasi.setText(pasiActuali + " / " + targetPasiActual);
        txtPasi.postDelayed(() -> {
            if (isAdded()) {
                txtPasi.setText(String.valueOf(pasiActuali));
                isShowingStepDetail = false;
            }
        }, 2000);
    }

    // Funcția pentru afișare detalii Calorii (2 secunde)
    private void showCalorieDetails() {
        if (isShowingCalorieDetail) return;
        isShowingCalorieDetail = true;
        txtCalorii.setText(caloriiActuale + " / " + targetCaloriiStatic);
        txtCalorii.postDelayed(() -> {
            if (isAdded()) {
                txtCalorii.setText(String.valueOf(caloriiActuale));
                isShowingCalorieDetail = false;
            }
        }, 2000);
    }

    private void loadLeaderboard() {
        UserManager.getInstance().getTopUsers(users -> {
            if (!isAdded()) return;
            users.sort((u1, u2) -> Float.compare(calculatePoints(u2), calculatePoints(u1)));

            containerLeaderboard.removeAllViews();
            int topCount = Math.min(users.size(), 3);

            for (int i = 0; i < topCount; i++) {
                User user = users.get(i);
                View row = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                TextView text1 = row.findViewById(android.R.id.text1);
                TextView text2 = row.findViewById(android.R.id.text2);

                String medal = (i == 0) ? "🥇 " : (i == 1) ? "🥈 " : "🥉 ";
                String displayName = (user.name != null && !user.name.isEmpty()) ? user.name : getString(R.string.label_anonymous);

                text1.setText(medal + displayName);
                text1.setTypeface(null, android.graphics.Typeface.BOLD);
                text2.setText("Level " + user.nivel_kangaroo + " • " + (int)calculatePoints(user) + " points");

                containerLeaderboard.addView(row);
            }
        });
    }

    private float calculatePoints(User u) {
        int[] stats = {u.genoflexiuni, u.flotari, u.pasi};
        float currentProgress = KangarooLevel.getOverallProgress(u.nivel_kangaroo, stats);
        return (u.nivel_kangaroo * 1000) + (currentProgress * 1000);
    }
}