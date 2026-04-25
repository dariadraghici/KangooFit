package com.example.kangoofit.model;

public class User {
    public String name, email, address;
    public long createdAt;
    public int flotari, genoflexiuni, pasi, nivel_kangaroo;
    public String stare_kangaroo;

    public User() {} // Necesar pentru Firebase

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