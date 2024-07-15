package com.hotstar.adtech.blaze.reach.synchronizer.config;

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
public class DecisionReachClusterRedisConfig {
  private static final String DECISION_REACH_CLUSTER = "decisionReachCluster";
  private static final String DECISION_REACH_CLUSTER_CONNECTION_FACTORY = DECISION_REACH_CLUSTER + CONNECTION_FACTORY;
  public static final String DECISION_REACH_CLUSTER_PROPERTIES = DECISION_REACH_CLUSTER + PROPERTIES;
  public static final String DECISION_REACH_CLUSTER_TEMPLATE = DECISION_REACH_CLUSTER + TEMPLATE;

  @Bean(DECISION_REACH_CLUSTER_PROPERTIES)
  @ConfigurationProperties(prefix = "blaze.redis.decision-cluster")
  public ClusterConfig reachClusterConfig() {
    return new ClusterConfig();
  }

  @Bean(DECISION_REACH_CLUSTER_CONNECTION_FACTORY)
  public LettuceConnectionFactory clusterRedisConnectionFactory(ClientResources clientResources,
                                                                @Qualifier(DECISION_REACH_CLUSTER_PROPERTIES)
                                                                ClusterConfig redisClusterProperties) {
    return RedisFactory.initLettuceClusterConnectionFactory(clientResources, redisClusterProperties);
  }

  @Bean(DECISION_REACH_CLUSTER_TEMPLATE)
  public RedisTemplate<String, Object> clusterRedisTemplate(
    @Qualifier(DECISION_REACH_CLUSTER_CONNECTION_FACTORY) RedisConnectionFactory redisConnectionFactory) {
    return RedisFactory.createTemplate(redisConnectionFactory);
  }
}
