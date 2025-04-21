FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /application
COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew bootJar --no-daemon

RUN java -Djarmode=tools -jar build/libs/app.jar extract --layers --launcher

FROM eclipse-temurin:17-jre-alpine AS run

WORKDIR /application
RUN adduser -S spring-user
USER spring-user
EXPOSE 8080

COPY --from=builder /application/app/spring-boot-loader/ ./
COPY --from=builder /application/app/dependencies/ ./
COPY --from=builder /application/app/snapshot-dependencies/ ./
COPY --from=builder /application/app/application/ ./

ENV JAVA_OPTS="-Duser.timezone=Europe/Moscow"
ENV JAVA_ARGS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher $JAVA_ARGS"]
