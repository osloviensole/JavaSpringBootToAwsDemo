package com.example.demo.controller;

import com.example.demo.model.ponacash.PaymentRequest;
import com.example.demo.model.ponacash.PaymentResponse;
import com.example.demo.service.PonaCashClient;
import com.example.demo.service.PonaCashEncryptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PaymentTestController {

    private final PonaCashEncryptionService encryptionService;
    private final PonaCashClient ponaCashClient;

    // Placeholder public key (Normally loaded from file/config)
    private static final String PUBLIC_KEY_PEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr4qvJXtRYTD1LqrDJKmJDuKfZyz4I9z3mSgyaHyQAlBshkpVRs00gTfW5j62xXQrd58D97SsKDonZzhxrXgbeFfnYipgQm0JjEm0S34UtQ5Sr2rcTUvhYNvnSh1iO/mRTyz0V7KgycUBbGrHfo4rECFqDilZ9f/2/XElGNCcpAVvntaddl6dtMZlxyGfoUBKIJ1qxv0hTXTDskn99eJ6vSkOPj9rLHPsdK8RHXNjqy60q9mxMEzVEKeQUgWNstG/GTef1PSOYotxA7B5nnlpUyYyIs1lX7j8LOvAhUQ9i8Xag2PMOwDBEQ9dsh1/NzzR/yuIgcTqHwPhWai1CGEwZwIDAQAB";

    public PaymentTestController(PonaCashEncryptionService encryptionService, PonaCashClient ponaCashClient) {
        this.encryptionService = encryptionService;
        this.ponaCashClient = ponaCashClient;
    }

    @PostMapping("/test-encryption")
    public Map<String, Object> testEncryptionAndPayment(@RequestBody PaymentRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. Encrypt
            PaymentRequest encryptedRequest = encryptionService.encrypt(request, PUBLIC_KEY_PEM);

            // 2. Call API
            PaymentResponse response = ponaCashClient.initiatePayment(encryptedRequest);
            result.put("apiResponse", response);

            result.put("status", "Success");
        } catch (Exception e) {
            result.put("status", "Error");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
