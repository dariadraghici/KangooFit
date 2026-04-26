package com.example.kangoofit;

import android.os.Build;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends Activity implements MessageClient.OnMessageReceivedListener, SensorEventListener {

    private LinearLayout layoutStartButtons, layoutActiveWorkout;
    private TextView tvActiveExercise, tvActiveStatus, tvActiveReps, tvActiveBpm;

    private SensorManager sensorManager;
    private Sensor heartRateSensor;

    private static final String START_EXERCISE_PATH = "/start_exercise";
    private static final String STOP_EXERCISE_PATH = "/stop_exercise";
    private static final String REP_UPDATE_PATH = "/rep_update";
    private static final String STATUS_UPDATE_PATH = "/status_update";
    private static final String BPM_UPDATE_PATH = "/bpm_update";

    private static final int PERMISSION_REQUEST_SENSORS = 100;
    private static final int PERMISSION_REQUEST_HEART_RATE = 101;
    private static final String PERMISSION_READ_HEART_RATE = "android.permission.health.READ_HEART_RATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutStartButtons = findViewById(R.id.layout_start_buttons);
        layoutActiveWorkout = findViewById(R.id.layout_active_workout);

        tvActiveExercise = findViewById(R.id.tv_active_exercise);
        tvActiveStatus = findViewById(R.id.tv_active_status);
        tvActiveReps = findViewById(R.id.tv_active_reps);
        tvActiveBpm = findViewById(R.id.tv_active_bpm);

        Button btnPushups = findViewById(R.id.btn_start_pushups);
        Button btnSquats = findViewById(R.id.btn_start_squats);
        Button btnJacks = findViewById(R.id.btn_start_jacks);
        Button btnBiceps = findViewById(R.id.btn_start_biceps);
        Button btnShoulder = findViewById(R.id.btn_start_shoulder);
        Button btnStopExercise = findViewById(R.id.btn_stop_exercise);

        btnPushups.setOnClickListener(v -> { sendCommandToPhone(START_EXERCISE_PATH, "PUSHUPS"); showActiveWorkoutScreen("Push-ups"); });
        btnSquats.setOnClickListener(v -> { sendCommandToPhone(START_EXERCISE_PATH, "SQUATS"); showActiveWorkoutScreen("Squats"); });
        btnJacks.setOnClickListener(v -> { sendCommandToPhone(START_EXERCISE_PATH, "JUMPING_JACKS"); showActiveWorkoutScreen("Jumping Jacks"); });
        btnBiceps.setOnClickListener(v -> { sendCommandToPhone(START_EXERCISE_PATH, "BICEP_CURLS"); showActiveWorkoutScreen("Bicep Curls"); });
        btnShoulder.setOnClickListener(v -> { sendCommandToPhone(START_EXERCISE_PATH, "SHOULDER_PRESS"); showActiveWorkoutScreen("Shoulder Press"); });

        btnStopExercise.setOnClickListener(v -> {
            sendCommandToPhone(STOP_EXERCISE_PATH, "STOP");
            hideActiveWorkoutScreen();
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        }

        checkPermissionsAndStartService();
    }

    private void checkPermissionsAndStartService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            // Pentru activitate fizică lăsăm ActivityCompat pentru că e la pornirea aplicației
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST_SENSORS);
        } else {
            startStepService();
        }
    }

    private void showActiveWorkoutScreen(String displayName) {
        layoutStartButtons.setVisibility(View.GONE);
        layoutActiveWorkout.setVisibility(View.VISIBLE);

        tvActiveExercise.setText(displayName);
        tvActiveReps.setText("Reps: 0");
        tvActiveStatus.setText("Status: Connecting...");

        // Apelăm metoda nouă dedicată pulsului
        checkAndRequestHeartRatePermission();
    }

    // --- METODA NOUĂ ---
    private void checkAndRequestHeartRatePermission() {
        if (Build.VERSION.SDK_INT >= 36) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_READ_HEART_RATE) != PackageManager.PERMISSION_GRANTED) {
                tvActiveBpm.setText("BPM: Need Permission");
                requestPermissions(new String[]{PERMISSION_READ_HEART_RATE}, PERMISSION_REQUEST_HEART_RATE);
            } else {
                startHeartRateSensor();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
                tvActiveBpm.setText("BPM: Need Permission");
                requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, PERMISSION_REQUEST_HEART_RATE);
            } else {
                startHeartRateSensor();
            }
        }
    }

    private void hideActiveWorkoutScreen() {
        layoutStartButtons.setVisibility(View.VISIBLE);
        layoutActiveWorkout.setVisibility(View.GONE);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void sendCommandToPhone(String path, String payload) {
        new Thread(() -> {
            try {
                List<Node> nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
                for (Node node : nodes) {
                    Wearable.getMessageClient(this).sendMessage(node.getId(), path, payload.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            int bpm = (int) event.values[0];
            runOnUiThread(() -> tvActiveBpm.setText("BPM: " + bpm));
            sendCommandToPhone(BPM_UPDATE_PATH, String.valueOf(bpm));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData(), StandardCharsets.UTF_8);

        runOnUiThread(() -> {
            if (path.equals(REP_UPDATE_PATH)) {
                tvActiveReps.setText("Reps: " + data);
            } else if (path.equals(STATUS_UPDATE_PATH)) {
                tvActiveStatus.setText("Status: " + data);
                if (data.equals("Counting...")) {
                    tvActiveStatus.setTextColor(android.graphics.Color.GREEN);
                } else {
                    tvActiveStatus.setTextColor(android.graphics.Color.YELLOW);
                }
            }
            else if (path.equals("/start_exercise_from_phone")) {
                String displayName = "Workout";
                switch (data) {
                    case "PUSHUPS": displayName = "Push-ups"; break;
                    case "SQUATS": displayName = "Squats"; break;
                    case "JUMPING_JACKS": displayName = "Jumping Jacks"; break;
                    case "BICEP_CURLS": displayName = "Bicep Curls"; break;
                    case "SHOULDER_PRESS": displayName = "Shoulder Press"; break;
                }
                showActiveWorkoutScreen(displayName);
            }
            else if (path.equals("/stop_exercise_from_phone")) {
                hideActiveWorkoutScreen();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_SENSORS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepService();
            }
        }
        else if (requestCode == PERMISSION_REQUEST_HEART_RATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startHeartRateSensor();
            } else {
                tvActiveBpm.setText("BPM: Denied");
            }
        }
    }

    private void startStepService() {
        Intent serviceIntent = new Intent(this, StepTrackingService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void startHeartRateSensor() {
        if (heartRateSensor != null) {
            tvActiveBpm.setText("BPM: Calibrating...");
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            tvActiveBpm.setText("BPM: Not Found");
        }
    }
}