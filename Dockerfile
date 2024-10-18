FROM amazoncorretto:17-alpine AS build
COPY . /usr/src/app/
WORKDIR /usr/src/app
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the fat JAR, Gradle also supports shadow
# and boot JAR by default.
RUN ./gradlew buildFatJar --no-daemon

FROM amazoncorretto:17-alpine AS runtime
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/escalaralcoiaicomtat.jar
ENTRYPOINT ["java","-jar","/app/escalaralcoiaicomtat.jar"]
