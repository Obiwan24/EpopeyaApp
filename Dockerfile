# ============================================================================
# Dockerfile para EpopeyaApp
# ----------------------------------------------------------------------------
# Render (y la mayoría de plataformas de despliegue) no soportan Java de
# forma nativa, así que en vez de un "build command" / "start command" en su
# interfaz, se les da este Dockerfile y ellos se encargan de construir y
# arrancar la imagen automáticamente.
#
# Es un build "multi-stage" (2 fases) para que la imagen final sea pequeña:
#   1) FASE "build": usa una imagen con Maven + JDK 17 para compilar el
#      proyecto y generar el .jar (esto pesa mucho, pero no viaja a producción).
#   2) FASE final: usa una imagen mínima solo con el JRE (Java para EJECUTAR,
#      no para compilar) y copia dentro SOLO el .jar ya compilado.
# ============================================================================

# ---------- FASE 1: compilar el proyecto con Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Se copia primero solo el pom.xml para aprovechar la caché de Docker: si no
# cambias las dependencias, en el siguiente build no las vuelve a descargar.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora sí, se copia el código fuente y se compila el .jar
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- FASE 2: imagen final, ligera, solo para ejecutar ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el .jar generado en la fase anterior. El nombre debe coincidir
# con <artifactId>-<version>.jar de tu pom.xml (EpopeyaApp-0.0.1-SNAPSHOT.jar).
# Si algún día subes de versión, actualiza este nombre.
COPY --from=build /app/target/EpopeyaApp-0.0.1-SNAPSHOT.jar app.jar

# Render (y la mayoría de plataformas) inyectan la variable PORT en tiempo de
# ejecución y esperan que tu app escuche en ese puerto. Spring Boot ya lee
# la variable de entorno PORT automáticamente si la mapeamos a server.port.
ENV SERVER_PORT=${PORT:-8080}
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]