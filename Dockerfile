# Built by .github/workflows/deploy.yml (context ., file Dockerfile) and pushed
# to Artifact Registry.
#
# A job image, not a server: the default command runs the Hibernate job
# (world.qode.app.Main) and exits 0 on success. It will never satisfy a $PORT
# health check. Two stages: Maven builds and tests target/app.jar plus its
# runtime jars in target/lib, a JRE-only image runs it as a non-root user.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package

FROM eclipse-temurin:21-jre AS runtime
ARG BUILD_ID=""
WORKDIR /app
ENV BUILD_ID=$BUILD_ID
RUN useradd -r -u 10001 app
COPY --from=build /src/target/lib /app/lib
COPY --from=build /src/target/app.jar /app/app.jar
USER app
CMD ["java", "-jar", "/app/app.jar"]
