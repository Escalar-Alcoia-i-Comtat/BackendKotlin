# Stage 1: Cache Gradle dependencies
FROM gradle:8-alpine AS cache

ARG SENTRY_DSN_TESTS
ENV SENTRY_DSN_TESTS=$SENTRY_DSN_TESTS
ARG SENTRY_AUTH_TOKEN
ENV SENTRY_AUTH_TOKEN=$SENTRY_AUTH_TOKEN
ARG SENTRY_DISABLE
ENV SENTRY_DISABLE=$SENTRY_DISABLE

RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home

COPY build.gradle.kts gradle.properties settings.gradle.kts /home/gradle/app/

RUN mkdir -p /home/gradle/app/gradle
COPY gradle/libs.versions.toml /home/gradle/app/gradle/

RUN mkdir -p /home/gradle/app/package
COPY package/version.txt /home/gradle/app/package/

WORKDIR /home/gradle/app
RUN gradle clean build -i --stacktrace

# Stage 2: Build Application
FROM gradle:8-alpine AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/app/
WORKDIR /usr/src/app
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the fat JAR, Gradle also supports shadow
# and boot JAR by default.
RUN gradle buildFatJar --no-daemon

# Stage 3: Create the Runtime Image
FROM amazoncorretto:22-alpine AS runtime
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/escalaralcoiaicomtat.jar
ENTRYPOINT ["java","-jar","/app/escalaralcoiaicomtat.jar"]
