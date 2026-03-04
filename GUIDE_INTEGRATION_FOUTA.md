# Guide d'Intégration Fouta (Java/Spring Boot)

Ce document détaille les étapes techniques pour intégrer le flux de paiement Fouta, en mettant l'accent sur la sécurité (chiffrement hybride) et la structure des données.

## 1. Prérequis

Vous devez disposer des éléments suivants :

- **Clé Publique Fouta** (`Fouta_public_key.pem`) : Pour chiffrer la clé de session AES.
- **Référence Solution** & **Nom d'utilisateur** : Vos identifiants API.
- **URL de l'API** :
  - Auth: `https://Fouta.com/api/kofuta/token/`
  - Transaction: `https://Fouta.com/api/kofuta/financial-transaction/`

## 2. Authentification

L'authentification se fait via une requête POST avec un corps JSON brut pour éviter les problèmes de sérialisation.

**Requête :**

```http
POST /api/kofuta/token/
Content-Type: application/json

{
    "solution_reference": "VOTRE_REF_SOLUTION",
    "client_username": "VOTRE_USERNAME"
}
```

**Réponse :**
Vous recevrez un `access` token (Bearer) à utiliser dans les en-têtes des requêtes suivantes.

## 3. Chiffrement Hybride (RSA + AES)

Fouta utilise un système de chiffrement hybride pour sécuriser les transactions.

### Flux de Chiffrement

1. **Générer une clé AES-256 éphémère** et un vecteur d'initialisation (IV) de 12 octets.
2. **Chiffrer le Payload (AES-GCM)** :
    - Chiffrez le JSON des données de paiement avec la clé AES et l'IV.
    - Résultat : `cypherText` (données chiffrées) et `authTag` (tag d'authentification).
3. **Chiffrer la Clé AES (RSA-OAEP)** :
    - Chiffrez la clé AES générée à l'étape 1 avec la **Clé Publique Fouta** (RSA/ECB/OAEPWithSHA-256AndMGF1Padding).
    - Résultat : `encryptedKey`.
4. **Encoder en Base64** : Tous les résultats binaires (clé chiffrée, IV, payload chiffré, tag) doivent être encodés en Base64.

### Structure Finale de la Requête de Paiement

Le corps de la requête POST envoyée à `/financial-transaction/` doit ressembler à ceci :

```json
{
  "encrypted_key": "BASE64_RSA_ENCRYPTED_AES_KEY",
  "iv": "BASE64_AES_IV",
  "validation_payload": "BASE64_AES_ENCRYPTED_DATA",
  "tag": "BASE64_AES_AUTH_TAG",
  "contribuable": { ... },
  "articles": [ ... ],
  "cout_total": 5000.0,
  "cout_paye": 5000.0,
  "external_id": "TRANS-12345",
  "numero": "242061234567",
  "type_transaction": "PAGE",
  "type_operation": "MAKE_TRANSACTION",
  ...
}
```

> **Note**: Les données en clair (`contribuable`, `articles`, etc.) sont envoyées **en parallèle** des champs chiffrés pour validation par l'API, mais c'est le contenu chiffré qui fait foi.

### Détail des Champs Chiffrés

| Champ | Description | Importance |
| :--- | :--- | :--- |
| **`encrypted_key`** | Clé de session AES (générée aléatoirement) chiffrée avec la **Clé Publique RSA** de Fouta. | **Critique**. Seul Fouta peut déchiffrer cette clé pour lire le reste. |
| **`iv`** | Vecteur d'Initialisation (12 octets aléatoires). | **Confidentialité**. Garantit que deux transactions identiques produisent un texte chiffré différent. |
| **`validation_payload`** | Le JSON complet de la transaction chiffré avec la clé AES (AES-GCM). | **Secret**. Contient toutes les données sensibles (montants, client). Illisible sans la clé AES. |
| **`tag`** | Empreinte numérique (16 octets) générée par AES-GCM. | **Intégrité**. Si une seule lettre est modifiée par un pirate, le tag sera invalide et la transaction rejetée. |

## 4. Règles de Formatage des Données

Pour éviter les erreurs d'intégrité (`Integrity Check Failed`), respectez strictement ces formats :

- **Montants (Articles)** : Les champs `cout` et `penalite` dans les objets `Article` doivent être envoyés sous forme de **String** formatés avec deux décimales (ex: `"5000.00"`, `"0.00"`).
  - *Incorrect* : `5000`, `5000.0`, `Decimal('5000')`
  - *Correct* : `"5000.00"`
- **Résidence Fiscale** : Le champ `residence_fiscale` dans `Contribuable` est obligatoire (peut être une chaîne vide `""`).
- **Type Opération** : Doit être `"MAKE_TRANSACTION"` à la racine.

### Exemple de Payload (Données en clair)

```json
{
    "type_operation": "MAKE_TRANSACTION",
    "type_transaction": "PAGE",
    "operateur": "AIRTEL_MONEY",
    "numero": "242061234567",
    "cout_paye": 5000.0,
    "cout_total": 5000.0,
    "external_id": "CMD-001",
    "contribuable": {
        "name": "Jean Dupont",
        "niu": "M051...",
        "rccm": "CG-BZV...",
        "residence_fiscale": "",
        "email": "jean@exemple.com",
        "adresse": "Brazzaville",
        "tel": "242061234567"
    },
    "articles": [
        {
            "article_nom": "Abonnement",
            "article_id": "SUB-01",
            "article_reference": "REF-01",
            "quantite": 1,
            "cout": "5000.00",    <-- String
            "penalite": "0.00"    <-- String
        }
    ]
}
```

## 5. Gestion de la Réponse

En cas de succès (HTTP 200/201), l'API renvoie :

```json
{
    "message": "Transaction created successfully",
    "transaction_id": "PONA-TRANS-ID",
    "reference": "YOUR-EXTERNAL-ID",
    "url_redirect": "https://Fouta.com/payment/..."
}
```

Redirigez l'utilisateur vers `url_redirect` pour finaliser le paiement.

## 6. Codes d'Erreur Courants

- **401 Unauthorized** : Vérifiez votre token Bearer.
- **401 Integrity Error** : Le payload chiffré ne correspond pas au payload en clair. Vérifiez le formatage des décimales (`String`) et l'ordre des champs.
- **400 Bad Request** : Champ manquant ou invalide (ex: `type_operation`).

---
v1.0 - Généré par Oslovie
