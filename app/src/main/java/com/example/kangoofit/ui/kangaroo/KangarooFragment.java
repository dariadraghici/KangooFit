package com.example.kangoofit.ui.kangaroo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kangoofit.R;
import com.example.kangoofit.model.KangarooLevel;
import com.example.kangoofit.database.UserManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

public class KangarooFragment extends Fragment {

    private ImageView imgKangaroo;
    private TextView tvLevelName, tvLevelNumber, tvSquatsProgress, tvPushupsProgress, tvStepsProgress, tvNextLevel;
    private ProgressBar progressBarSquats, progressBarPushups, progressBarSteps;
    private LinearProgressIndicator progressBarOverall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kangaroo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Bind Views
        imgKangaroo = view.findViewById(R.id.img_kangaroo);
        tvLevelName = view.findViewById(R.id.tv_level_name);
        tvLevelNumber = view.findViewById(R.id.tv_level_number);
        tvSquatsProgress = view.findViewById(R.id.tv_squats_progress);
        tvPushupsProgress = view.findViewById(R.id.tv_pushups_progress);
        tvStepsProgress = view.findViewById(R.id.tv_steps_progress);
        tvNextLevel = view.findViewById(R.id.tv_next_level);

        progressBarSquats = view.findViewById(R.id.progress_squats);
        progressBarPushups = view.findViewById(R.id.progress_pushups);
        progressBarSteps = view.findViewById(R.id.progress_steps);
        // Atenție: În XML ai LinearProgressIndicator pentru overall
        progressBarOverall = view.findViewById(R.id.progress_overall);

        // 2. Firebase Real-time Listener
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {

                    int currentLevel = user.nivel_kangaroo > 0 ? user.nivel_kangaroo : 1;

                    int[] stats = new int[]{
                            user.genoflexiuni,
                            user.flotari,
                            user.pasi
                    };

                    // --- LOGICA DE LEVEL UP ---
                    if (KangarooLevel.isLevelComplete(currentLevel, stats) && currentLevel < KangarooLevel.TOTAL_LEVELS) {

                        // Creăm un Map pentru a actualiza toate câmpurile într-o singură scriere
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("nivel_kangaroo", currentLevel + 1);
                        updates.put("genoflexiuni", 0);
                        updates.put("flotari", 0);
                        updates.put("pasi", 0);

                        // Trimitem tot Map-ul către Firestore
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    if (isAdded()) {
                                        Toast.makeText(getContext(), "FELICITĂRI! Obiective îndeplinite. Nivel nou!", Toast.LENGTH_LONG).show();
                                    }
                                });

                        return; // Oprim execuția pentru a lăsa Firebase să trimită noile date (0, 0, 0)
                    }

                    // --- ACTUALIZARE UI ---
                    float totalProgress = KangarooLevel.getOverallProgress(currentLevel, stats);
                    updateUI(currentLevel, stats, totalProgress);
                }
            });
        }
    }

    private void updateUI(int level, int[] stats, float totalProgress) {
        if (!isAdded()) return;

        // Imaginea corectă conform nivelului
        imgKangaroo.setImageResource(KangarooLevel.getDrawableResId(level));

        // Texte
        tvLevelNumber.setText(getString(R.string.level_prefix_simple) + level);
        tvLevelName.setText(KangarooLevel.LEVEL_NAMES[level - 1]);

        if (level >= KangarooLevel.TOTAL_LEVELS) {
            tvNextLevel.setText(getString(R.string.max_level_reached));
        } else {
            // Folosim formatarea pentru a pune numărul nivelului următor
            tvNextLevel.setText(getString(R.string.progress_to_level, (level + 1)));
        }

        // Barele individuale
        updateSingleBar(progressBarSquats, tvSquatsProgress, stats[0], KangarooLevel.getRequirement(level, 0));
        updateSingleBar(progressBarPushups, tvPushupsProgress, stats[1], KangarooLevel.getRequirement(level, 1));
        updateSingleBar(progressBarSteps, tvStepsProgress, stats[2], KangarooLevel.getRequirement(level, 2));
        // Bara de sus (Overall)
        progressBarOverall.setProgress((int) (totalProgress * 100));
    }

    private void updateSingleBar(ProgressBar bar, TextView text, int done, int req) {
        if (bar == null || text == null) return;

        int percent = (req > 0) ? (done * 100 / req) : 100;
        bar.setProgress(Math.min(100, percent));
        text.setText(done + " / " + req);
    }
}