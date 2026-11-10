# --------------------
# Fase de build
# --------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 1. Copiar primero SOLO los archivos de configuración y el wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# 2. Dar permisos y descargar dependencias (Esto se guardará en la caché de Docker)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# 3. Copiar el código fuente del proyecto
COPY src ./src

# 4. Compilar el proyecto (Usará las dependencias ya descargadas en el paso 2)
RUN ./mvnw clean package -DskipTests

# ---------------------
# Fase de ejecucion
# ---------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar api-docs.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-docs.jar"]
