FROM eclipse-temurin:17-alpine as builder

COPY . .

RUN ./gradlew build -x test

FROM eclipse-temurin:17-alpine

COPY --from=builder ./build/libs/user-*-all.jar /user.jar
EXPOSE 6450

ENTRYPOINT ["java", "-jar", "user.jar"]
