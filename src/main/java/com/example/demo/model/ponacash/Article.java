package com.example.demo.model.ponacash;

public class Article {
    private String article_nom;
    private String article_id;
    private String article_reference;
    private int quantite;
    private String cout;
    private String penalite;

    // Getters and Setters
    public String getArticle_nom() {
        return article_nom;
    }

    public void setArticle_nom(String article_nom) {
        this.article_nom = article_nom;
    }

    public String getArticle_id() {
        return article_id;
    }

    public void setArticle_id(String article_id) {
        this.article_id = article_id;
    }

    public String getArticle_reference() {
        return article_reference;
    }

    public void setArticle_reference(String article_reference) {
        this.article_reference = article_reference;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getCout() {
        return cout;
    }

    public void setCout(String cout) {
        this.cout = cout;
    }

    public String getPenalite() {
        return penalite;
    }

    public void setPenalite(String penalite) {
        this.penalite = penalite;
    }
}
