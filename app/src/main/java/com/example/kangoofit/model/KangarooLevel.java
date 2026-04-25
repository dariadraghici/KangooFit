package com.example.kangoofit.model;

import com.example.kangoofit.R;

public class KangarooLevel {
    public static final int TOTAL_LEVELS = 6;

    public static final String[] LEVEL_NAMES = {
            "Pui de Cangur", "Explorator Junior", "Cangur Sportiv",
            "Săritor de Elită", "Maestru Kanga", "Cangur Legendar"
    };

    // Index 0: Squats, 1: Pushups, 2: Steps
    // Fiecare rând reprezintă un nivel (Nivel 1, Nivel 2...)
    public static final int[][] REQUIREMENTS = {
            {15, 10, 1000},   // Nivel 1
            {30, 20, 2000},   // Nivel 2
            {45, 30, 4000},   // Nivel 3
            {60, 40, 6000},   // Nivel 4
            {80, 60, 8000},   // Nivel 5
            {100, 80, 10000}  // Nivel 6
    };

    public static int getRequirement(int level, int type) {
        if (level < 1 || level > TOTAL_LEVELS) return 99999;
        return REQUIREMENTS[level - 1][type];
    }

    public static boolean isLevelComplete(int level, int[] progress) {
        if (level < 1 || level > TOTAL_LEVELS) return false;

        int[] req = REQUIREMENTS[level - 1];

        // Verificăm fiecare condiție în parte
        boolean squatsDone = progress[0] >= req[0];
        boolean pushupsDone = progress[1] >= req[1];
        boolean stepsDone = progress[2] >= req[2];

        return squatsDone && pushupsDone && stepsDone;
    }

    public static int getDrawableResId(int level) {
        switch (level) {
            case 1: return R.drawable.kangur_1;
            case 2: return R.drawable.kangur_2;
            case 3: return R.drawable.kangur_3;
            case 4: return R.drawable.kangur_4;
            case 5: return R.drawable.kangur_5;
            case 6: return R.drawable.kangur_6;
            default: return R.drawable.kangur_1;
        }
    }

    public static float getOverallProgress(int level, int[] progress) {
        if (level > TOTAL_LEVELS) return 1.0f;
        if (level < 1) level = 1;

        float p1 = calculatePercent(progress[0], REQUIREMENTS[level - 1][0]);
        float p2 = calculatePercent(progress[1], REQUIREMENTS[level - 1][1]);
        float p3 = calculatePercent(progress[2], REQUIREMENTS[level - 1][2]);

        return (p1 + p2 + p3) / 3f;
    }

    private static float calculatePercent(int done, int req) {
        if (req <= 0) return 1.0f;
        return Math.min(1.0f, (float) done / req);
    }
}