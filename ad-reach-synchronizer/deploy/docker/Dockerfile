ARG BASE_IMAGE=084690408984.dkr.ecr.us-east-1.amazonaws.com/adoptium-openjdk-21:2024.09.12-16.31.03-fa24b64
FROM ${BASE_IMAGE}
USER root

ARG APP_NAME=ad-reach-synchronizer

COPY target/${APP_NAME}.jar /usr/app/app.jar
WORKDIR /usr/app

ENTRYPOINT exec java -Dspring.profiles.active=$PROFILE $JVM_OPTS -jar app.jar
