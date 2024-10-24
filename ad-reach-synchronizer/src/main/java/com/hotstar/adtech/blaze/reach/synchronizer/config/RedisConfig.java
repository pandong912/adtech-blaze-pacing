package com.hotstar.adtech.blaze.reach.synchronizer.config;

import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.CONNECTION_FACTORY;
import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.PROPERTIES;
import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.TEMPLATE;

import com.hotstar.adtech.blaze.adserver.data.redis.RedisFactory;
import com.hotstar.adtech.blaze.adserver.data.redis.model.ClusterRedisConfig;
import io.lettuce.core.resource.ClientResources;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
  private static final String DECISION_REDIS = "Decision";
  private static final String DECISION_REDIS_PROPERTIES = DECISION_REDIS + PROPERTIES;
  private static final String DECISION_REDIS_CONNECTION_FACTORY = DECISION_REDIS + CONNECTION_FACTORY;
  public static final String DECISION_REDIS_TEMPLATE = DECISION_REDIS + TEMPLATE;

  @Bean(DECISION_REDIS_PROPERTIES)
  @ConfigurationProperties(prefix = "blaze.redis.decision-cluster")
  public ClusterRedisConfig clusterConfig() {
    return new ClusterRedisConfig();
  }

  @Bean(DECISION_REDIS_CONNECTION_FACTORY)
  public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources,
                                                           @Qualifier(DECISION_REDIS_PROPERTIES)
                                                           ClusterRedisConfig redisClusterProperties) {
    return RedisFactory.initLettuceConnectionFactory(clientResources, redisClusterProperties);
  }

  @Bean(DECISION_REDIS_TEMPLATE)
  public RedisTemplate<String, Object> redisTemplate(
    @Qualifier(DECISION_REDIS_CONNECTION_FACTORY) RedisConnectionFactory redisConnectionFactory) {
    return RedisFactory.createRedisTemplate(redisConnectionFactory);
  }

}
