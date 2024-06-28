package com.hotstar.adtech.blaze.reach.synchronizer.repository;

import static com.hotstar.adtech.blaze.pacing.redis.DecisionReachClusterRedisConfig.DECISION_REACH_CLUSTER_TEMPLATE;

import com.hotstar.adtech.blaze.pacing.redis.DecisionReachClusterRedisConfig;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


@Repository
@ConditionalOnBean(DecisionReachClusterRedisConfig.class)
@Slf4j
public class DecisionReachDataRepository {
  public static final long DEFAULT_TTL_SEC = Duration.ofMinutes(10).getSeconds();
  public static final String DEFAULT_KEY_SPLITTER = "|";

  private static final String REACH_KEY_PREFIX = "reach" + DEFAULT_KEY_SPLITTER;
  private static final String REACH_BUCKET_PREFIX = "reach-bucket" + DEFAULT_KEY_SPLITTER;

  private final RedisTemplate<String, Object> redisTemplate;

  public DecisionReachDataRepository(
    @Qualifier(DECISION_REACH_CLUSTER_TEMPLATE) RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  private static String getReachKey(String contentId, String tsBucket, String cohort) {
    return REACH_KEY_PREFIX + contentId + DEFAULT_KEY_SPLITTER + tsBucket
      + DEFAULT_KEY_SPLITTER + cohort;
  }

  private static String getReachBucketKey(String contentId) {
    return REACH_BUCKET_PREFIX + contentId;
  }

  public void setContentCohortReachRatio(String contentId, String cohort, String tsBucket,
                                         Map<String, Double> unReachData) {
    String key = getReachKey(contentId, tsBucket, cohort);
    redisTemplate.opsForHash().putAll(key, unReachData);
    redisTemplate.expire(key, DEFAULT_TTL_SEC, TimeUnit.SECONDS);
  }

  public void setContentReachBucket(String contentId, String tsBucket) {
    String reachBucketKey = getReachBucketKey(contentId);
    redisTemplate.opsForValue().set(reachBucketKey, tsBucket, DEFAULT_TTL_SEC, TimeUnit.SECONDS);
  }

  public Map<String, Double> getContentCohortReachRatio(String contentId, String cohort) {
    String tsBucket = getTsBucket(contentId);
    return getContentCohortReachRatio(contentId, tsBucket, cohort);
  }

  public Map<String, Double> getContentCohortReachRatio(String contentId, String tsBucket, String cohort) {
    String key = getReachKey(contentId, tsBucket, cohort);
    return redisTemplate.<String, Double>opsForHash().entries(key);
  }

  public String getTsBucket(String contentId) {
    String reachBucketKey = getReachBucketKey(contentId);
    return (String) redisTemplate.opsForValue().get(reachBucketKey);
  }
}
