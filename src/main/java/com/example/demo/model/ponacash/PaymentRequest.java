package com.example.demo.model.ponacash;

import java.util.List;

public class PaymentRequest {
    private Contribuable contribuable;
    private List<Article> articles;
    private double cout_paye;
    private double cout_total;
    private String external_id;
    private String numero;
    private String numero_agence;
    private String code_secret;
    private String type_transaction;
    private String operateur;
    private String message;
    private String type_operation;
    private String callback;

    // Crypto fields
    private String encrypted_key;
    private String iv;
    private String validation_payload;
    private String tag;

    // Getters and Setters
    public Contribuable getContribuable() {
        return contribuable;
    }

    public void setContribuable(Contribuable contribuable) {
        this.contribuable = contribuable;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public double getCout_paye() {
        return cout_paye;
    }

    public void setCout_paye(double cout_paye) {
        this.cout_paye = cout_paye;
    }

    public double getCout_total() {
        return cout_total;
    }

    public void setCout_total(double cout_total) {
        this.cout_total = cout_total;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNumero_agence() {
        return numero_agence;
    }

    public void setNumero_agence(String numero_agence) {
        this.numero_agence = numero_agence;
    }

    public String getCode_secret() {
        return code_secret;
    }

    public void setCode_secret(String code_secret) {
        this.code_secret = code_secret;
    }

    public String getType_transaction() {
        return type_transaction;
    }

    public void setType_transaction(String type_transaction) {
        this.type_transaction = type_transaction;
    }

    public String getOperateur() {
        return operateur;
    }

    public void setOperateur(String operateur) {
        this.operateur = operateur;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType_operation() {
        return type_operation;
    }

    public void setType_operation(String type_operation) {
        this.type_operation = type_operation;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getEncrypted_key() {
        return encrypted_key;
    }

    public void setEncrypted_key(String encrypted_key) {
        this.encrypted_key = encrypted_key;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getValidation_payload() {
        return validation_payload;
    }

    public void setValidation_payload(String validation_payload) {
        this.validation_payload = validation_payload;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
