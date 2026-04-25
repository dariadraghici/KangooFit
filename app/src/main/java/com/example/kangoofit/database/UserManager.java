package com.example.kangoofit.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import com.example.kangoofit.model.User;

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
        if (uid != null) {
            db.collection("users").document(uid).update(field, com.google.firebase.firestore.FieldValue.increment(amount));
        }
    }

    // Interfață pentru a trimite datele înapoi în UI
    public interface OnUserUpdateListener {
        void onUpdate(User user);
    }
}