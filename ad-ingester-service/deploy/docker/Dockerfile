ARG BASE_IMAGE=084690408984.dkr.ecr.us-east-1.amazonaws.com/openjdk-21:v199.0

FROM ${BASE_IMAGE}

COPY target/ad-ingester-service.jar /usr/app/ad-ingester-service.jar

WORKDIR /usr/app

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/usr/bin/java -Dspring.profiles.active=$PROFILE $JVM_OPTS  -jar ad-ingester-service.jar"]
