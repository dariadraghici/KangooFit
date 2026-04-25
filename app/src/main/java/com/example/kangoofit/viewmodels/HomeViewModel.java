package com.example.kangoofit.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    // Aici vom ține starea cangurului și datele de pe ecran
    private final MutableLiveData<String> kangarooState = new MutableLiveData<>();

    public HomeViewModel() {
        kangarooState.setValue("HAPPY"); // Starea inițială
    }

    public LiveData<String> getKangarooState() {
        return kangarooState;
    }

    // Metode viitoare pentru a actualiza starea din Repository
}