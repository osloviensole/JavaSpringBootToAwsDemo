# Guide d'Intégration : Paiement Sécurisé PonaCash (Mode Hybride RSA+AES)

Ce guide explique comment implémenter le chiffrement hybride RSA-OAEP + AES-GCM pour sécuriser les appels à l'API de paiement PonaCash.

## Principe

Afin de garantir la confidentialité et l'intégrité des transactions, vous devez chiffrer le payload de votre requête en utilisant une approche hybride :

1. **AES-GCM (Symétrique)** pour chiffrer les données (rapide et sécurisé).
2. **RSA-OAEP (Asymétrique)** pour chiffrer la clé AES (échange de clé sécurisé).

Le serveur PonaCash déchiffre le payload et compare son contenu avec les données envoyées en clair pour valider l'intégrité de la transaction.

---

## 1. Prérequis

Vous avez besoin de :

- La **Clé Publique RSA** de PonaCash (format PEM).
- Une bibliothèque cryptographique supportant **AES-GCM** et **RSA-OAEP avec SHA-256**.

---

## 2. Structure du Payload

Votre requête POST doit contenir :

- Les champs métier **en clair** (pour le traitement immédiat).
- 4 champs cryptographiques (pour la validation de sécurité).

### Endpoint

`POST /api/kofuta/financial-transaction/`

### Exemple de JSON complet

```json
{
    "contribuable": {
        "name": "Entreprise SARL",
        "niu": "M051234567890X",
        "rccm": "CG-BZV-01-2023-B12-00123",
        "tel": "242068518115",
        "email": "exemple@mail.com",
        "adresse": "123 Avenue de la République, Brazzaville"
    },
    "articles": [
        {
            "article_nom": "Abonnement Premium",
            "article_id": "SUB-001",
            // NOUVEAU CHAMP ARTICLE
            "article_reference": "REF-ART-2024-X",
            "quantite": 1,
            "cout": 5000,
            "penalite": 0
        },
        {
            "article_nom": "Frais de service",
            "article_id": "FEE-001",
            // NOUVEAU CHAMP ARTICLE
            "article_reference": "REF-ART-2024-X",
            "quantite": 1,
            "cout": 500,
            "penalite": 0
        }
    ],
    // NOUVEUX CHAMPS GLOBAUX
    "cout_paye": 4000.00, // Montant que le client va payer
    "cout_total": 5500.00, // Montant total théorique (doit être >= cout_paye)
    "external_id": "TRANS-123456789",
    "numero": "242061245540", // en cas de banque c'est le numero de compte
    "numero_agence": "", // en cas de banque
    "code_secret": "", // en cas de banque
    "type_transaction": "PAGE",
    "operateur": "879401bc-f547-4326-8c89-16b457ba7ff8", // api_key du moyen de paiement
    "message": "Paiement facture",
    "type_operation": "MAKE_TRANSACTION",
    "callback": "https://mon-site.com/callback",
    
    // --- Champs Crypto (Mode Hybride) ---
    "encrypted_key": "Base64(RSA_OAEP(clé_AES_256))",
    "iv": "Base64(12 bytes aléatoires)",
    "validation_payload": "Base64(AES_GCM(tout_le_payload_JSON))",
    "tag": "Base64(16 bytes)"
}
```

---

## 3. Workflow de Chiffrement (Étape par Étape)

Pour générer les 4 champs crypto, suivez ces étapes à chaque requête :

### Étape A : Préparer les données

Sérialisez votre objet JSON métier (sans les champs crypto) en une chaîne de caractères, puis en octets (UTF-8).
> **Note :** L'ordre des champs n'importe pas, mais la structure JSON doit être valide.

### Étape B : Générer les secrets (Session)

1. Générez une clé **AES-256** aléatoire (32 octets).
2. Générez un **IV** (Vecteur d'Initialisation) aléatoire de **12 octets**.

### Étape C : Chiffrement AES-GCM (Symétrique)

Chiffrez le JSON (étape A) avec la clé AES et l'IV.

- Algorithme : AES-GCM
- Données : JSON métier complet
- Résultat : Vous obtenez le **ciphertext** et le **tag** d'authentification (16 octets).
- **Action :** Encodez le ciphertext en Base64 → champ `validation_payload`.
- **Action :** Encodez le tag en Base64 → champ `tag`.
- **Action :** Encodez l'IV en Base64 → champ `iv`.

### Étape D : Chiffrement RSA (Asymétrique)

Chiffrez la clé AES (32 octets) avec la clé publique RSA de PonaCash.

- Algorithme : RSA-OAEP
- Hachage : SHA-256
- MGF1 Hachage : SHA-256
- **Action :** Encodez le résultat en Base64 → champ `encrypted_key`.

---

## 4. Exemple d'Implémentation (Python)

```python
import json
import os
import base64
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

# 1. Charger la clé publique PonaCash
with open("ponacash_public_key.pem", "rb") as f:
    public_key = serialization.load_pem_public_key(f.read())

# 2. Données métier
payload = {
    "type_operation": "MAKE_TRANSACTION",
    "operateur": "uuid-...",
    "contribuable": {"name": "Jean", "tel": "242..."},
    "cout_paye": 5000,
    # ... autres champs
}

# 3. Préparation AES
aes_key = os.urandom(32)  # Clé AES-256
iv = os.urandom(12)       # IV 12 bytes

# 4. Chiffrement AES-GCM du payload
aesgcm = AESGCM(aes_key)
# Sérialiser tout le payload métier en JSON UTF-8
plaintext = json.dumps(payload).encode('utf-8')

# AES-GCM retourne ciphertext + tag concaténés
ciphertext_with_tag = aesgcm.encrypt(nonce=iv, data=plaintext, associated_data=None)

# Séparation ciphertext / tag
ciphertext = ciphertext_with_tag[:-16]
tag = ciphertext_with_tag[-16:]

# 5. Chiffrement RSA de la clé AES
encrypted_key = public_key.encrypt(
    aes_key,
    padding.OAEP(
        mgf=padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None
    )
)

# 6. Construction du payload final
final_payload = payload.copy()
final_payload.update({
    "encrypted_key": base64.b64encode(encrypted_key).decode('utf-8'),
    "iv": base64.b64encode(iv).decode('utf-8'),
    "validation_payload": base64.b64encode(ciphertext).decode('utf-8'),
    "tag": base64.b64encode(tag).decode('utf-8')
})

print(json.dumps(final_payload, indent=2))
```

## 5. Gestion des Erreurs

Si votre intégration est incorrecte, l'API retournera :

- **400 Bad Request** : Base64 invalide, champ manquant, ou IV/tag de mauvaise longueur.
- **401 Unauthorized** :
  - `Tag GCM invalide` : La clé AES ou le tag ne correspond pas au ciphertext.
  - `Intégrité compromise` : Les données en clair diffèrent des données chiffrées (tentative de modification).

---
**Sécurité :** Ne jamais réutiliser la même clé AES ou le même IV pour plusieurs transactions. Générez-en de nouveaux à chaque appel.
