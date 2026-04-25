package com.example.kangoofit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class StepTrackingService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "StepPrefs";
    private static final String LAST_SENSOR_STEPS = "last_sensor_steps";
    private static final String STEP_UPDATE_PATH = "/step_update";

    private static final String CHANNEL_ID = "StepTrackerChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Pornim notificarea permanentă pentru Foreground Service
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("KangooFit Activ")
                .setContentText("Contorizăm pașii tăi...")
                // Folosește iconița aplicației tale. Poți schimba cu R.drawable.ic_numele_tau
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build();

        // Specificăm tipul de serviciu pentru Android-urile noi (health)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        // 2. Inițializăm senzorul de pași
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor != null) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Tracker Service Channel",
                    NotificationManager.IMPORTANCE_LOW // LOW ca să nu sune/vibreze la fiecare actualizare
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentSensorSteps = (int) event.values[0];
            Log.d("WEAR_STEPS", "Senzorul a detectat un total de: " + currentSensorSteps + " pasi");
            int lastSensorSteps = prefs.getInt(LAST_SENSOR_STEPS, -1);

            if (lastSensorSteps == -1) {
                prefs.edit().putInt(LAST_SENSOR_STEPS, currentSensorSteps).apply();
                return;
            }

            int newSteps = currentSensorSteps - lastSensorSteps;

            if (newSteps > 0) {
                sendStepsToPhone(newSteps);
                prefs.edit().putInt(LAST_SENSOR_STEPS, currentSensorSteps).apply();
            } else if (newSteps < 0) {
                prefs.edit().putInt(LAST_SENSOR_STEPS, currentSensorSteps).apply();
            }
        }
    }

    private void sendStepsToPhone(int newSteps) {
        new Thread(() -> {
            try {
                List<Node> nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
                String stepsString = String.valueOf(newSteps);

                for (Node node : nodes) {
                    Wearable.getMessageClient(this).sendMessage(
                            node.getId(),
                            STEP_UPDATE_PATH,
                            stepsString.getBytes(StandardCharsets.UTF_8)
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}