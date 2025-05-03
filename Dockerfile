FROM eclipse-temurin:17-jdk-alpine

WORKDIR /application

COPY build/libs/app.jar app.jar

RUN adduser -S spring-user
USER spring-user

EXPOSE 8080

ENV JAVA_OPTS="-Duser.timezone=Europe/Moscow"
ENV JAVA_ARGS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $JAVA_ARGS"]
