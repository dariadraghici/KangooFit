package com.example.kangoofit;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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
    private static final String START_EXERCISE_PATH = "/start_exercise";
    private static final String REP_UPDATE_PATH = "/rep_update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRepsWatch = findViewById(R.id.tv_reps_watch);
        Button btnPushups = findViewById(R.id.btn_start_pushups);
        Button btnSquats = findViewById(R.id.btn_start_squats);

        btnPushups.setOnClickListener(v -> sendCommandToPhone("PUSHUPS"));
        btnSquats.setOnClickListener(v -> sendCommandToPhone("SQUATS"));
    }

    private void sendCommandToPhone(String exerciseType) {
        tvRepsWatch.setText("Se pornește camera...");

        // Trimitem mesajul pe un thread separat pentru a nu bloca UI-ul
        new Thread(() -> {
            try {
                List<Node> nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
                for (Node node : nodes) {
                    Wearable.getMessageClient(this).sendMessage(
                            node.getId(), START_EXERCISE_PATH, exerciseType.getBytes(StandardCharsets.UTF_8)
                    );
                }
            } catch (ExecutionException | InterruptedException e) {
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