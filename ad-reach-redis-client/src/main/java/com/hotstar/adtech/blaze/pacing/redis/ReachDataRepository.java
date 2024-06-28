package com.hotstar.adtech.blaze.pacing.redis;

import static com.hotstar.adtech.blaze.pacing.redis.ReachClusterRedisConfig.REACH_TEMPLATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


@Repository
@ConditionalOnBean(ReachClusterRedisConfig.class)
@Slf4j
public class ReachDataRepository {
  public static final String DEFAULT_KEY_SPLITTER = "|";
  private static final int SHARD = 50;

  private static final String REACH_KEY_PREFIX = "reach" + DEFAULT_KEY_SPLITTER;

  private final RedisTemplate<String, Object> redisTemplate;

  public ReachDataRepository(@Qualifier(REACH_TEMPLATE) RedisTemplate<String, Object> redisRepository) {
    this.redisTemplate = redisRepository;
  }

  private static long getDefaultTsBucket() {
    return (Instant.now().minus(4, ChronoUnit.MINUTES).getEpochSecond() / 60) * 60;
  }

  public Map<String, Map<String, Double>> batchGetContentCohortReachRatio(String contentId, int shard) {
    String tsBucket = String.valueOf(getDefaultTsBucket());
    return batchGetContentCohortReachRatio(contentId, tsBucket, shard);
  }

  public Map<String, Map<String, Double>> batchGetContentCohortReachRatio(String contentId, String tsBucket,
                                                                          int shard) {
    String key = getReachKey(contentId, tsBucket, shard);
    return redisTemplate.<String, Map<String, Double>>opsForHash().entries(key);
  }

  private static String getReachKey(String contentId, String tsBucket, int shard) {
    return REACH_KEY_PREFIX + contentId + DEFAULT_KEY_SPLITTER + tsBucket
      + DEFAULT_KEY_SPLITTER + shard;
  }

  public void setContentCohortReachRatio(String contentId, String tsBucket, String cohort,
                                         Map<String, Double> reachRatio) {
    String reachKey = getReachKey(contentId, tsBucket, calculateShardId(cohort));
    redisTemplate.opsForHash().put(reachKey, cohort, reachRatio);
  }

  private int calculateShardId(String cohort) {
    return Math.abs(cohort.hashCode()) % SHARD;
  }
}
