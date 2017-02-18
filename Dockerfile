FROM java:openjdk-8-jre-alpine

ARG VERSION="1.0.0-SNAPSHOT"

LABEL name="graphiak" version=$VERSION

ENV PORT 2003

RUN apk add --no-cache curl openjdk8="$JAVA_ALPINE_VERSION"

WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn ./.mvn/

RUN ./mvnw install

COPY . .

RUN ./mvnw clean package -DskipTests=true -Dmaven.javadoc.skip=true -Dmaven.source.skip=true && \
    rm target/original-*.jar && \
    mv target/*.jar app.jar && \
    rm -rf /root/.m2 && \
    rm -rf target && \
    apk del openjdk8

EXPOSE 2003 8080

HEALTHCHECK --interval=10s --timeout=5s CMD curl -f http://127.0.0.1:8080/admin/healthcheck || exit 1

ENTRYPOINT ["java", "-d64", "-server", "-jar", "app.jar"]
CMD ["server", "config.yml"]
