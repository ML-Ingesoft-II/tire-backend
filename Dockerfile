# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Create a non-root user
RUN addgroup -S tire && adduser -S tire -G tire

COPY --from=build /app/target/*.jar app.jar
RUN chown tire:tire app.jar

USER tire
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
