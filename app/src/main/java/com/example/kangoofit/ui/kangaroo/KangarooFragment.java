package com.example.kangoofit.ui.kangaroo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.kangoofit.R;
import com.example.kangoofit.model.KangarooLevel;
import com.example.kangoofit.viewmodel.KangarooViewModel;

public class KangarooFragment extends Fragment {

    private KangarooViewModel viewModel;

    // Views
    private ImageView imgKangaroo;
    private TextView tvLevelName;
    private TextView tvLevelNumber;
    private ProgressBar progressBarSquats;
    private ProgressBar progressBarPushups;
    private ProgressBar progressBarPullups;
    private ProgressBar progressBarSteps;
    private TextView tvSquatsProgress;
    private TextView tvPushupsProgress;
    private TextView tvPullupsProgress;
    private TextView tvStepsProgress;
    private TextView tvNextLevel;
    private ProgressBar progressBarOverall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kangaroo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inițializare ViewModel
        viewModel = new ViewModelProvider(this).get(KangarooViewModel.class);

        // Bind views
        imgKangaroo       = view.findViewById(R.id.img_kangaroo);
        tvLevelName       = view.findViewById(R.id.tv_level_name);
        tvLevelNumber     = view.findViewById(R.id.tv_level_number);
        progressBarSquats = view.findViewById(R.id.progress_squats);
        progressBarPushups= view.findViewById(R.id.progress_pushups);
        progressBarPullups= view.findViewById(R.id.progress_pullups);
        progressBarSteps  = view.findViewById(R.id.progress_steps);
        tvSquatsProgress  = view.findViewById(R.id.tv_squats_progress);
        tvPushupsProgress = view.findViewById(R.id.tv_pushups_progress);
        tvPullupsProgress = view.findViewById(R.id.tv_pullups_progress);
        tvStepsProgress   = view.findViewById(R.id.tv_steps_progress);
        tvNextLevel       = view.findViewById(R.id.tv_next_level);
        progressBarOverall= view.findViewById(R.id.progress_overall);

        // Observă nivelul curent
        viewModel.getCurrentLevel().observe(getViewLifecycleOwner(), level -> {
            // Actualizează imaginea cangurului
            imgKangaroo.setImageResource(KangarooLevel.getDrawableResId(level));

            // Actualizează textul de nivel
            String levelName = KangarooLevel.LEVEL_NAMES[level - 1];
            tvLevelName.setText(levelName);
            tvLevelNumber.setText(getString(R.string.level_label, level));

            // Dacă e adult complet, ascunde bara de progres
            if (level >= KangarooLevel.TOTAL_LEVELS) {
                tvNextLevel.setText(R.string.level_max_reached);
                progressBarOverall.setProgress(100);
            } else {
                tvNextLevel.setText(getString(R.string.next_level_label,
                        KangarooLevel.LEVEL_NAMES[level]));
            }

            // Actualizează barele individuale
            updateProgressBars(level,
                    viewModel.getExerciseProgress().getValue());
        });

        // Observă progresul exercițiilor
        viewModel.getExerciseProgress().observe(getViewLifecycleOwner(), progress -> {
            Integer level = viewModel.getCurrentLevel().getValue();
            if (level == null) level = 1;
            updateProgressBars(level, progress);
        });
    }

    private void updateProgressBars(int level, int[] progress) {
        if (progress == null) progress = new int[]{0, 0, 0, 0};

        if (level >= KangarooLevel.TOTAL_LEVELS) {
            // Toate barele la 100%
            progressBarSquats.setProgress(100);
            progressBarPushups.setProgress(100);
            progressBarPullups.setProgress(100);
            progressBarSteps.setProgress(100);
            tvSquatsProgress.setText("✓");
            tvPushupsProgress.setText("✓");
            tvPullupsProgress.setText("✓");
            tvStepsProgress.setText("✓");
            progressBarOverall.setProgress(100);
            return;
        }

        int reqSq = KangarooLevel.getRequirement(level, 0);
        int reqPu = KangarooLevel.getRequirement(level, 1);
        int reqPl = KangarooLevel.getRequirement(level, 2);
        int reqSt = KangarooLevel.getRequirement(level, 3);

        int doneSq = progress[0];
        int donePu = progress[1];
        int donePl = progress[2];
        int doneSt = progress[3];

        // Setează progresul barelor (0–100)
        progressBarSquats.setProgress(reqSq > 0 ? Math.min(100, doneSq * 100 / reqSq) : 100);
        progressBarPushups.setProgress(reqPu > 0 ? Math.min(100, donePu * 100 / reqPu) : 100);
        progressBarPullups.setProgress(reqPl > 0 ? Math.min(100, donePl * 100 / reqPl) : 100);
        progressBarSteps.setProgress(reqSt > 0 ? Math.min(100, doneSt * 100 / reqSt) : 100);

        // Textele „X / Y"
        tvSquatsProgress.setText(reqSq > 0 ? doneSq + " / " + reqSq : "—");
        tvPushupsProgress.setText(reqPu > 0 ? donePu + " / " + reqPu : "—");
        tvPullupsProgress.setText(reqPl > 0 ? donePl + " / " + reqPl : "—");
        tvStepsProgress.setText(reqSt > 0 ? doneSt + " / " + reqSt : "—");

        // Bara generală de progres
        float overall = KangarooLevel.getOverallProgress(level, progress);
        progressBarOverall.setProgress((int) (overall * 100));
    }
}