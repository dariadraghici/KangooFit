package com.example.kangoofit.ui.progress;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView; // Import necesar
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kangoofit.R;
import com.example.kangoofit.database.LoginActivity;
import com.example.kangoofit.database.UserManager;
import com.example.kangoofit.model.KangarooLevel; // Importă modelul de nivele
import com.google.firebase.auth.FirebaseAuth;

public class ProgressFragment extends Fragment {

    private ProgressBar progressBarPasi;
    private TextView txtPasi;
    private TextView txtFlotari;
    private TextView txtGenoflexiuni;
    private ImageView imgMascota; // Variabilă pentru imaginea de banner

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // 1. Inițializăm elementele din UI
        progressBarPasi = view.findViewById(R.id.progressPasi);
        txtPasi = view.findViewById(R.id.txtPasi);
        txtFlotari = view.findViewById(R.id.txtFlotari);
        txtGenoflexiuni = view.findViewById(R.id.txtGenoflexiuni);
        imgMascota = view.findViewById(R.id.imgMascota); // ID-ul din XML-ul tău

        // 2. Configurări inițiale
        int targetPasi = 10000;
        progressBarPasi.setMax(targetPasi);

        // 3. Ascultăm datele din Firebase în timp real
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {

                    // ACTUALIZĂM MASCOTA (Banner-ul)
                    int nivel = user.nivel_kangaroo > 0 ? user.nivel_kangaroo : 1;
                    imgMascota.setImageResource(KangarooLevel.getDrawableResId(nivel));

                    // Actualizăm Pașii
                    txtPasi.setText(String.valueOf(user.pasi));
                    progressBarPasi.setProgress(user.pasi);

                    // Actualizăm textele pentru exerciții
                    txtFlotari.setText(user.flotari + " Flotări");
                    txtGenoflexiuni.setText(user.genoflexiuni + " Genoflexiuni");
                }
            });
        }

        // --- BUTON TEST ---
        Button btnTest = view.findViewById(R.id.btn_test_login);
        if (btnTest != null) {
            btnTest.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }
}