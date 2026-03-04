package com.example.demo.service;

import com.example.demo.model.ponacash.AuthRequest;
import com.example.demo.model.ponacash.AuthResponse;
import com.example.demo.model.ponacash.PaymentRequest;
import com.example.demo.model.ponacash.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PonaCashClient {

    @Value("${ponacash.api.url}")
    private String apiUrl;

    @Value("${ponacash.auth.url}")
    private String authUrl;

    @Value("${ponacash.auth.solution-reference}")
    private String solutionReference;

    @Value("${ponacash.auth.client-username}")
    private String clientUsername;

    @Value("${ponacash.redirect.url}")
    private String redirectUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String authenticate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build raw JSON string to match exact curl format
        String jsonBody = "{\"solution_reference\": \"" + solutionReference + "\", \"client_username\": \""
                + clientUsername + "\"}";
        System.out.println("DEBUG: Auth request body: " + jsonBody);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                authUrl,
                HttpMethod.POST,
                entity,
                AuthResponse.class);

        if (response.getBody() != null) {
            System.out.println("DEBUG: Auth successful, token_type=" + response.getBody().getToken_type());
            return response.getBody().getAccess();
        }
        throw new RuntimeException("Authentication failed: No response body");
    }

    public PaymentResponse initiatePayment(PaymentRequest request) {
        // 1. Authenticate to get fresh token
        String token = authenticate();

        // 2. Serialize request to JSON to debug
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(request);
            System.out.println("DEBUG: Payment request JSON: " + jsonBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payment request", e);
        }

        // 3. Make Payment Request with raw JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        headers.set("X-Solution-ID", solutionReference);
        headers.set("X-Redirect-Url", redirectUrl);
        System.out.println("DEBUG: Payment headers - Authorization: Bearer "
                + token.substring(0, Math.min(50, token.length())) + "...");
        System.out.println("DEBUG: Payment headers - X-Solution-ID: " + solutionReference);
        System.out.println("DEBUG: Payment headers - X-Redirect-Url: " + redirectUrl);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<PaymentResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                PaymentResponse.class);

        return response.getBody();
    }
}
