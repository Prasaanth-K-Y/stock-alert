FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/universal/stage/ /app

ENV PLAY_SECRET="replace-this-with-a-very-long-and-random-secret-key-!@#$"
ENV DB_HOST=mysql
ENV DB_NAME=testdb
ENV DB_USER=stockuser
ENV DB_PASSWORD=stockpass

EXPOSE 9000

CMD ["./bin/stock-alert-app", "-Dplay.http.secret.key=${PLAY_SECRET}", "-Dconfig.file=conf/application.conf", "-Dhttp.port=9000"]
