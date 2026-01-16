# --- ÉTAPE 1 : BUILD ---
# On utilise une version de Maven compatible avec Java 21 (Eclipse Temurin)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Compilation du JAR (en ignorant les tests pour Render)
RUN mvn clean package -DskipTests

# --- ÉTAPE 2 : RUNTIME ---
# On utilise l'image JRE légère recommandée pour la production
FROM eclipse-temurin:21-jre-slim
WORKDIR /app
# Copie du fichier JAR généré à l'étape précédente
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
