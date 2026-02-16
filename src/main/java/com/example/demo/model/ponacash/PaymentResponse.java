package com.example.demo.model.ponacash;

public class PaymentResponse {
    private String message;

    @com.fasterxml.jackson.annotation.JsonProperty("transaction_id")
    private String transactionId;

    private String reference;

    @com.fasterxml.jackson.annotation.JsonProperty("url_redirect")
    private String urlRedirect;

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getUrlRedirect() {
        return urlRedirect;
    }

    public void setUrlRedirect(String urlRedirect) {
        this.urlRedirect = urlRedirect;
    }
}
