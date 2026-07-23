# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------
# Stage 1: build
# Compiles the app inside the container, so a working local Maven/JDK
# install is never required to produce a deployable image - only Docker.
# ---------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy the POM first and download dependencies into a separate layer, so
# `docker build` only re-downloads dependencies when pom.xml actually
# changes, not on every source edit.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------
# Stage 2: runtime
# A minimal JRE (not a full JDK) image - smaller attack surface and image
# size than shipping the build toolchain into production.
# ---------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

# Run as a non-root user rather than the image default root.
RUN addgroup -S gemblog && adduser -S gemblog -G gemblog
USER gemblog

WORKDIR /app
COPY --from=build /build/target/gemblog-pro.jar app.jar

EXPOSE 8080

# Used by `docker run --health-cmd` implicitly and by orchestrators
# (Docker Compose, Render, Kubernetes) that read the image's own
# HEALTHCHECK. Hits the Actuator endpoint added in Phase 5.
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
