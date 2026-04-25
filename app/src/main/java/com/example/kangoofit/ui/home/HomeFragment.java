package com.example.kangoofit.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.kangoofit.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnLogExercise = view.findViewById(R.id.btn_log_exercise);
        Button btnSyncWatch = view.findViewById(R.id.btn_sync_watch);

        btnLogExercise.setOnClickListener(v ->
                Toast.makeText(getContext(), "Logica pentru exerciții urmează!", Toast.LENGTH_SHORT).show()
        );

        btnSyncWatch.setOnClickListener(v ->
                Toast.makeText(getContext(), "Integrarea WearOS urmează!", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}