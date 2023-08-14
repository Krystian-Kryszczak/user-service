FROM eclipse-temurin:17-alpine

COPY ./layers/ .
EXPOSE 6450

ENTRYPOINT ["java", "-jar", "application.jar"]
