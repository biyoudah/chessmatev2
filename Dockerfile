# Étape 1 : Build de l'application
FROM maven:3.8.4-openjdk-17 AS build
COPY . /app
WORKDIR /app
# On construit le JAR en ignorant les tests pour accélérer le build sur Render
RUN mvn clean package -DskipTests

# Étape 2 : Exécution (C'est ici qu'on change l'image)
# Utilisez 'eclipse-temurin' au lieu de 'openjdk'
FROM eclipse-temurin:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
