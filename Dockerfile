FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && \
    apt-get install -y wget netcat mysql-client redis-tools curl && \
    wget -O /usr/local/bin/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && \
    chmod +x /usr/local/bin/wait-for-it.sh

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY wait-for-mysql.sh /usr/local/bin/wait-for-mysql.sh

# 실행 권한 + CRLF 제거 (중요)
RUN chmod +x /usr/local/bin/wait-for-mysql.sh \
    && sed -i 's/\r$//' /usr/local/bin/wait-for-mysql.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/wait-for-mysql.sh"]