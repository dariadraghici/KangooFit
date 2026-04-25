package com.example.kangoofit.model;

public class KangarooLevel {

    public static final int TOTAL_LEVELS = 6;

    // Level names in English (match kangur_1.png ... kangur_6.png)
    public static final String[] LEVEL_NAMES = {
            "Newborn Joey",   // level 1 — kangur_1
            "Small Joey",     // level 2 — kangur_2
            "Juvenile",       // level 3 — kangur_3
            "Young Adult",    // level 4 — kangur_4
            "Sub-adult",      // level 5 — kangur_5
            "Full Adult"      // level 6 — kangur_6
    };

    // Requirements to advance to the next level
    // [level][0]=squats [1]=push-ups [2]=pull-ups [3]=steps
    public static final int[][] REQUIREMENTS = {
            {50,   30,  0,   1000},   // level 1 → 2
            {100,  60,  10,  5000},   // level 2 → 3
            {200,  100, 30,  10000},  // level 3 → 4
            {400,  200, 60,  20000},  // level 4 → 5
            {800,  400, 100, 50000},  // level 5 → 6
            {0,    0,   0,   0}       // level 6 — full adult, no requirement
    };

    public static int getDrawableResId(int level) {
        switch (level) {
            case 1: return com.example.kangoofit.R.drawable.kangur_1;
            case 2: return com.example.kangoofit.R.drawable.kangur_2;
            case 3: return com.example.kangoofit.R.drawable.kangur_3;
            case 4: return com.example.kangoofit.R.drawable.kangur_4;
            case 5: return com.example.kangoofit.R.drawable.kangur_5;
            case 6: return com.example.kangoofit.R.drawable.kangur_6;
            default: return com.example.kangoofit.R.drawable.kangur_1;
        }
    }

    public static int getRequirement(int currentLevel, int exerciseType) {
        if (currentLevel < 1 || currentLevel > TOTAL_LEVELS) return 0;
        return REQUIREMENTS[currentLevel - 1][exerciseType];
    }

    public static float getOverallProgress(int currentLevel, int[] progress) {
        if (currentLevel >= TOTAL_LEVELS) return 1f;
        int[] req = REQUIREMENTS[currentLevel - 1];
        float total = 0f;
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (req[i] > 0) {
                total += Math.min(1f, (float) progress[i] / req[i]);
                count++;
            }
        }
        return count > 0 ? total / count : 0f;
    }
}