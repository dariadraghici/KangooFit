package com.example.kangoofit;

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

        btnPushups.setOnClickListener(v -> startWorkout("PUSHUPS", "Push-ups"));
        btnSquats.setOnClickListener(v -> startWorkout("SQUATS", "Squats"));
        btnJacks.setOnClickListener(v -> startWorkout("JUMPING_JACKS", "Jumping Jacks"));
        btnBiceps.setOnClickListener(v -> startWorkout("BICEP_CURLS", "Bicep Curls"));
        btnShoulder.setOnClickListener(v -> startWorkout("SHOULDER_PRESS", "Shoulder Press"));

        btnStopExercise.setOnClickListener(v -> {
            sendCommandToPhone(STOP_EXERCISE_PATH, "STOP");
            stopWorkout();
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        }

        checkPermissionsAndStartService();
    }

    private void checkPermissionsAndStartService() {
        String[] permissions = {Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.BODY_SENSORS};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_SENSORS);
        } else {
            startStepService();
        }
    }

    private void startStepService() {
        Intent serviceIntent = new Intent(this, StepTrackingService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void startWorkout(String typeCode, String displayName) {
        sendCommandToPhone(START_EXERCISE_PATH, typeCode);

        layoutStartButtons.setVisibility(View.GONE);
        layoutActiveWorkout.setVisibility(View.VISIBLE);

        tvActiveExercise.setText(displayName);
        tvActiveReps.setText("Reps: 0");
        tvActiveStatus.setText("Status: Connecting...");
        tvActiveBpm.setText("BPM: --");

        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void stopWorkout() {
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
        });
    }
}