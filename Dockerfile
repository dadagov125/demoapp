FROM openjdk:8
COPY ./ /home/src/
WORKDIR /home/src
RUN ./gradlew -Dspring.profiles.active=prod -Pprod -Pswagger build --refresh-dependencies -x test
WORKDIR /home/src/build/libs
EXPOSE ${SERVER_PORT:-8080}
ENTRYPOINT ["java", "-jar", "app.jar", "-Dserver.port=${SERVER_PORT:-8080}"]
