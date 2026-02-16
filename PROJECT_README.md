# Documentation Technique du Projet PonaCash (Java/Spring Boot)

Ce projet implémente un client de paiement sécurisé pour l'API PonaCash, utilisant un chiffrement hybride (RSA + AES) pour protéger les transactions financières.

## 1. Structure du Projet

L'intégration est organisée autour de trois packages principaux :

### 1.1 Modèles (`com.example.demo.model.ponacash`)

Ces classes (DTO) servent à mapper les requêtes et réponses JSON de l'API.

- **`PaymentRequest.java`** : L'objet principal envoyé à l'API. Il contient à la fois :
  - Les champs chiffrés (`encrypted_key`, `iv`, `validation_payload`, `tag`).
  - Les champs en clair pour validation (`contribuable`, `articles`, `cout_paye`, etc.).
- **`PaymentResponse.java`** : Mappe la réponse de succès (`transaction_id`, `reference`, `url_redirect`).
- **`AuthRequest.java` / `AuthResponse.java`** : Gèrent l'authentification (récupération du token Bearer).
- **`Contribuable.java`** : Informations sur le payeur (nom, email, *residence_fiscale*).
- **`Article.java`** : Détails des articles.
  - *Point important* : `cout` et `penalite` sont stockés en **String** (`"5000.00"`) pour garantir une correspondance exacte avec le type `Decimal` du serveur lors du contrôle d'intégrité.

### 1.2 Services (`com.example.demo.service`)

Contiennent la logique métier et technique.

- **`PonaCashClient.java`** : Gère la communication HTTP.
  - `authenticate()` : Récupère le token JWT. Envoie un JSON brut pour éviter les erreurs de sérialisation.
  - `initiatePayment(PaymentRequest)` : Orchestre l'appel à l'API de transaction. Ajoute les en-têtes requis (`Authorization`, `X-Solution-ID`, `X-Redirect-Url`).
- **`PonaCashEncryptionService.java`** : Le cœur de la sécurité. Implémente le chiffrement hybride :
    1. Génération d'une clé AES-256 éphémère.
    2. Chiffrement du payload JSON avec AES-GCM (produit `validation_payload` et `tag`).
    3. Chiffrement de la clé AES avec la clé publique RSA de PonaCash (produit `encrypted_key`).

### 1.3 Contrôleurs (`com.example.demo.controller`)

Points d'entrée pour tester l'intégration.

- **`PaymentTestController.java`** : Expose l'endpoint `/test-encryption`.
  - Reçoit un JSON brut depuis le frontend (`test-payment.html`).
  - Appelle `PonaCashEncryptionService` pour chiffrer les données.
  - Appelle `PonaCashClient` pour initier le paiement.
  - Renvoie le résultat complet (payload chiffré + réponse API).

## 2. Flux de Chiffrement & Intégration

### Étape 1 : Configuration

Le fichier `application.properties` contient les clés et URLs :

```properties
ponacash.api.url=https://ponacash.com/api/kofuta/financial-transaction/
ponacash.auth.url=https://ponacash.com/api/kofuta/token/
ponacash.public.key.path=classpath:ponacash_public_key.pem
```

### Étape 2 : Création de la Requête

Dans `PaymentTestController`, nous construisons l'objet `PaymentRequest` avec les données métier (montant, client, articles).

### Étape 3 : Chiffrement (Code)

```java
// Exemple simplifié de PonaCashEncryptionService
public void encryptPaymentRequest(PaymentRequest request) {
    // 1. Générer clé AES
    SecretKey aesKey = generateAESKey(256);
    byte[] iv = generateIV(12);

    // 2. Chiffrer Payload (AES-GCM)
    String payloadJson = objectMapper.writeValueAsString(request); // Sérialise les champs métier
    byte[] cipherText = aesGcmEncrypt(payloadJson, aesKey, iv);

    // 3. Chiffrer Clé AES (RSA-OAEP)
    byte[] encryptedAesKey = rsaEncrypt(aesKey.getEncoded(), publicKey);

    // 4. Remplir les champs chiffrés
    request.setEncrypted_key(Base64.encode(encryptedAesKey));
    request.setIv(Base64.encode(iv));
    request.setValidation_payload(Base64.encode(cipherText));
    request.setTag(Base64.encode(authTag));
}
```

### Étape 4 : Envoi

Le client HTTP envoie l'objet `PaymentRequest` complet (champs clairs + champs chiffrés) à PonaCash.

## 3. Points d'Attention (Troubleshooting)

 Lors de l'intégration, deux erreurs sont fréquentes :

1. **Erreur d'Intégrité (`401 Integrity Error`)** :
    - Cause : Le JSON chiffré (côté client) ne correspond pas exactement au JSON reconstruit par le serveur.
    - Solution : Assurez-vous que les montants (`cout`, `penalite`) sont des chaînes de caractères (`"5000.00"`) et non des nombres (`5000.0`), et que le champ `residence_fiscale` est présent.

2. **Erreur de Sérialisation Auth** :
    - Cause : Jackson peut mal nommer les champs (`snake_case` vs `camelCase`).
    - Solution : Utilisez `@JsonProperty` sur les DTOs ou envoyez un JSON brut pour l'authentification.

---
v1.0 - Généré par Antigravity
