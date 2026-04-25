package com.example.kangoofit.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.kangoofit.model.KangarooLevel;

public class KangarooViewModel extends ViewModel {

    // Nivelul curent al cangurului (1 = pui nou, 6 = adult)
    private final MutableLiveData<Integer> currentLevel = new MutableLiveData<>(1);

    // Progresul curent: [genuflexiuni, flotări, tracțiuni, pași]
    private final MutableLiveData<int[]> exerciseProgress = new MutableLiveData<>(new int[]{0, 0, 0, 0});

    public LiveData<Integer> getCurrentLevel() {
        return currentLevel;
    }

    public LiveData<int[]> getExerciseProgress() {
        return exerciseProgress;
    }

    // Adaugă exerciții și verifică dacă se avansează la nivel
    // type: 0=genuflexiuni, 1=flotări, 2=tracțiuni, 3=pași
    public void addExercise(int type, int amount) {
        int[] progress = exerciseProgress.getValue();
        if (progress == null) progress = new int[]{0, 0, 0, 0};
        int[] newProgress = progress.clone();
        newProgress[type] += amount;
        exerciseProgress.setValue(newProgress);
        checkLevelUp(newProgress);
    }

    private void checkLevelUp(int[] progress) {
        int level = currentLevel.getValue() != null ? currentLevel.getValue() : 1;
        if (level >= KangarooLevel.TOTAL_LEVELS) return;

        int[] req = KangarooLevel.REQUIREMENTS[level - 1];
        boolean allMet = true;
        for (int i = 0; i < 4; i++) {
            if (req[i] > 0 && progress[i] < req[i]) {
                allMet = false;
                break;
            }
        }
        if (allMet) {
            currentLevel.setValue(level + 1);
            // Resetăm progresul după avansare
            exerciseProgress.setValue(new int[]{0, 0, 0, 0});
        }
    }

    // Returnează progresul procentual global (0.0 - 1.0)
    public float getOverallProgress() {
        int level = currentLevel.getValue() != null ? currentLevel.getValue() : 1;
        int[] progress = exerciseProgress.getValue();
        if (progress == null) return 0f;
        return KangarooLevel.getOverallProgress(level, progress);
    }
}