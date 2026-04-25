package com.example.kangoofit.services;

import android.content.Intent;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.example.kangoofit.ui.exercises.CameraExerciseActivity;
import java.nio.charset.StandardCharsets;

public class WearCommunicationService extends WearableListenerService {

    private static final String START_EXERCISE_PATH = "/start_exercise";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if (messageEvent.getPath().equals(START_EXERCISE_PATH)) {
            String exerciseType = new String(messageEvent.getData(), StandardCharsets.UTF_8);

            // Deschidem CameraExerciseActivity
            Intent intent = new Intent(this, CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", exerciseType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Obligatoriu când pornești dintr-un Service
            startActivity(intent);
        }
    }
}