#--------------------
# Fase de build
# --------------------
# FROM eclipse-temurin:21-jdk AS build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

# RUN chmod +x mvnw
# RUN ./mvnw clean package -DskipTests

# ---------------------
# Fase de ejecucion
# ---------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar api-docs.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-docs.jar"]