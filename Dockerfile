FROM gradle:8-jdk17 AS BUILD

WORKDIR /home/gradle

COPY . .

RUN gradle bootJar --no-daemon && \
    java -Djarmode=layertools -jar ./build/libs/app.jar extract

FROM eclipse-temurin:17-jre-alpine AS DONE

WORKDIR /app

COPY --from=BUILD /home/gradle/dependencies/ ./
COPY --from=BUILD /home/gradle/spring-boot-loader/ ./
COPY --from=BUILD /home/gradle/snapshot-dependencies/ ./
COPY --from=BUILD /home/gradle/application/ ./

RUN { \
    echo '#!/bin/sh'; \
    echo 'set -e'; \
    echo '# Запуск приложения с помощью Spring Boot JarLauncher'; \
    echo 'exec java org.springframework.boot.loader.launch.JarLauncher'; \
} > /runjava.sh && chmod +x /runjava.sh

RUN adduser -S spring-user && chown -R spring-user /app
USER spring-user

EXPOSE 8080

ENTRYPOINT ["/bin/sh", "/runjava.sh"]
