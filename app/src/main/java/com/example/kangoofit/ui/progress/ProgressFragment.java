package com.example.kangoofit.ui.progress;

import android.content.Intent;
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
import com.example.kangoofit.database.LoginActivity;
import com.example.kangoofit.database.UserManager;
import com.example.kangoofit.model.KangarooLevel; // Importă modelul de nivele
import com.example.kangoofit.model.User;
import com.google.firebase.auth.FirebaseAuth;

public class ProgressFragment extends Fragment {

    private ProgressBar progressBarPasi, progressBarCalorii;
    private TextView txtPasi, txtCalorii, txtState;
    private ImageView imgMascota;
    private LinearLayout containerLeaderboard;

    // 1. Adăugăm aceste variabile pentru a ține minte valorile curente
    private int pasiActuali = 0;
    private int targetActual = 0;
    private boolean isShowingDetail = false; // Pentru a preveni bug-urile la click-uri repetate

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // 1. Inițializăm elementele din UI
        progressBarPasi = view.findViewById(R.id.progressPasi);
        txtPasi = view.findViewById(R.id.txtPasi);
        progressBarCalorii = view.findViewById(R.id.progressCalorii);
        txtCalorii = view.findViewById(R.id.txtCalorii);
        txtState = view.findViewById(R.id.txtState);
        imgMascota = view.findViewById(R.id.imgMascota);
        containerLeaderboard = view.findViewById(R.id.container_leaderboard);

        // 2. Configurări inițiale
        txtPasi.setOnClickListener(v -> showStepDetails());

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {
                    int nivel = user.nivel_kangaroo > 0 ? user.nivel_kangaroo : 1;

                    // --- LOGICA PENTRU TARGET DINAMIC ---
                    // Luăm target-ul de pași specific nivelului curent (Type 2 = Pasi)
                    pasiActuali = user.pasi;
                    targetActual = KangarooLevel.getRequirement(nivel, 2);

                    progressBarPasi.setMax(targetActual);
                    progressBarPasi.setProgress(pasiActuali);

                    // Dacă nu suntem în modul de detalii, afișăm doar pașii
                    if (!isShowingDetail) {
                        txtPasi.setText(String.valueOf(pasiActuali));
                    }

                    // Logica Calorii (Calcul estimativ)
                    // Formula: pasi*0.04 + flotari*0.5 + genoflexiuni*0.4
                    int caloriiArse = (int) ((user.pasi * 0.04) + (user.flotari * 0.5) + (user.genoflexiuni * 0.4));
                    int targetCalorii = 500; // Target static sau calculat din KangarooLevel
                    progressBarCalorii.setMax(targetCalorii);
                    progressBarCalorii.setProgress(caloriiArse);
                    txtCalorii.setText(String.valueOf(caloriiArse));

                    // Mascota si Stare
                    imgMascota.setImageResource(KangarooLevel.getDrawableResId(nivel));
                    int[] stats = new int[]{user.genoflexiuni, user.flotari, user.pasi};
                    float progress = KangarooLevel.getOverallProgress(nivel, stats);

                    String mood;
                    if (progress < 0.3f) mood = "SAD";
                    else if (progress < 0.7f) mood = "NEUTRAL";
                    else mood = "HAPPY";
                    txtState.setText("State: " + mood);

                    loadLeaderboard();
                }
            });
        }

        loadLeaderboard();
        return view;
    }

    private void showStepDetails() {
        if (isShowingDetail) return;
        isShowingDetail = true;
        txtPasi.setText(pasiActuali + " / " + targetActual);
        txtPasi.postDelayed(() -> {
            if (isAdded()) {
                txtPasi.setText(String.valueOf(pasiActuali));
                isShowingDetail = false;
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