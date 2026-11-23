# Étape 1 : compiler l'application avec Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Étape 2 : l'image finale, avec seulement Java et le jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 booking
COPY --from=build /app/target/resource-booking-api.jar app.jar
USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]