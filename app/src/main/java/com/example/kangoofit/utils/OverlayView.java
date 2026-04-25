package com.example.kangoofit.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import java.util.List;

public class OverlayView extends View {
    private PoseLandmarkerResult poseResult;
    private Paint pointPaint;
    private Paint linePaint;

    // Conexiunile standard pentru corpul uman (perechi de puncte pe care le unim cu linii)
    private static final int[][] POSE_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 7}, {0, 4}, {4, 5}, {5, 6}, {6, 8}, {9, 10},
            {11, 12}, {11, 13}, {13, 15}, {15, 17}, {15, 19}, {15, 21}, {17, 19},
            {12, 14}, {14, 16}, {16, 18}, {16, 20}, {16, 22}, {18, 20}, {11, 23},
            {12, 24}, {23, 24}, {23, 25}, {25, 27}, {27, 29}, {27, 31}, {29, 31},
            {24, 26}, {26, 28}, {28, 30}, {28, 32}, {30, 32}
    };

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Cum arată punctele (roșii)
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);

        // Cum arată liniile (verzi, mai groase)
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    // Funcția care primește rezultatele de la MainActivity
    public void setResults(PoseLandmarkerResult result) {
        this.poseResult = result;
        postInvalidate(); // Spune Android-ului să refacă desenul pe ecran
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (poseResult == null || poseResult.landmarks().isEmpty()) return;

        List<NormalizedLandmark> landmarks = poseResult.landmarks().get(0);
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Raportul camerei setat de noi mai devreme (4:3 în mod portrait înseamnă 3:4)
        float imageAR = 3f / 4f;
        float viewAR = (float) viewWidth / viewHeight;

        float scaledWidth;
        float scaledHeight;
        float offsetX = 0;
        float offsetY = 0;

        // Calculăm exact cum a făcut PreviewView zoom ca să umple ecranul
        if (viewAR < imageAR) {
            // Ecranul e mai îngust -> Android a tăiat din stânga și dreapta
            scaledHeight = viewHeight;
            scaledWidth = viewHeight * imageAR;
            offsetX = (scaledWidth - viewWidth) / 2f;
        } else {
            // Ecranul e mai scurt -> Android a tăiat din sus și jos
            scaledWidth = viewWidth;
            scaledHeight = viewWidth / imageAR;
            offsetY = (scaledHeight - viewHeight) / 2f;
        }

        // 1. Desenăm liniile între puncte, folosind coordonatele recalibrate
        for (int[] connection : POSE_CONNECTIONS) {
            NormalizedLandmark start = landmarks.get(connection[0]);
            NormalizedLandmark end = landmarks.get(connection[1]);

            // Facem (1 - x) pentru a funcționa ca o oglindă (front camera)
            float startX = (1 - start.x()) * scaledWidth - offsetX;
            float startY = start.y() * scaledHeight - offsetY;
            float endX = (1 - end.x()) * scaledWidth - offsetX;
            float endY = end.y() * scaledHeight - offsetY;

            canvas.drawLine(startX, startY, endX, endY, linePaint);
        }

        // 2. Desenăm punctele articulațiilor
        for (NormalizedLandmark landmark : landmarks) {
            float x = (1 - landmark.x()) * scaledWidth - offsetX;
            float y = landmark.y() * scaledHeight - offsetY;
            canvas.drawCircle(x, y, 10f, pointPaint);
        }
    }
}