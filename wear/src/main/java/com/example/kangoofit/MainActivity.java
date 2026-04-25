package com.example.kangoofit;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends android.app.Activity implements MessageClient.OnMessageReceivedListener {

    private TextView tvRepsWatch;
    private LinearLayout layoutStartButtons;
    private Button btnStopExercise;
    private static final String START_EXERCISE_PATH = "/start_exercise";
    private static final String REP_UPDATE_PATH = "/rep_update";
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 100;
    private static final String STOP_EXERCISE_PATH = "/stop_exercise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRepsWatch = findViewById(R.id.tv_reps_watch);
        layoutStartButtons = findViewById(R.id.layout_start_buttons);
        btnStopExercise = findViewById(R.id.btn_stop_exercise);

        Button btnPushups = findViewById(R.id.btn_start_pushups);
        Button btnSquats = findViewById(R.id.btn_start_squats);

        btnPushups.setOnClickListener(v -> sendCommandToPhone(START_EXERCISE_PATH, "PUSHUPS"));
        btnSquats.setOnClickListener(v -> sendCommandToPhone(START_EXERCISE_PATH, "SQUATS"));

        // Logica pentru butonul de STOP
        btnStopExercise.setOnClickListener(v -> {
            sendCommandToPhone(STOP_EXERCISE_PATH, "STOP");
            // Resetăm UI-ul
            layoutStartButtons.setVisibility(View.VISIBLE);
            btnStopExercise.setVisibility(View.GONE);
            tvRepsWatch.setText("Alege Exercițiul");
        });

        checkPermissionsAndStartService();
    }

    private void checkPermissionsAndStartService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        } else {
            startStepService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepService();
            } else {
                tvRepsWatch.setText("Permisiune refuzată pt pași");
            }
        }
    }

    private void startStepService() {
        Intent serviceIntent = new Intent(this, StepTrackingService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void sendCommandToPhone(String path, String payload) {
        if (path.equals(START_EXERCISE_PATH)) {
            tvRepsWatch.setText("Se pornește camera...");
            layoutStartButtons.setVisibility(View.GONE);
            btnStopExercise.setVisibility(View.VISIBLE);
        }

        new Thread(() -> {
            try {
                List<Node> nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
                for (Node node : nodes) {
                    Wearable.getMessageClient(this).sendMessage(
                            node.getId(), path, payload.getBytes(StandardCharsets.UTF_8)
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    // Aici primim repetările de la telefon
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(REP_UPDATE_PATH)) {
            String reps = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            runOnUiThread(() -> tvRepsWatch.setText("Repetări: " + reps));
        }
    }
}