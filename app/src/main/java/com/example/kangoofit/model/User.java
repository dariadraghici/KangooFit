package com.example.kangoofit.model;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties // Ignoră câmpurile din DB care nu sunt în Java (previne crash-uri)
public class User {

    @PropertyName("nume") // Forțează Firebase să pună valoarea din cheia "nume" aici
    public String name;

    public String email, address, stare_kangaroo;
    public long createdAt;
    public int flotari, genoflexiuni, pasi, nivel_kangaroo;

    // CONSTRUCTORUL GOL ESTE OBLIGATORIU pentru Firebase
    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.address = "Nesetată";
        this.createdAt = System.currentTimeMillis();
        this.flotari = 0;
        this.genoflexiuni = 0;
        this.pasi = 0;
        this.nivel_kangaroo = 1;
        this.stare_kangaroo = "HAPPY";
    }
}