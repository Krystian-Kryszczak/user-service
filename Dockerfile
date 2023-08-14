FROM eclipse-temurin:17-alpine

COPY ./layers/application.jar .
EXPOSE 6450

ENTRYPOINT ["java", "-jar", "application.jar"]
