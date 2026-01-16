# --- ÉTAPE 1 : BUILD (Traduction de ton stage 'build' GitLab) ---
# On utilise la même image que dans ton .yml
FROM maven:3.8.6-openjdk-21 AS build

# Définition du répertoire de travail
WORKDIR /app

# Copie de l'intégralité du projet (Code source + pom.xml)
COPY . .

# Exécution de la commande de build (on ignore les tests pour accélérer le déploiement sur Render)
RUN mvn clean package -DskipTests

# --- ÉTAPE 2 : RUN (Traduction de ton stage 'deploy') ---
# On utilise une image plus légère pour l'exécution
FROM eclipse-temurin:21-jre-slim

WORKDIR /app

# On récupère uniquement le JAR généré à l'étape précédente (ton artifact)
COPY --from=build /app/target/*.jar app.jar

# Port utilisé par ton application Spring Boot
EXPOSE 8080

# Commande de lancement
ENTRYPOINT ["java", "-jar", "app.jar"]
