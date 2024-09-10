ARG BASE_IMAGE=084690408984.dkr.ecr.us-east-1.amazonaws.com/adoptium-openjdk-21:2024.09.12-16.31.03-fa24b64
FROM ${BASE_IMAGE}
USER root

COPY target/ad-ingester-service.jar /usr/app/ad-ingester-service.jar

WORKDIR /usr/app

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/usr/bin/java -Dspring.profiles.active=$PROFILE $JVM_OPTS  -jar ad-ingester-service.jar"]
