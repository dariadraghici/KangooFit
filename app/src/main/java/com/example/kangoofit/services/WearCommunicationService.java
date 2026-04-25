package com.example.kangoofit.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.example.kangoofit.ui.exercises.CameraExerciseActivity;
import com.example.kangoofit.database.UserManager; // Importă UserManager-ul tău
import java.nio.charset.StandardCharsets;

public class WearCommunicationService extends WearableListenerService {

    private static final String START_EXERCISE_PATH = "/start_exercise";
    private static final String STEP_UPDATE_PATH = "/step_update"; // Calea pentru pași

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData(), StandardCharsets.UTF_8);

//        Log.d("PHONE_STEPS", "Am primit de la ceas calea: " + path + " cu datele: " + data);

        if (path.equals(START_EXERCISE_PATH)) {
            // 1. Deschidem CameraExerciseActivity
            Intent intent = new Intent(this, CameraExerciseActivity.class);
            intent.putExtra("EXERCISE_TYPE", data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else if (path.equals("/stop_exercise")) {
            Intent stopIntent = new Intent("STOP_EXERCISE_ACTION");
            sendBroadcast(stopIntent);
        } else if (path.equals(STEP_UPDATE_PATH)) {
            // 2. Primim pașii de la ceas și îi salvăm în Firebase
            try {
                int stepsReceived = Integer.parseInt(data);

                // Folosim incrementStat ca să adunăm pașii noi la cei existenți
                // Dacă ceasul trimite totalul zilei, folosește updateField("pasi", stepsReceived)
                UserManager.getInstance().incrementStat("pasi", stepsReceived);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}