# Bibliothèque Universitaire — Application Web Complète

Application de gestion d'une bibliothèque universitaire développée dans le cadre du module
**Développement Backend (Spring Boot) & Frontend (Angular)**.

**Enrichi** avec des
fonctionnalités avancées pour dépasser le niveau de base :

- **Réservations avec file d'attente FIFO** quand un livre est épuisé
- **Pénalités de retard calculées automatiquement** au retour (jours × tarif)
- **Notifications d'échéance** (in-app) via une **tâche planifiée** `@Scheduled`
- **Tableau de bord statistiques** pour le bibliothécaire (compteurs + graphiques)
- **Multi-auteurs** : relation **N-N** Livre ↔ Auteur
- **Frontend Angular complet** (Angular Material) par-dessus l'API

---

## Architecture

```
SpringAngular/
├── BuBackend/                 # API REST Spring Boot 4 (Java 26)
│   └── src/main/java/com/projet/bibliotheque/
│       ├── config/            # Security, CORS, Beans, DataInitializer, propriétés métier
│       ├── controller/        # Endpoints REST
│       ├── service/           # Logique métier (IoC / injection par constructeur)
│       ├── repository/        # Spring Data JPA
│       ├── model/             # Entités JPA
│       ├── dto/               # Records (contrat d'API — jamais les entités)
│       ├── security/          # JWT (service, filtre, utilisateur courant)
│       └── exception/         # Gestion globale des erreurs
└── BuFrontend/                # SPA Angular 22 + Angular Material
    └── src/app/
        ├── core/              # Modèles, services API, AuthService, intercepteur, guards
        ├── shared/            # Cloche de notifications
        └── pages/             # Catalogue, détail, espace étudiant, espace bibliothécaire
```

### Stack technique
| Couche | Technologies |
|---|---|
| Backend | Spring Boot 4.1, Java 26, Spring Web MVC, Spring Data JPA, Spring Security 7, **JWT (jjwt 0.13)** |
| Base de données | **MySQL** (par défaut) · **H2** en mémoire (profil `dev`) |
| Frontend | **Angular 22** (standalone, signals, zoneless), **Angular Material**, RxJS |
| Tests | JUnit 5 + Mockito (services métier), H2 pour le contexte |

---

## Démarrage

### Prérequis
- **JDK 17+** (le projet cible Java 26, mais compile dès 17)
- **Node.js 20+** et npm
- (optionnel) **MySQL 8** — inutile en profil `dev`

### 1. Backend

**Option A — profil `dev` (H2 en mémoire, aucune installation, recommandé pour tester) :**
```bash
cd BuBackend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```
Console H2 : http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:bibliotheque_db`, user `sa`).

**Option B — MySQL (conforme au sujet du TP) :**
```sql
CREATE DATABASE bibliotheque_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'biblio_user'@'localhost' IDENTIFIED BY 'adriano';
GRANT ALL PRIVILEGES ON bibliotheque_db.* TO 'biblio_user'@'localhost';
FLUSH PRIVILEGES;
```
Ajustez au besoin les identifiants dans `BuBackend/src/main/resources/application.properties`, puis :
```bash
cd BuBackend
./mvnw spring-boot:run
```

Au premier démarrage, un jeu de **données de démonstration** est inséré automatiquement
(livres multi-auteurs, un emprunt déjà en retard, un livre épuisé avec sa file d'attente).

### 2. Frontend
```bash
cd BuFrontend
npm install
npm start
```
Application : http://localhost:4200 (le proxy `/api` redirige vers le backend `:8080`).

---

## Comptes de démonstration

| Rôle | Email | Mot de passe |
|---|---|---|
| Admin | `admin@universite.sn` | `Admin2024!` |
| Bibliothécaire | `biblio@universite.sn` | `Biblio2024!` |
| Étudiant | `etudiant@universite.sn` | `Etudiant2024!` |

(Deux autres étudiants : `fatou@universite.sn`, `cheikh@universite.sn` — même mot de passe.)

---

## Fonctionnalités par rôle

**Étudiant** — parcourir le catalogue, emprunter, **réserver un livre épuisé** (avec position
dans la file), confirmer une réservation devenue disponible, prolonger un emprunt, consulter
ses pénalités, recevoir des notifications.

**Bibliothécaire / Admin** — tout ce qui précède, plus : CRUD livres (**multi-auteurs**) et
auteurs, enregistrement des **retours** (avec pénalité automatique et promotion de la file
d'attente), gestion des pénalités, **tableau de bord statistiques**.

### Règles métier notables
- Emprunt refusé si stock épuisé, pénalité impayée, doublon, ou limite de 3 emprunts atteinte.
- Au **retour en retard** : pénalité = `jours de retard × 100 FCFA` (configurable) + notification.
- **File d'attente** : au retour d'un exemplaire, la 1re réservation en attente passe
  `DISPONIBLE` (exemplaire mis de côté 48 h) et l'utilisateur est notifié ; sinon le stock est
  réincrémenté.
- **Tâche planifiée** quotidienne (déclenchable manuellement via *« Lancer les traitements »*
  sur le dashboard) : notifie les échéances proches, passe les emprunts en retard, expire les
  réservations non confirmées.

---

## Principaux endpoints de l'API

| Méthode | URL | Accès |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/livres`, `/api/livres/{id}`, `/api/livres/recherche?q=` | public |
| POST/PUT/DELETE | `/api/livres`, `/api/auteurs` | bibliothécaire/admin |
| POST | `/api/emprunts` · PATCH `/api/emprunts/{id}/prolonger` | authentifié |
| PATCH | `/api/emprunts/{id}/retour` · GET `/api/emprunts/retards` | bibliothécaire/admin |
| POST | `/api/reservations` · POST `/api/reservations/{id}/confirmer` | authentifié |
| GET | `/api/penalites/mes-penalites` · PATCH `/api/penalites/{id}/payer` | selon rôle |
| GET | `/api/notifications/mes-notifications`, `/non-lues/count` | authentifié |
| GET | `/api/stats/dashboard` · POST `/api/stats/traitements` | bibliothécaire/admin |

Toutes les routes protégées attendent l'en-tête `Authorization: Bearer <token>`.

---

## Tests

```bash
cd BuBackend
./mvnw test
```
Tests unitaires (Mockito) ciblés sur le cœur métier : décrément de stock, éligibilité,
pénalité proportionnelle au retard, file d'attente FIFO, confirmation et expiration de
réservation.

---

## Couverture des critères d'évaluation du TP

- **IoC / DI** : injection par constructeur systématique dans les services.
- **JPA** : 7 entités, relations `@ManyToOne`/`@OneToMany`/**`@ManyToMany`**, requêtes dérivées + JPQL.
- **REST** : contrôleurs REST, codes HTTP corrects (201/204/400/401/403/404/409), **DTOs** en réponse.
- **Sécurité JWT** : authentification stateless, autorisation par rôle (`@PreAuthorize` + règles globales), mots de passe **BCrypt**.
- **Validation** : `@Valid` + contraintes (`@NotBlank`, `@Email`, `@Min`…) sur les DTOs d'entrée.
- **Gestion d'erreurs** : `@RestControllerAdvice` global.
- **Frontend Angular** complet consommant l'API.
