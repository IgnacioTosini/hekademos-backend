# Usar Eclipse Temurin (reemplazo oficial de openjdk)
FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="IgnacioTosini"

# Crear usuario no-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copiar archivos necesarios para cachear dependencias
COPY mvnw ./
COPY mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Dar permisos al wrapper
RUN chmod +x ./mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación
RUN ./mvnw clean package -DskipTests

# Cambiar a usuario seguro
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=production

CMD ["sh", "-c", "java $JAVA_OPTS -jar target/hekademos-backend-*.jar"]
