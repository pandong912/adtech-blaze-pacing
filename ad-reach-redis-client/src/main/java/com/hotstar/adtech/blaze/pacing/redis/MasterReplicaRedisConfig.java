package com.hotstar.adtech.blaze.pacing.redis;

import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.CONNECTION_FACTORY;
import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.PROPERTIES;
import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.STRING_TEMPLATE;
import static com.hotstar.adtech.blaze.adserver.data.redis.RedisConst.TEMPLATE;

import com.hotstar.adtech.blaze.adserver.data.redis.RedisFactory;
import com.hotstar.adtech.blaze.adserver.data.redis.model.MasterReplicaConfig;
import io.lettuce.core.resource.ClientResources;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(name = "blaze.redis.master-replica.enable")
@RequiredArgsConstructor
public class MasterReplicaRedisConfig {

  private static final String MASTER_REPLICA = "masterReplica";
  private static final String MASTER_REPLICA_PROPERTIES = MASTER_REPLICA + PROPERTIES;
  private static final String MASTER_REPLICA_CONNECTION_FACTORY = MASTER_REPLICA + CONNECTION_FACTORY;
  public static final String MASTER_REPLICA_TEMPLATE = MASTER_REPLICA + TEMPLATE;
  public static final String MASTER_REPLICA_STRING_TEMPLATE = MASTER_REPLICA + STRING_TEMPLATE;

  @Bean(MASTER_REPLICA_PROPERTIES)
  @ConfigurationProperties(prefix = "blaze.redis.master-replica")
  public MasterReplicaConfig masterReplicaRedisProperties() {
    return new MasterReplicaConfig();
  }

  @Bean(MASTER_REPLICA_CONNECTION_FACTORY)
  public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources,
                                                       @Qualifier(MASTER_REPLICA_PROPERTIES)
                                                       MasterReplicaConfig properties) {
    LettuceClientConfiguration clientConfig =
      RedisFactory.initLettuceClientConfiguration(clientResources, properties);

    RedisStaticMasterReplicaConfiguration staticMasterReplicaConfiguration =
      new RedisStaticMasterReplicaConfiguration(properties.getMaster().getHost(), properties.getMaster().getPort());
    properties.getReplicas()
      .forEach(replica -> staticMasterReplicaConfiguration.addNode(replica.getHost(), replica.getPort()));
    return new LettuceConnectionFactory(staticMasterReplicaConfiguration, clientConfig);
  }

  @Bean(MASTER_REPLICA_TEMPLATE)
  public RedisTemplate<String, Object> redisTemplate(
    @Qualifier(MASTER_REPLICA_CONNECTION_FACTORY) RedisConnectionFactory redisConnectionFactory) {
    return RedisFactory.createTemplate(redisConnectionFactory);
  }

  @Bean(MASTER_REPLICA_STRING_TEMPLATE)
  public StringRedisTemplate stringRedisTemplate(
    @Qualifier(MASTER_REPLICA_CONNECTION_FACTORY) RedisConnectionFactory redisConnectionFactory) {
    return new StringRedisTemplate(redisConnectionFactory);
  }

}
