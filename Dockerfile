FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && \
    apt-get install -y wget && \
    wget -O /usr/local/bin/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && \
    chmod +x /usr/local/bin/wait-for-it.sh

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/wait-for-it.sh", "redis:6379", "--timeout=30", "--", "java", "-jar", "app.jar"]