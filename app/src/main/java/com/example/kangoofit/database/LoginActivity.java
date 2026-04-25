package com.example.kangoofit.database; // Verifică să fie pachetul tău real
import com.example.kangoofit.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.kangoofit.MainActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "GoogleSignIn";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Verificăm semnalul înainte de orice
        boolean isDirectLogin = getIntent().getBooleanExtra("DIRECT_GOOGLE_LOGIN", false);

        // 2. Încărcăm layout-ul și butoanele DOAR dacă nu e login direct
        if (!isDirectLogin) {
            setContentView(R.layout.activity_login);
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
            findViewById(R.id.btn_google_login).setOnClickListener(v -> signIn());
        }

        // 3. Inițializăm restul serviciilor (astea merg oricum)
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Launcher-ul trebuie înregistrat mereu aici (înainte de onStart)
        setupSignInLauncher();

        // 4. Dacă e direct, sărim la bătaie!
        if (isDirectLogin) {
            signIn();
        }
    }

    // Mută înregistrarea launcher-ului într-o metodă separată ca să fie curat
    private void setupSignInLauncher() {
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.e(TAG, "Google sign in failed", e);
                            finish(); // Închidem dacă eșuează
                        }
                    } else {
                        finish(); // Închidem dacă userul dă "Back" din fereastra Google
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserInFirestore(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Autentificare Firebase eșuată.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInFirestore(FirebaseUser fUser) {
        if (fUser == null) return;

        DocumentReference userRef = db.collection("users").document(fUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                // UTILIZATOR NOU - Creăm profilul cu toate câmpurile cerute
                Map<String, Object> userData = new HashMap<>();
                userData.put("nume", fUser.getDisplayName());
                userData.put("email", fUser.getEmail());
                userData.put("adresa", "Nesetată");
                userData.put("createdAt", System.currentTimeMillis());

                // Exerciții vechi
                userData.put("flotari", 0);
                userData.put("genoflexiuni", 0);
                userData.put("pasi", 0);

                // --- AM ADĂUGAT CÂMPURILE NOI PENTRU FIREBASE ---
                userData.put("jumping_jacks", 0);
                userData.put("biceps", 0);
                userData.put("umeri", 0);
                // ------------------------------------------------

                userData.put("nivel_kangaroo", 1);
                userData.put("stare_kangaroo", "HAPPY");

                userRef.set(userData).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil creat cu succes!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                }).addOnFailureListener(e -> Log.e(TAG, "Error saving user", e));
            } else {
                // UTILIZATOR VECHI - Doar intrăm în aplicație
                goToMainActivity();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Închidem LoginActivity ca să nu poată merge "Back" la ea
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}