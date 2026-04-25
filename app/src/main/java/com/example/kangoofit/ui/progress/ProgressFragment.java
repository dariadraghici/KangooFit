package com.example.kangoofit.ui.progress; // Asigură-te că e pachetul tău

import android.content.Intent; // Import nou pentru navigare
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import pentru buton
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kangoofit.R;
import com.example.kangoofit.database.LoginActivity;
import com.example.kangoofit.database.UserManager;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class ProgressFragment extends Fragment {

    private ProgressBar progressBarPasi;
    private TextView txtPasi;
    private TextView txtFlotari;
    private TextView txtGenoflexiuni;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Legăm fișierul Java de layout-ul XML pe care l-ai făcut
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // Inițializăm elementele din UI
        progressBarPasi = view.findViewById(R.id.progressPasi);
        txtPasi = view.findViewById(R.id.txtPasi);
        txtFlotari = view.findViewById(R.id.txtFlotari);
        txtGenoflexiuni = view.findViewById(R.id.txtGenoflexiuni);

        // update pasi din database
        int targetPasi = 10000;
        progressBarPasi.setMax(targetPasi);

        // 3. Ascultăm datele din Firebase prin UserManager-ul tău
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserManager.getInstance().listenToUser(uid, user -> {
                if (isAdded() && user != null) {
                    // Actualizăm TEXTUL
                    txtPasi.setText(String.valueOf(user.pasi));

                    // Actualizăm CERCUL (Progresul)
                    // Progresul se va muta automat la valoarea nouă
                    progressBarPasi.setProgress(user.pasi);

                    // update flotari si genoflexiuni
                    txtFlotari.setText(String.valueOf(user.flotari) + " Flotări");
                    txtGenoflexiuni.setText(String.valueOf(user.genoflexiuni) + " Genoflexiuni");
                }
            });
        }

        // --- COD PENTRU BUTONUL DE TEST ---
        Button btnTest = view.findViewById(R.id.btn_test_login);
        btnTest.setOnClickListener(v -> {
            // Deschide pagina de login pe care am făcut-o anterior
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
        // ----------------------------------

        return view;
    }

    // Funcție utilă pentru a actualiza cercul de progres
    public void updateProgress(int pasiActuali, int targetPasi) {
        if (progressBarPasi != null) {
            progressBarPasi.setMax(targetPasi);
            progressBarPasi.setProgress(pasiActuali);
        }
        if (txtPasi != null) {
            txtPasi.setText(String.valueOf(pasiActuali));
        }
    }
}