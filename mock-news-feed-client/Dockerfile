# Build stage
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -pl mock-news-feed-client -am

# Runtime stage
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/mock-news-feed-client/target/mock-news-feed-client-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
COPY mock-news-feed-client/docker-entrypoint.sh .

RUN chmod +x docker-entrypoint.sh
ENTRYPOINT ["./docker-entrypoint.sh"]