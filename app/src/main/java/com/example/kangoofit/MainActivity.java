package com.example.kangoofit;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.kangoofit.ui.home.HomeFragment;
import com.example.kangoofit.ui.exercises.ExercisesFragment; // Importul noului fragment
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Setăm fragmentul inițial (Cangurul)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_cangur) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_progres) {
                // selectedFragment = new ProgressFragment();
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_exercitii) { // <--- AICI AM MODIFICAT
                selectedFragment = new ExercisesFragment(); // Deschidem Fragmentul de Exerciții
            } else if (itemId == R.id.nav_comunitate) {
                // selectedFragment = new CommunityFragment();
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_profil) {
                // selectedFragment = new ProfileFragment();
                selectedFragment = new HomeFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}