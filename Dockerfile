# Multi-stage Dockerfile para DistriSchool Microservice Template
# Suporta desenvolvimento com hot reloading e produção otimizada

# Stage 1: Resolver dependências (cache layer)
FROM maven:3.9-eclipse-temurin-17 AS deps

WORKDIR /app
COPY pom.xml /app

# Resolve dependências Maven
RUN mvn dependency:resolve

# Stage 2: Build da aplicação
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY --from=deps /root/.m2/repository /root/.m2/repository
COPY . /app

RUN mvn package -DskipTests

# Stage 3: Desenvolvimento com hot reloading
FROM maven:3.9-eclipse-temurin-17 AS dev

WORKDIR /app
COPY --from=deps /root/.m2/repository /root/.m2/repository
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

# Instala inotify-tools para monitorar mudanças nos arquivos
RUN apt-get update && apt-get install -y inotify-tools
RUN chmod +x /docker-entrypoint.sh

# Configurações para hot reload
ENV SPRING_DEVTOOLS_RESTART_ENABLED=true
ENV SPRING_DEVTOOLS_LIVERELOAD_ENABLED=true
ENV SPRING_DEVTOOLS_RESTART_POLL_INTERVAL=1000
ENV SPRING_DEVTOOLS_RESTART_QUIET_PERIOD=400

EXPOSE 8080 5005 35729

ENTRYPOINT ["/docker-entrypoint.sh"]

# Stage 4: Produção otimizada
FROM eclipse-temurin:17-jdk AS release

LABEL maintainer="DistriSchool Team"
WORKDIR /app

# Copia o JAR construído
COPY --from=build /app/target/microservice-template-1.0.0.jar /app/app.jar

# Cria usuário não-root para segurança
RUN groupadd --system app && useradd --system --shell /bin/false --gid app app
RUN chown -R app:app /app

USER app

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]