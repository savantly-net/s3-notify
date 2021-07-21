FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/*-runner.jar /app.jar
COPY entrypoint.sh /app/

RUN chmod -R 755 /app

ENTRYPOINT [ "/app/entrypoint.sh" ]