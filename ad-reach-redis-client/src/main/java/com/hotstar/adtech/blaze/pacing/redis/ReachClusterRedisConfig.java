package com.hotstar.adtech.blaze.pacing.redis;

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
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
public class ReachClusterRedisConfig {
  private static final String REACH_REDIS = "Reach";
  private static final String REACH_REDIS_PROPERTIES = REACH_REDIS + PROPERTIES;
  private static final String REACH_REDIS_CONNECTION_FACTORY = REACH_REDIS + CONNECTION_FACTORY;
  public static final String REACH_REDIS_TEMPLATE = REACH_REDIS + TEMPLATE;

  @Bean(REACH_REDIS_PROPERTIES)
  @ConfigurationProperties(prefix = "blaze.redis.reach-cluster")
  public ClusterRedisConfig clusterConfig() {
    return new ClusterRedisConfig();
  }

  @Bean(REACH_REDIS_CONNECTION_FACTORY)
  public LettuceConnectionFactory lettuceConnectionFactory(ClientResources clientResources,
                                                           @Qualifier(REACH_REDIS_PROPERTIES)
                                                           ClusterRedisConfig redisClusterProperties) {
    return RedisFactory.initLettuceConnectionFactory(clientResources, redisClusterProperties);
  }

  @Bean(REACH_REDIS_TEMPLATE)
  public RedisTemplate<String, Object> redisTemplate(
    @Qualifier(REACH_REDIS_CONNECTION_FACTORY) RedisConnectionFactory redisConnectionFactory) {
    return RedisFactory.createRedisTemplate(redisConnectionFactory);
  }
}
