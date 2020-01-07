FROM java:8

WORKDIR /usr/src/app

ADD tinyurl-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080

ARG BUILD_DATE
ARG BUILD_VERSION
ARG COMMIT

ENTRYPOINT ["java","-jar","./app.jar","--port=8080"]
