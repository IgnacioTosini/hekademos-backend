# Usar una imagen base de OpenJDK 17 con Alpine para menor tamaño
FROM openjdk:17-jdk-alpine

# Información del mantenedor
LABEL maintainer="IgnacioTosini"

# Crear un usuario no-root para mayor seguridad
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar los archivos de Maven wrapper y pom.xml primero (para aprovechar cache de Docker)
COPY mvnw ./
COPY mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Descargar dependencias (esto se cachea si no cambia el pom.xml)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Cambiar al usuario no-root
USER appuser

# Exponer el puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=production

# Comando para ejecutar la aplicación
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/hekademos-backend-*.jar"]