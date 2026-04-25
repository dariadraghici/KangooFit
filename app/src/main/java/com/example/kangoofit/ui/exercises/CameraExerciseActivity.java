package com.example.kangoofit.ui.exercises;

import androidx.camera.core.AspectRatio;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import com.example.kangoofit.utils.OverlayView; // Importă OverlayView
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

public class CameraExerciseActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView tvCounter;
    private PoseLandmarker poseLandmarker;
    private ExecutorService cameraExecutor;
    private OverlayView overlayView;

    private String exerciseType = ""; // PUSHUPS sau SQUATS
    private int repCount = 0;
    private String movementStage = "up";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_exercise);

        // Preluăm tipul de exercițiu trimis din ExercisesFragment
        if (getIntent() != null && getIntent().hasExtra("EXERCISE_TYPE")) {
            exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");
        }

        previewView = findViewById(R.id.previewView);
        tvCounter = findViewById(R.id.tvCounter);
        overlayView = findViewById(R.id.overlayView);

        tvCounter.setText(exerciseType.equals("PUSHUPS") ? "Flotări: 0" : "Genuflexiuni: 0");

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
                        if (bitmap != null) {
                            MPImage mpImage = new BitmapImageBuilder(bitmap).build();
                            long timestampMs = imageProxy.getImageInfo().getTimestamp() / 1000000;
                            poseLandmarker.detectAsync(mpImage, timestampMs);
                        }
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

    private void onPoseDetected(PoseLandmarkerResult result, MPImage mpImage) {
        if (!result.landmarks().isEmpty()) {
            overlayView.setResults(result);
            List<NormalizedLandmark> landmarks = result.landmarks().get(0);

            if (exerciseType.equals("PUSHUPS")) {
                NormalizedLandmark shoulder = landmarks.get(12);
                NormalizedLandmark elbow = landmarks.get(14);
                NormalizedLandmark wrist = landmarks.get(16);

                double armAngle = calculateAngle(shoulder, elbow, wrist);

                if (armAngle > 160) {
                    if (movementStage.equals("down")) {
                        repCount++;
                        runOnUiThread(() -> tvCounter.setText("Flotări: " + repCount));
                    }
                    movementStage = "up";
                } else if (armAngle < 90) {
                    movementStage = "down";
                }
            } else if (exerciseType.equals("SQUATS")) {
                NormalizedLandmark hip = landmarks.get(24);
                NormalizedLandmark knee = landmarks.get(26);
                NormalizedLandmark ankle = landmarks.get(28);

                double legAngle = calculateAngle(hip, knee, ankle);

                if (legAngle > 160) {
                    if (movementStage.equals("down")) {
                        repCount++;
                        runOnUiThread(() -> tvCounter.setText("Genuflexiuni: " + repCount));
                    }
                    movementStage = "up";
                } else if (legAngle < 100) {
                    movementStage = "down";
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
        // Aici (pe viitor) vei putea salva "repCount" în baza de date/ViewModel
        // pentru a hrăni cangurul și a actualiza clasamentul!
    }
}