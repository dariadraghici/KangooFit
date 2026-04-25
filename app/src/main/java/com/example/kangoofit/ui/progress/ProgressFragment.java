package com.example.kangoofit.ui.progress;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView; // Import necesar
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

    private ProgressBar progressBarPasi;
    private TextView txtPasi;
    private TextView txtFlotari;
    private TextView txtGenoflexiuni;
    private ImageView imgMascota; // Variabilă pentru imaginea de banner

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
        txtFlotari = view.findViewById(R.id.txtFlotari);
        txtGenoflexiuni = view.findViewById(R.id.txtGenoflexiuni);
        imgMascota = view.findViewById(R.id.imgMascota); // ID-ul din XML-ul tău

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


                    // Actualizăm restul UI-ului
                    imgMascota.setImageResource(KangarooLevel.getDrawableResId(nivel));
                    txtFlotari.setText(user.flotari + " Flotări");
                    txtGenoflexiuni.setText(user.genoflexiuni + " Genoflexiuni");

                    // Reîncărcăm leaderboard-ul când se schimbă datele
                    loadLeaderboard();
                }
            });
        }



        // LEADERBORD TYPE SHI
        containerLeaderboard = view.findViewById(R.id.container_leaderboard);

        // Invocă funcția de leaderboard
        loadLeaderboard();



        return view;
    }


    private void showStepDetails() {
        if (isShowingDetail) return; // Evităm suprapunerea timer-elor

        isShowingDetail = true;
        txtPasi.setText(pasiActuali + " / " + targetActual);

        // Așteptăm 2 secunde (2000 milisecunde)
        txtPasi.postDelayed(() -> {
            if (isAdded()) { // Verificăm dacă userul mai e pe pagină
                txtPasi.setText(String.valueOf(pasiActuali));
                isShowingDetail = false;
            }
        }, 2000);
    }

    // Metoda pentru calcul și afișare
    private void loadLeaderboard() {
        UserManager.getInstance().getTopUsers(users -> {
            if (!isAdded()) return;

            // 1. Calculăm punctele pentru fiecare user și sortăm
            users.sort((u1, u2) -> {
                float pts1 = calculatePoints(u1);
                float pts2 = calculatePoints(u2);
                return Float.compare(pts2, pts1); // Sortare descrescătoare
            });

            // 2. Curățăm containerul și afișăm primii 3
            containerLeaderboard.removeAllViews();
            int topCount = Math.min(users.size(), 3);

            for (int i = 0; i < topCount; i++) {
                User user = users.get(i);
                View row = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                TextView text1 = row.findViewById(android.R.id.text1);
                TextView text2 = row.findViewById(android.R.id.text2);

                String medal = (i == 0) ? "🥇 " : (i == 1) ? "🥈 " : "🥉 ";

                // Verificăm dacă numele este null și punem ceva în loc (pentru debug)
                String displayName = (user.name != null && !user.name.isEmpty()) ? user.name : "Anonim (Lipsă nume)";

                text1.setText(medal + displayName);
                text1.setTypeface(null, android.graphics.Typeface.BOLD);

                // Calculăm punctele
                int puncte = (int) calculatePoints(user);
                text2.setText("Nivel " + user.nivel_kangaroo + " • " + puncte + " puncte");

                containerLeaderboard.addView(row);
            }
        });
    }

    private float calculatePoints(User u) {
        int[] stats = {u.genoflexiuni, u.flotari, u.pasi};
        float currentProgress = KangarooLevel.getOverallProgress(u.nivel_kangaroo, stats);
        // Formula: Nivelul are ponderea cea mai mare, progresul curent face diferența
        return (u.nivel_kangaroo * 1000) + (currentProgress * 1000);
    }
}