FROM gradle:7.6.1-jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=$PORT -Dserver.address=0.0.0.0 -jar app.jar"]

