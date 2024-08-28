ARG BASE_IMAGE=084690408984.dkr.ecr.us-east-1.amazonaws.com/openjdk-21:v199.0

FROM ${BASE_IMAGE}

COPY target/ad-allocation-diagnosis-writer.jar /usr/app/ad-allocation-diagnosis-writer.jar

WORKDIR /usr/app

ENTRYPOINT exec java -Dspring.profiles.active=$PROFILE $JVM_OPTS -jar ad-allocation-diagnosis-writer.jar