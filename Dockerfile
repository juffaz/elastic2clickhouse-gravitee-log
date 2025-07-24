FROM clojure:openjdk-11-lein

WORKDIR /app
COPY project.clj .
RUN lein deps

COPY src src
RUN lein uberjar

CMD ["java", "-jar", "target/uberjar/gravitee-sync-0.1.0-SNAPSHOT-standalone.jar"]
