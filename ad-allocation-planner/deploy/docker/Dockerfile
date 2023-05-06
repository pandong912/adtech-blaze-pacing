ARG BASE_IMAGE=084690408984.dkr.ecr.us-east-1.amazonaws.com/openjdk-8:v58.0
FROM ${BASE_IMAGE}

COPY target/ad-allocation-planner.jar /usr/app/ad-allocation-planner.jar

WORKDIR /usr/app

ENTRYPOINT exec java -Dspring.profiles.active=$PROFILE $JVM_OPTS -jar ad-allocation-planner.jar
