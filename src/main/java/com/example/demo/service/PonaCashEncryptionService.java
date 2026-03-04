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
        try {
            // ==================================================================================
            // ÉTAPE 1 : GÉNÉRATION DE LA CLÉ DE SESSION (AES-256)
            // ==================================================================================
            // On génère une clé AES unique pour cette transaction. C'est du chiffrement
            // symétrique.
            // Cette clé servira à chiffrer le gros volume de données (le payload).
            SecretKey aesKey = generateAesKey();

            // Génération du Vecteur d'Initialisation (IV) de 12 octets pour AES-GCM
            // L'IV garantit que deux chiffrements avec la même clé produiront des résultats
            // différents.
            byte[] iv = generateIv();

            // ==================================================================================
            // ÉTAPE 2 : CHIFFREMENT DU PAYLOAD (AES-GCM)
            // ==================================================================================
            // On convertit l'objet PaymentRequest en JSON (sérialisation).
            String jsonPayload = objectMapper.writeValueAsString(request);
            byte[] plaintext = jsonPayload.getBytes(StandardCharsets.UTF_8);

            // Configuration du chiffrement AES en mode GCM (Galois/Counter Mode).
            // Le mode GCM assure à la fois la confidentialité et l'intégrité des données
            // via un Tag.
            Cipher aesCipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv); // Tag de 128 bits
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);

            // Exécution du chiffrement
            byte[] ciphertextWithTag = aesCipher.doFinal(plaintext);

            // Extraction du Tag d'authentification (les 16 derniers octets du résultat GCM)
            // Ce tag permettra au serveur de vérifier que les données n'ont pas été
            // altérées.
            // Java GCM ajoute le tag à la fin du ciphertext.
            int tagLengthBytes = 16;
            int ciphertextLength = ciphertextWithTag.length - tagLengthBytes;

            byte[] ciphertext = Arrays.copyOfRange(ciphertextWithTag, 0, ciphertextLength);
            byte[] tag = Arrays.copyOfRange(ciphertextWithTag, ciphertextLength, ciphertextWithTag.length);

            // ==================================================================================
            // ÉTAPE 3 : CHIFFREMENT DE LA CLÉ AES (RSA-OAEP)
            // ==================================================================================
            // On chiffre la clé AES elle-même avec la Clé Publique RSA de Fouta.
            // C'est du chiffrement asymétrique : seul Fouta (avec sa clé privée) pourra
            // déchiffrer la clé AES.
            // OAEP est un schéma de padding sécurisé obligatoire ici.
            PublicKey rsaPublicKey = loadPublicKey(publicKeyPem);
            Cipher rsaCipher = Cipher.getInstance(RSA_OAEP_PADDING);
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT);
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey, oaepParams);
            byte[] encryptedKeyBytes = rsaCipher.doFinal(aesKey.getEncoded());

            // ==================================================================================
            // ÉTAPE 4 : ASSEMBLAGE DE LA REQUÊTE FINALE
            // ==================================================================================
            // On encode tous les éléments binaires en Base64 pour le transport JSON.
            // L'objet request est mis à jour avec ces valeurs chiffrées.
            request.setEncrypted_key(Base64.getEncoder().encodeToString(encryptedKeyBytes));
            request.setIv(Base64.getEncoder().encodeToString(iv));
            request.setValidation_payload(Base64.getEncoder().encodeToString(ciphertext));
            request.setTag(Base64.getEncoder().encodeToString(tag));

            return request;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement de la demande de paiement", e);
        }
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
