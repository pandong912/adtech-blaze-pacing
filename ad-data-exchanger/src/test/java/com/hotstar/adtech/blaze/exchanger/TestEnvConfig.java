package com.hotstar.adtech.blaze.exchanger;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


@Slf4j
@Transactional
public abstract class TestEnvConfig {
  private static final int REDIS_CLUSTER_PORT = 6379;
  static PostgreSQLContainer<?> postgreSQLContainer;
  static GenericContainer<?> redis;
  static DockerComposeContainer<?> redisCluster;

  static {
    System.setProperty("AD_MODEL_DB_USERNAME", "default");
    System.setProperty("AD_MODEL_DB_PASSWORD", "");

    copyDbMigrationFile();

    startPostgres();

    startRedis();

    startRedisCluster();
  }

  private static void copyDbMigrationFile() {
    try {
      Runtime.getRuntime()
        .exec("mkdir -p target/test-classes/db/");
      Runtime.getRuntime()
        .exec("cp -R -f ../ad-model-db-migration/src/main/resources/db/migration/ target/test-classes/db/migration/");
      Runtime.getRuntime()
        .exec("cp -R -f ../ad-model-db-migration/src/main/resources/db/data/ target/test-classes/db/data/");
    } catch (Exception e) {
      throw new ServiceException("copy db migration file fail", e);
    }
  }

  private static void startRedisCluster() {
    redisCluster =
      new DockerComposeContainer(new File("src/test/resources/docker-compose-test.yml"))
        .withExposedService("redis-single-node-cluster", REDIS_CLUSTER_PORT);
    redisCluster.start();
  }

  private static void startRedis() {
    redis = new GenericContainer<>("redis:7")
      .withExposedPorts(6379);
    redis.start();
  }

  private static void startPostgres() {
    postgreSQLContainer =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:10"))
        .withDatabaseName("ad_model_test")
        .withUsername("ads_test")
        .withPassword("ads_test");
    postgreSQLContainer.start();
  }

  @DynamicPropertySource
  static void datasourceConfig(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("blaze.redis.common.master.port", () -> redis.getMappedPort(6379).toString());
    registry.add("blaze.redis.common.replica.port", () -> redis.getMappedPort(6379).toString());
    registry.add("blaze.redis.cluster.nodes", () -> "localhost:" + REDIS_CLUSTER_PORT);
    registry.add("blaze.redis.beacon.nodes", () -> "localhost:" + REDIS_CLUSTER_PORT);
    registry.add("blaze.redis.reach.nodes", () -> "localhost:" + REDIS_CLUSTER_PORT);
  }
}

