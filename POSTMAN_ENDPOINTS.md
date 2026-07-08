# Endpoints à tester avec Postman — Bibliothèque Universitaire

Base URL : `http://localhost:8081` (backend Spring Boot, profil dev ou prod — voir README/mémoire pour lancer le serveur).

## Authentification (JWT)

Le token JWT s'obtient via `/api/auth/login` ou `/api/auth/register`, puis se passe dans le header :

```
Authorization: Bearer <token>
```

### Comptes de démo (seedés par `DataInitializer`)

| Rôle | Email | Mot de passe |
|---|---|---|
| Admin | `admin@universite.sn` | `Admin2024!` |
| Bibliothécaire | `biblio@universite.sn` | `Biblio2024!` |
| Étudiant | `etudiant@universite.sn` (ou `fatou@`, `cheikh@`) | `Etudiant2024!` |

---

## 1. Auth — `/api/auth` (publics sauf `/me`)

| Méthode | URL | Auth | Body |
|---|---|---|---|
| POST | `/api/auth/register` | Public | `{ "nom": "...", "email": "...", "password": "..." }` |
| POST | `/api/auth/login` | Public | `{ "email": "...", "password": "..." }` |
| GET | `/api/auth/me` | JWT requis | — |

Réponse login/register : `{ token, type, userId, email, nom, role }`

---

## 2. Livres — `/api/livres`

| Méthode | URL | Auth | Body / Params |
|---|---|---|---|
| GET | `/api/livres` | Public | — |
| GET | `/api/livres/disponibles` | Public | — |
| GET | `/api/livres/recherche?q=...` | Public | query `q` |
| GET | `/api/livres/{id}` | Public | — |
| POST | `/api/livres` | BIBLIOTHECAIRE/ADMIN | `{ titre, isbn, anneePublication, categorie, stockTotal, auteurIds: [], imageUrl, description }` |
| PUT | `/api/livres/{id}` | BIBLIOTHECAIRE/ADMIN | idem POST |
| DELETE | `/api/livres/{id}` | BIBLIOTHECAIRE/ADMIN | — |

---

## 3. Auteurs — `/api/auteurs`

| Méthode | URL | Auth | Body |
|---|---|---|---|
| GET | `/api/auteurs` | Public | — |
| GET | `/api/auteurs/{id}` | Public | — |
| GET | `/api/auteurs/{id}/livres` | Public | — |
| POST | `/api/auteurs` | BIBLIOTHECAIRE/ADMIN | `{ nom, prenom, nationalite }` |
| PUT | `/api/auteurs/{id}` | BIBLIOTHECAIRE/ADMIN | idem POST |
| DELETE | `/api/auteurs/{id}` | BIBLIOTHECAIRE/ADMIN | — |

---

## 4. Emprunts — `/api/emprunts`

| Méthode | URL | Auth | Body |
|---|---|---|---|
| POST | `/api/emprunts` | Authentifié | `{ "livreId": 1 }` |
| GET | `/api/emprunts/mes-emprunts` | Authentifié | — |
| PATCH | `/api/emprunts/{id}/prolonger` | Authentifié (propriétaire) | — |
| GET | `/api/emprunts` | BIBLIOTHECAIRE/ADMIN | — |
| GET | `/api/emprunts/retards` | BIBLIOTHECAIRE/ADMIN | — |
| PATCH | `/api/emprunts/{id}/retour` | BIBLIOTHECAIRE/ADMIN | — |

---

## 5. Réservations — `/api/reservations`

| Méthode | URL | Auth | Body |
|---|---|---|---|
| POST | `/api/reservations` | Authentifié | `{ "livreId": 1 }` |
| GET | `/api/reservations/mes-reservations` | Authentifié | — |
| POST | `/api/reservations/{id}/confirmer` | Authentifié | — |
| DELETE | `/api/reservations/{id}` | Authentifié | — |
| GET | `/api/reservations` | BIBLIOTHECAIRE/ADMIN | — |

---

## 6. Pénalités — `/api/penalites`

| Méthode | URL | Auth | Body |
|---|---|---|---|
| GET | `/api/penalites/mes-penalites` | Authentifié | — |
| GET | `/api/penalites` | BIBLIOTHECAIRE/ADMIN | — |
| PATCH | `/api/penalites/{id}/payer` | BIBLIOTHECAIRE/ADMIN | — |

---

## 7. Notifications — `/api/notifications`

| Méthode | URL | Auth | Body |
|---|---|---|---|
| GET | `/api/notifications/mes-notifications` | Authentifié | — |
| GET | `/api/notifications/non-lues/count` | Authentifié | — |
| PATCH | `/api/notifications/{id}/lue` | Authentifié | — |
| PATCH | `/api/notifications/tout-lu` | Authentifié | — |

---

## 8. Administration — `/api/admin` (ADMIN uniquement)

| Méthode | URL | Body |
|---|---|---|
| POST | `/api/admin/users` | `{ nom, email, password, role }` |
| GET | `/api/admin/users` | — |
| PUT | `/api/admin/users/{id}` | `{ nom, email, password, role, actif }` |
| DELETE | `/api/admin/users/{id}` | — |

---

## 9. Statistiques — `/api/stats` (BIBLIOTHECAIRE/ADMIN)

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/stats/dashboard` | Statistiques du tableau de bord |
| POST | `/api/stats/traitements` | Force l'exécution des jobs planifiés (échéances, retards, expirations de réservation) |

---

## Scénario de test suggéré (dans Postman)

1. **Login admin** → `POST /api/auth/login` → sauvegarder `token` dans une variable Postman (`{{admin_token}}`).
2. **Login étudiant** → sauvegarder `{{student_token}}`.
3. Créer un auteur et un livre (avec `admin_token` ou `biblio_token`).
4. Emprunter le livre avec `student_token` → `POST /api/emprunts`.
5. Vérifier `GET /api/emprunts/mes-emprunts`.
6. Tester réservation sur un livre à stock 0 → `POST /api/reservations`.
7. Retourner l'emprunt en tant que biblio → `PATCH /api/emprunts/{id}/retour`.
8. Vérifier notifications → `GET /api/notifications/mes-notifications`.
9. Déclencher les jobs planifiés → `POST /api/stats/traitements` puis vérifier pénalités/retards.
10. Vérifier `GET /api/stats/dashboard`.

## Astuce Postman

Créez un environnement avec les variables `base_url` (`http://localhost:8081`), `admin_token`, `biblio_token`, `student_token`. Dans l'onglet "Authorization" de chaque requête protégée, choisissez `Bearer Token` et référencez `{{admin_token}}` etc. Vous pouvez aussi automatiser la capture du token avec un script "Tests" sur la requête de login :

```js
const json = pm.response.json();
pm.environment.set("admin_token", json.token);
```
