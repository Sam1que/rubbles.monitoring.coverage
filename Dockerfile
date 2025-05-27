FROM maven:3.6.3-openjdk-17 as MAVEN_BUILD

COPY ./ ./

RUN mvn clean package

FROM openjdk:17.0.2-jdk-slim

ARG JAR_FILE=target/*.jar

COPY --from=MAVEN_BUILD ${JAR_FILE} /adapter.jar

ENTRYPOINT ["java", "-jar", "/adapter.jar"]
CMD ["-action", "run"]