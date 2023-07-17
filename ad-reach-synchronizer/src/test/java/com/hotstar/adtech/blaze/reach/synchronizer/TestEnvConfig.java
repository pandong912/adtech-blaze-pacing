package com.hotstar.adtech.blaze.reach.synchronizer;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;


@Slf4j
@ActiveProfiles("local")
public abstract class TestEnvConfig {
  private static final int REDIS_CLUSTER_PORT = 6379;
  static GenericContainer<?> redis;
  static DockerComposeContainer<?> redisCluster;

  static {
    startRedis();
    startRedisCluster();
  }

  private static void startRedis() {
    redis = new GenericContainer<>("redis:7")
      .withExposedPorts(6379);
    redis.start();
  }

  private static void startRedisCluster() {
    redisCluster =
      new DockerComposeContainer(new File("src/test/resources/docker-compose-test.yml"))
        .withExposedService("redis-single-node-cluster", REDIS_CLUSTER_PORT);
    redisCluster.start();

  }

  @DynamicPropertySource
  static void datasourceConfig(DynamicPropertyRegistry registry) {
    registry.add("blaze.redis.master-replica.master.port", () -> redis.getMappedPort(6379).toString());
    registry.add("blaze.redis.master-replica.replicas.port", () -> redis.getMappedPort(6379).toString());
  }
}

