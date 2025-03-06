package com.example.demo.model;

import java.time.LocalDate;

public class User {
    private final String nom;
    private final String postnom;
    private final int age;
    private final String sexe;
    private final LocalDate dateCreation;

    public User(String nom, String postnom, int age, String sexe, LocalDate dateCreation) {
        this.nom = nom;
        this.postnom = postnom;
        this.age = age;
        this.sexe = sexe;
        this.dateCreation = dateCreation;
    }

    public String getNom() { return nom; }
    public String getPostnom() { return postnom; }
    public int getAge() { return age; }
    public String getSexe() { return sexe; }
    public LocalDate getDateCreation() { return dateCreation; }
}
