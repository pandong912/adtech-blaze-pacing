package com.hotstar.adtech.blaze.pacing.redis;

import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.CONNECTION_FACTORY;
import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.PROPERTIES;
import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.TEMPLATE;

import com.hotstar.adtech.blaze.adserver.data.redis.RedisFactory;
import com.hotstar.adtech.blaze.adserver.data.redis.model.ClusterConfig;
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
  private static final String REACH = "reach";
  private static final String REACH_PROPERTIES = REACH + PROPERTIES;
  private static final String REACH_CONNECTION_FACTORY = REACH + CONNECTION_FACTORY;
  public static final String REACH_TEMPLATE = REACH + TEMPLATE;

  @Bean(REACH_PROPERTIES)
  @ConfigurationProperties(prefix = "blaze.redis.reach-cluster")
  public ClusterConfig reachClusterConfig() {
    return new ClusterConfig();
  }

  @Bean(REACH_CONNECTION_FACTORY)
  public LettuceConnectionFactory clusterRedisConnectionFactory(ClientResources clientResources,
                                                                @Qualifier(REACH_PROPERTIES)
                                                                ClusterConfig redisClusterProperties) {
    return RedisFactory.initLettuceClusterConnectionFactory(clientResources, redisClusterProperties);
  }

  @Bean(REACH_TEMPLATE)
  public RedisTemplate<String, Object> clusterRedisTemplate(
    @Qualifier(REACH_CONNECTION_FACTORY) RedisConnectionFactory redisConnectionFactory) {
    return RedisFactory.createTemplate(redisConnectionFactory);
  }
}
