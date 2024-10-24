#
# Build stage
#
FROM alpine:latest AS build
RUN apk add openjdk17 maven
WORKDIR /app
COPY src /app/src
COPY pom.xml /app
RUN mvn -f pom.xml clean package

#
# Package stage
#
FROM alpine:latest
RUN apk add openjdk17
WORKDIR /app
COPY --from=build /app/target/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]