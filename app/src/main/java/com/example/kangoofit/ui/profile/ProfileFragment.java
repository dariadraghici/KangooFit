package com.example.kangoofit.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide; // Va trebui să adaugi dependența Glide în build.gradle
import com.example.kangoofit.R;
import com.example.kangoofit.database.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private LinearLayout layoutLoggedIn, layoutLoggedOut;
    private TextView tvName, tvEmail;
    private ImageView imgProfile;
    private Button btnLogout;
    private View btnLoginGoogle;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = firebaseAuth -> updateUI();

        layoutLoggedIn = view.findViewById(R.id.layout_logged_in);
        layoutLoggedOut = view.findViewById(R.id.layout_logged_out);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        imgProfile = view.findViewById(R.id.img_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnLoginGoogle = view.findViewById(R.id.btn_google_login_profile);

        updateUI();

        btnLoginGoogle.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);

            // Adăugăm această linie:
            intent.putExtra("DIRECT_GOOGLE_LOGIN", true);

            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            // 1. Deconectare
            mAuth.signOut();

            // 2. Navigare către Progres folosind ID-urile tale reale
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        getActivity().findViewById(R.id.bottom_navigation); // ID-ul tău din MainActivity

                if (bottomNav != null) {
                    // ID-ul tău din meniu (nav_progres)
                    bottomNav.setSelectedItemId(R.id.nav_progres);
                }

                Toast.makeText(getContext(), "Te-ai deconectat!", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void updateUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            layoutLoggedIn.setVisibility(View.VISIBLE);
            layoutLoggedOut.setVisibility(View.GONE);

            tvName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .into(imgProfile);
            }
        } else {
            layoutLoggedIn.setVisibility(View.GONE);
            layoutLoggedOut.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}