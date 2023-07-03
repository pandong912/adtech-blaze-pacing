export PROFILE="prod"
export JVM_OPTS="-Xms6144m -Xmx6144m -XX:+UseG1GC"
export ADS_ENV_NAMESPACE_NAME="adtech-in-prod-apse1"
export ADS_ENV_SERVICE_HOSTNAME="adtech-blaze-data-exchanger-apse1.hotstar-labs.com"
export ADS_ENV_RESOURCES_LIMIT_MEMORY="8192Mi"
export ADS_ENV_RESOURCES_LIMIT_CPU=4
export ADS_ENV_REQUESTS_LIMIT_MEMORY="8192Mi"
export ADS_ENV_REQUESTS_LIMIT_CPU=4
export ADS_ENV_STAGE="PROD"
export ADS_ENV_HPA_MIN_PODS=2
export ADS_ENV_HPA_MAX_PODS=4
export ACCESS_LOGS_BUCKET_NAME="all-elb-logs-prod-sgp"

