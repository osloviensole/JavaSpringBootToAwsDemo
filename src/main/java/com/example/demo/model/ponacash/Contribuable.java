package com.example.demo.model.ponacash;

public class Contribuable {
    private String name;
    private String niu;
    private String rccm;
    private String residence_fiscale = "";
    private String tel;
    private String email;
    private String adresse;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNiu() {
        return niu;
    }

    public void setNiu(String niu) {
        this.niu = niu;
    }

    public String getRccm() {
        return rccm;
    }

    public void setRccm(String rccm) {
        this.rccm = rccm;
    }

    public String getResidence_fiscale() {
        return residence_fiscale;
    }

    public void setResidence_fiscale(String residence_fiscale) {
        this.residence_fiscale = residence_fiscale;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
