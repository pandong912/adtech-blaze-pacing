ARG BASE_IMAGE=084690408984.dkr.ecr.us-east-1.amazonaws.com/openjdk-8:v58.0
FROM ${BASE_IMAGE}

COPY target/ad-data-exchanger.jar /usr/app/ad-data-exchanger.jar

WORKDIR /usr/app

ENTRYPOINT exec java -Dspring.profiles.active=$PROFILE $JVM_OPTS -jar ad-data-exchanger.jar