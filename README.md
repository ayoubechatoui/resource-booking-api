\# resource-booking-api



Une API de réservation de machines (serveurs GPU, VM de test) pour une équipe.





\## Pourquoi cette API



Dans une équipe qui partage quelques machines chères, les réservations se font souvent par message et deux personnes finissent par utiliser la même machine en même temps. L'idée est de centraliser les réservations et d'interdire les chevauchements.



\## Règles que l'API doit respecter



1\. Il faut être connecté pour tout faire, sauf s'inscrire et se connecter.

2\. Une inscription crée toujours un utilisateur simple, jamais un admin.

3\. Une réservation doit se terminer après son début.

4\. Deux réservations en attente ou validées ne peuvent pas se chevaucher sur la même machine.

5\. Seul un admin peut valider, refuser, tout lister ou ajouter une machine.

6\. On ne peut valider ou refuser qu'une réservation en attente.

7\. Un utilisateur ne peut annuler que ses propres réservations.



\## Routes prévues



&#x20;   POST   /api/auth/register

&#x20;   POST   /api/auth/login

&#x20;   GET    /api/resources

&#x20;   POST   /api/resources                    (admin)

&#x20;   POST   /api/reservations

&#x20;   GET    /api/reservations/mine

&#x20;   GET    /api/reservations                 (admin)

&#x20;   PATCH  /api/reservations/{id}/validate   (admin)

&#x20;   PATCH  /api/reservations/{id}/refuse     (admin)

&#x20;   DELETE /api/reservations/{id}



\## Technos



Java 21, Spring Boot, Spring Security (JWT), PostgreSQL, Flyway, Docker. Déploiement prévu sur Kubernetes, sur AWS.


## Lancer le projet

Le plus simple est Docker : il suffit d'avoir Docker Desktop, pas besoin de Java ni de Maven.

Je crée d'abord un fichier `.env` à la racine (il est ignoré par Git) :

```
JWT_SECRET=une-cle-d-au-moins-32-caracteres
ADMIN_PASSWORD=un-mot-de-passe
```

Puis je lance la base et l'API :

```
docker compose up -d --build
```

L'API répond sur http://localhost:8080 et la documentation Swagger est sur
http://localhost:8080/swagger-ui/index.html. Le compte admin est `admin@booking.local`
avec le mot de passe mis dans `.env`.

Pour développer sans reconstruire l'image à chaque fois, je lance seulement la base
avec `docker compose up -d db`, puis l'API avec Maven et le profil `local`
(le fichier `src/main/resources/application-local.yml` contient les mêmes secrets) :

```
$env:SPRING_PROFILES_ACTIVE = "local"
mvn spring-boot:run
```

Les tests n'ont besoin de rien d'autre : `mvn test` utilise une base H2 en mémoire.
