package com.example.kangoofit.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import com.example.kangoofit.model.User;

import java.util.List;

public class UserManager {
    private static UserManager instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private UserManager() {}

    public static synchronized UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    // --- FUNCȚII DE ACCES ---

    // 1. Ascultă în timp real modificările (pentru UI care se schimbă singur)
    public ListenerRegistration listenToUser(String uid, OnUserUpdateListener listener) {
        return db.collection("users").document(uid)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        User user = value.toObject(User.class);
                        listener.onUpdate(user);
                    }
                });
    }

    // 2. Actualizează un singur câmp (ex: doar pașii sau doar adresa)
    public void updateField(String field, Object value) {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("users").document(uid).update(field, value);
        }
    }

    // 3. Incrementare (folosit special pentru exerciții: flotări++, pași++)
    public void incrementStat(String field, int amount) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        DocumentReference userRef = db.collection("users").document(uid);

        if (field.equals("pasi")) {
            // Doar pașii, fără timestamp
            userRef.update(field, FieldValue.increment(amount));
        } else {
            // Exercițiu fizic -> Update stat + Timestamp (într-o singură interogare)
            userRef.update(
                    field, FieldValue.increment(amount),
                    "lastExerciseTimestamp", System.currentTimeMillis()
            );
        }
    }

    // Interfață pentru a trimite datele înapoi în UI
    public interface OnUserUpdateListener {
        void onUpdate(User user);
    }

    // leaderbord type shi
    public void getTopUsers(OnLeaderboardListener listener) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> userList = queryDocumentSnapshots.toObjects(User.class);
                    listener.onDataReceived(userList);
                });
    }

    public interface OnLeaderboardListener {
        void onDataReceived(List<User> users);
    }
}