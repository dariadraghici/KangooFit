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

    // NOU: Vom salva dimensiunile reale ale imaginii analizate
    private int imageWidth = 480;
    private int imageHeight = 640;

    private static final int[][] POSE_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 7}, {0, 4}, {4, 5}, {5, 6}, {6, 8}, {9, 10},
            {11, 12}, {11, 13}, {13, 15}, {15, 17}, {15, 19}, {15, 21}, {17, 19},
            {12, 14}, {14, 16}, {16, 18}, {16, 20}, {16, 22}, {18, 20}, {11, 23},
            {12, 24}, {23, 24}, {23, 25}, {25, 27}, {27, 29}, {27, 31}, {29, 31},
            {24, 26}, {26, 28}, {28, 30}, {28, 32}, {30, 32}
    };

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    public void setResults(PoseLandmarkerResult result) {
        this.poseResult = result;
        postInvalidate();
    }

    // NOU: Funcție care preia proporțiile de la cameră
    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (poseResult == null || poseResult.landmarks().isEmpty()) return;

        List<NormalizedLandmark> landmarks = poseResult.landmarks().get(0);
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Calculăm aspect ratio pe baza dimensiunilor REALE ale imaginii prelucrate
        float imageAR = (float) imageWidth / imageHeight;
        float viewAR = (float) viewWidth / viewHeight;

        float scaledWidth;
        float scaledHeight;
        float offsetX = 0;
        float offsetY = 0;

        if (viewAR < imageAR) {
            scaledHeight = viewHeight;
            scaledWidth = viewHeight * imageAR;
            offsetX = (scaledWidth - viewWidth) / 2f;
        } else {
            scaledWidth = viewWidth;
            scaledHeight = viewWidth / imageAR;
            offsetY = (scaledHeight - viewHeight) / 2f;
        }

        // Desenăm liniile (Fără `1 - x`, deoarece imaginea e oglindită în prealabil!)
        for (int[] connection : POSE_CONNECTIONS) {
            NormalizedLandmark start = landmarks.get(connection[0]);
            NormalizedLandmark end = landmarks.get(connection[1]);

            float startX = start.x() * scaledWidth - offsetX;
            float startY = start.y() * scaledHeight - offsetY;
            float endX = end.x() * scaledWidth - offsetX;
            float endY = end.y() * scaledHeight - offsetY;

            canvas.drawLine(startX, startY, endX, endY, linePaint);
        }

        // Desenăm punctele
        for (NormalizedLandmark landmark : landmarks) {
            float x = landmark.x() * scaledWidth - offsetX;
            float y = landmark.y() * scaledHeight - offsetY;
            canvas.drawCircle(x, y, 10f, pointPaint);
        }
    }
}