package com.example.demo.service;

import com.example.demo.model.ponacash.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Arrays;

@Service
public class PonaCashEncryptionService {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final String RSA_OAEP_PADDING = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16 * 8; // in bits

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Placeholder public key (Replace with actual PEM content loading logic)
    private static final String PUBLIC_KEY_PEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr4qvJXtRYTD1LqrDJKmJDuKfZyz4I9z3mSgyaHyQAlBshkpVRs00gTfW5j62xXQrd58D97SsKDonZzhxrXgbeFfnYipgQm0JjEm0S34UtQ5Sr2rcTUvhYNvnSh1iO/mRTyz0V7KgycUBbGrHfo4rECFqDilZ9f/2/XElGNCcpAVvntaddl6dtMZlxyGfoUBKIJ1qxv0hTXTDskn99eJ6vSkOPj9rLHPsdK8RHXNjqy60q9mxMEzVEKeQUgWNstG/GTef1PSOYotxA7B5nnlpUyYyIs1lX7j8LOvAhUQ9i8Xag2PMOwDBEQ9dsh1/NzzR/yuIgcTqHwPhWai1CGEwZwIDAQAB";

    public PaymentRequest encrypt(PaymentRequest request, String publicKeyPem) throws Exception {
        // 1. Prepare Data
        // Create a copy of the request to serialize (excluding crypto fields, though
        // they are null anyway)
        // In practice, we just serialize the current state of the DTO which should have
        // business data
        String jsonPayload = objectMapper.writeValueAsString(request);
        byte[] plaintext = jsonPayload.getBytes(StandardCharsets.UTF_8);

        // 2. Generate Secrets
        SecretKey aesKey = generateAesKey();
        byte[] iv = generateIv();

        // 3. AES-GCM Encryption
        Cipher aesCipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);

        byte[] ciphertextWithTag = aesCipher.doFinal(plaintext);

        // Extract ciphertext and tag
        // Java's GCM implementation appends the tag to the end of the ciphertext
        int tagLengthBytes = 16;
        int ciphertextLength = ciphertextWithTag.length - tagLengthBytes;

        byte[] ciphertext = Arrays.copyOfRange(ciphertextWithTag, 0, ciphertextLength);
        byte[] tag = Arrays.copyOfRange(ciphertextWithTag, ciphertextLength, ciphertextWithTag.length);

        // 4. RSA-OAEP Encryption of AES Key
        PublicKey rsaPublicKey = loadPublicKey(publicKeyPem);
        Cipher rsaCipher = Cipher.getInstance(RSA_OAEP_PADDING);
        OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT);
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey, oaepParams);
        byte[] encryptedKeyBytes = rsaCipher.doFinal(aesKey.getEncoded());

        // 5. Populate Request
        request.setEncrypted_key(Base64.getEncoder().encodeToString(encryptedKeyBytes));
        request.setIv(Base64.getEncoder().encodeToString(iv));
        request.setValidation_payload(Base64.getEncoder().encodeToString(ciphertext));
        request.setTag(Base64.getEncoder().encodeToString(tag));

        return request;
    }

    private SecretKey generateAesKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private PublicKey loadPublicKey(String pem) throws Exception {
        String publicKeyPEM = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }
}
