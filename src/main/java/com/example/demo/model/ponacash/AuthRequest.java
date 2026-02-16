package com.example.demo.model.ponacash;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequest {
    @JsonProperty("solution_reference")
    private String solutionReference;

    @JsonProperty("client_username")
    private String clientUsername;

    public AuthRequest() {
    }

    public AuthRequest(String solutionReference, String clientUsername) {
        this.solutionReference = solutionReference;
        this.clientUsername = clientUsername;
    }

    @JsonProperty("solution_reference")
    public String getSolutionReference() {
        return solutionReference;
    }

    public void setSolutionReference(String solutionReference) {
        this.solutionReference = solutionReference;
    }

    @JsonProperty("client_username")
    public String getClientUsername() {
        return clientUsername;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }
}
