ARG BASE_IMAGE=084690408984.dkr.ecr.us-east-1.amazonaws.com/openjdk-8:v58.0
FROM ${BASE_IMAGE}

ARG APP_NAME=ad-reach-synchronizer

COPY target/${APP_NAME}.jar /usr/app/app.jar
WORKDIR /usr/app

ENTRYPOINT exec java -Dspring.profiles.active=$PROFILE $JVM_OPTS -jar app.jar
