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

public class ProgressFragment extends Fragment {

    private ProgressBar progressBarPasi;
    private TextView txtPasi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Legăm fișierul Java de layout-ul XML pe care l-ai făcut
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // Inițializăm elementele din UI
        progressBarPasi = view.findViewById(R.id.progressPasi);
        txtPasi = view.findViewById(R.id.txtPasi);

        // Exemplu: Setăm pașii din cod (aici vei aduce datele din Firebase mai târziu)
        updateProgress(8450, 10000);

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