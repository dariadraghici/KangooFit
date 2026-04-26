package com.example.kangoofit.ui.exercises;

import androidx.camera.core.AspectRatio;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.content.ContextCompat;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import com.example.kangoofit.utils.OverlayView;
import com.example.kangoofit.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.graphics.Matrix;

public class CameraExerciseActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView tvExerciseName, tvStatus, tvReps, tvBpm;
    private PoseLandmarker poseLandmarker;
    private ExecutorService cameraExecutor;
    private OverlayView overlayView;

    private String exerciseType = "";
    private int repCount = 0;
    private String movementStage = "up";
    private String currentStatus = "";

    private BroadcastReceiver wearDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("STOP_EXERCISE_ACTION".equals(intent.getAction())) {
                finishWorkoutAndSave();
            } else if ("BPM_UPDATE_ACTION".equals(intent.getAction())) {
                String bpm = intent.getStringExtra("BPM_VALUE");
                tvBpm.setText(bpm + " BPM");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_exercise);

        if (getIntent() != null && getIntent().hasExtra("EXERCISE_TYPE")) {
            exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");
        }

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvStatus = findViewById(R.id.tvStatus);
        tvReps = findViewById(R.id.tvReps);
        tvBpm = findViewById(R.id.tvBpm);

        String displayName = "";
        switch (exerciseType) {
            case "PUSHUPS": displayName = "Push-ups"; break;
            case "SQUATS": displayName = "Squats"; break;
            case "JUMPING_JACKS": displayName = "Jumping Jacks"; break;
            case "BICEP_CURLS": displayName = "Bicep Curls"; break;
            case "SHOULDER_PRESS": displayName = "Shoulder Press"; break;
        }
        tvExerciseName.setText(displayName);
        updateStatus("Stand in frame!");

        // NOU: Trimitem semnal ceasului că am pornit de pe telefon
        sendDataToWatch("/start_exercise_from_phone", exerciseType);

        // NOU: Logica butonului de STOP de pe telefon
        Button btnStopPhone = findViewById(R.id.btnStopPhone);
        btnStopPhone.setOnClickListener(v -> {
            finishWorkoutAndSave();
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1001);
        } else {
            setupMediaPipe();
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupMediaPipe();
            startCamera();
        }
    }

    private void setupMediaPipe() {
        BaseOptions baseOptions = BaseOptions.builder().setModelAssetPath("pose_landmarker_lite.task").build();
        PoseLandmarker.PoseLandmarkerOptions options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::onPoseDetected)
                .build();
        poseLandmarker = PoseLandmarker.createFromOptions(this, options);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("STOP_EXERCISE_ACTION");
        filter.addAction("BPM_UPDATE_ACTION");
        ContextCompat.registerReceiver(this, wearDataReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wearDataReceiver);
    }

    private void updateStatus(String newStatus) {
        if (!currentStatus.equals(newStatus)) {
            currentStatus = newStatus;
            runOnUiThread(() -> {
                tvStatus.setText("Status: " + newStatus);
                if (newStatus.equals("Counting...")) {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#00A859"));
                } else {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FFEB3B"));
                }
            });
            sendStatusToWatch(newStatus);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    try {
                        Bitmap bitmap = imageProxy.toBitmap();
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

                        Matrix matrix = new Matrix();
                        if (bitmap.getWidth() > bitmap.getHeight() && rotationDegrees != 0) {
                            matrix.postRotate(rotationDegrees);
                        }
                        Bitmap uprightBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                        Matrix flipMatrix = new Matrix();
                        flipMatrix.postScale(-1f, 1f, uprightBitmap.getWidth() / 2f, uprightBitmap.getHeight() / 2f);
                        Bitmap finalBitmap = Bitmap.createBitmap(uprightBitmap, 0, 0, uprightBitmap.getWidth(), uprightBitmap.getHeight(), flipMatrix, true);

                        overlayView.setImageDimensions(finalBitmap.getWidth(), finalBitmap.getHeight());

                        MPImage mpImage = new BitmapImageBuilder(finalBitmap).build();
                        long timestampMs = imageProxy.getImageInfo().getTimestamp() / 1000000;
                        poseLandmarker.detectAsync(mpImage, timestampMs);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private double calculateAngle(NormalizedLandmark a, NormalizedLandmark b, NormalizedLandmark c) {
        double radians = Math.atan2(c.y() - b.y(), c.x() - b.x()) - Math.atan2(a.y() - b.y(), a.x() - b.x());
        double angle = Math.abs(radians * 180.0 / Math.PI);
        if (angle > 180.0) {
            angle = 360.0 - angle;
        }
        return angle;
    }

    // Am relaxat validarea: ne interesează doar ca AI-ul să fie sigur că vede osul (> 40% încredere)
    private boolean isLandmarkValid(NormalizedLandmark lm) {
        if (lm.visibility().isPresent() && lm.visibility().get() < 0.4f) {
            return false;
        }
        return true;
    }

    private void incrementRep() {
        repCount++;
        runOnUiThread(() -> tvReps.setText("Reps: " + repCount));
        sendDataToWatch("/rep_update", String.valueOf(repCount));
    }

    private void onPoseDetected(PoseLandmarkerResult result, MPImage mpImage) {
        if (result.landmarks().isEmpty()) {
            updateStatus("Stand in frame!");
            return;
        }

        overlayView.setResults(result);
        List<NormalizedLandmark> landmarks = result.landmarks().get(0);

        // Identificăm care parte a corpului este vizibilă clar
        boolean leftArmValid = isLandmarkValid(landmarks.get(11)) && isLandmarkValid(landmarks.get(13)) && isLandmarkValid(landmarks.get(15));
        boolean rightArmValid = isLandmarkValid(landmarks.get(12)) && isLandmarkValid(landmarks.get(14)) && isLandmarkValid(landmarks.get(16));

        boolean leftLegValid = isLandmarkValid(landmarks.get(23)) && isLandmarkValid(landmarks.get(25)) && isLandmarkValid(landmarks.get(27));
        boolean rightLegValid = isLandmarkValid(landmarks.get(24)) && isLandmarkValid(landmarks.get(26)) && isLandmarkValid(landmarks.get(28));

        boolean isReady = false;
        double currentAngle = 0;

        switch (exerciseType) {
            case "PUSHUPS":
            case "BICEP_CURLS":
            case "SHOULDER_PRESS":
                if (leftArmValid) {
                    isReady = true;
                    currentAngle = calculateAngle(landmarks.get(11), landmarks.get(13), landmarks.get(15));
                } else if (rightArmValid) {
                    isReady = true;
                    currentAngle = calculateAngle(landmarks.get(12), landmarks.get(14), landmarks.get(16));
                }
                break;
            case "SQUATS":
                if (leftLegValid) {
                    isReady = true;
                    currentAngle = calculateAngle(landmarks.get(23), landmarks.get(25), landmarks.get(27));
                } else if (rightLegValid) {
                    isReady = true;
                    currentAngle = calculateAngle(landmarks.get(24), landmarks.get(26), landmarks.get(28));
                }
                break;
            case "JUMPING_JACKS":
                // La jacks avem nevoie de o imagine de ansamblu (măcar mâinile și gleznele să fie clare)
                isReady = leftArmValid && rightArmValid && isLandmarkValid(landmarks.get(27)) && isLandmarkValid(landmarks.get(28));
                break;
        }

        if (!isReady) {
            updateStatus("Stand in frame!");
            return; // Oprește procesarea rep-ului
        } else {
            updateStatus("Counting...");
        }

        // Dacă ești "Ready", calculează repetările dinamic pe partea care a fost detectată!
        if (exerciseType.equals("PUSHUPS")) {
            if (currentAngle > 160) {
                if (movementStage.equals("down")) incrementRep();
                movementStage = "up";
            } else if (currentAngle < 90) {
                movementStage = "down";
            }
        }
        else if (exerciseType.equals("SQUATS")) {
            if (currentAngle > 160) {
                if (movementStage.equals("down")) incrementRep();
                movementStage = "up";
            } else if (currentAngle < 100) {
                movementStage = "down";
            }
        }
        else if (exerciseType.equals("BICEP_CURLS")) {
            if (currentAngle < 45) {
                if (movementStage.equals("down")) incrementRep();
                movementStage = "up";
            } else if (currentAngle > 140) {
                movementStage = "down";
            }
        }
        else if (exerciseType.equals("SHOULDER_PRESS")) {
            // Căutăm exact încheietura/umărul părții vizibile
            NormalizedLandmark wrist = leftArmValid ? landmarks.get(15) : landmarks.get(16);
            NormalizedLandmark shoulder = leftArmValid ? landmarks.get(11) : landmarks.get(12);

            if (currentAngle > 150 && wrist.y() < shoulder.y()) {
                if (movementStage.equals("down")) incrementRep();
                movementStage = "up";
            } else if (currentAngle < 100 && wrist.y() >= shoulder.y() - 0.1) {
                movementStage = "down";
            }
        }
        else if (exerciseType.equals("JUMPING_JACKS")) {
            NormalizedLandmark leftWrist = landmarks.get(15);
            NormalizedLandmark rightWrist = landmarks.get(16);
            NormalizedLandmark leftShoulder = landmarks.get(11);
            NormalizedLandmark rightShoulder = landmarks.get(12);
            NormalizedLandmark leftAnkle = landmarks.get(27);
            NormalizedLandmark rightAnkle = landmarks.get(28);

            boolean armsUp = leftWrist.y() < leftShoulder.y() && rightWrist.y() < rightShoulder.y();
            boolean legsApart = Math.abs(leftAnkle.x() - rightAnkle.x()) > Math.abs(leftShoulder.x() - rightShoulder.x()) * 1.5;

            if (armsUp && legsApart) {
                if (movementStage.equals("down")) incrementRep();
                movementStage = "up";
            } else if (!armsUp && !legsApart) {
                movementStage = "down";
            }
        }
    }

    private void sendDataToWatch(String path, String payload) {
        new Thread(() -> {
            try {
                List<com.google.android.gms.wearable.Node> nodes =
                        com.google.android.gms.tasks.Tasks.await(
                                com.google.android.gms.wearable.Wearable.getNodeClient(this).getConnectedNodes()
                        );
                for (com.google.android.gms.wearable.Node node : nodes) {
                    com.google.android.gms.wearable.Wearable.getMessageClient(this).sendMessage(
                            node.getId(), path, payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                    );
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void sendStatusToWatch(String status) {
        sendDataToWatch("/status_update", status);
    }

    private void finishWorkoutAndSave() {
        if (repCount > 0) {
            String firebaseField = "";
            switch (exerciseType) {
                case "PUSHUPS": firebaseField = "flotari"; break;
                case "SQUATS": firebaseField = "genoflexiuni"; break;
                case "JUMPING_JACKS": firebaseField = "jumping_jacks"; break;
                case "BICEP_CURLS": firebaseField = "biceps"; break;
                case "SHOULDER_PRESS": firebaseField = "umeri"; break;
            }

            if (!firebaseField.isEmpty()) {
                com.example.kangoofit.database.UserManager.getInstance().incrementStat(firebaseField, repCount);
            }
        }

        sendDataToWatch("/stop_exercise_from_phone", "STOP");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
    }
}