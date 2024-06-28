package com.hotstar.adtech.blaze.ingester.repository;

import static com.hotstar.adtech.blaze.pacing.redis.MasterReplicaRedisConfig.MASTER_REPLICA_TEMPLATE;

import com.hotstar.adtech.blaze.admodel.common.util.MapSplitter;
import com.hotstar.adtech.blaze.pacing.redis.MasterReplicaRedisConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


@Repository
@ConditionalOnBean(MasterReplicaRedisConfig.class)
public class StreamCohortConcurrencyRepository {

  public static final long DEFAULT_TTL_SEC = Duration.ofDays(3).getSeconds();
  public static final long DEFAULT_CONCURRENCY_BUFFER_TIME_SEC = Duration.ofMinutes(4).getSeconds();

  public static final String DEFAULT_KEY_SPLITTER = ":";

  public static final String LIVE_CONTENT_STREAM_COHORT_CONCURRENCY_KEY_PREFIX =
    "content" + DEFAULT_KEY_SPLITTER + "stream-cohort-concurrency" + DEFAULT_KEY_SPLITTER;

  public static final String LIVE_CONTENT_STEAM_COHORT_CONCURRENCY_BUCKET_KEY_PREFIX =
    "content" + DEFAULT_KEY_SPLITTER + "stream-cohort-concurrency-bucket" + DEFAULT_KEY_SPLITTER;

  private final RedisTemplate<String, Object> redisTemplate;

  public StreamCohortConcurrencyRepository(
    @Qualifier(MASTER_REPLICA_TEMPLATE) RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  private static long getDefaultTsBucket() {
    return ((Instant.now().getEpochSecond() - DEFAULT_CONCURRENCY_BUFFER_TIME_SEC) / 60) * 60;
  }

  public Map<String, Long> getContentStreamAllCohortConcurrency(String contentId) {
    String tsBucket = getStreamCohortConcurrencyBucket(contentId);
    return getContentStreamAllCohortConcurrency(contentId, tsBucket);
  }

  public Map<String, Long> getContentStreamAllCohortConcurrency(String contentId, String tsBucket) {
    String key = getStreamCohortConcurrencyKey(contentId, tsBucket);
    return redisTemplate.<String, Long>opsForHash().entries(key);
  }

  public void setContentAllStreamCohortConcurrency(String contentId, String tsBucket,
                                                   Map<String, Long> concurrencyValues) {
    String key = getStreamCohortConcurrencyKey(contentId, tsBucket);
    MapSplitter.partition(concurrencyValues, 500)
      .forEach(chunk -> redisTemplate.opsForHash().putAll(key, chunk));
    redisTemplate.expire(key, DEFAULT_TTL_SEC, TimeUnit.SECONDS);
  }

  public void setStreamCohortConcurrencyBucket(String contentId, String tsBucket) {
    String currentBucket = getStreamCohortConcurrencyBucketKey(contentId);
    redisTemplate.opsForValue().set(currentBucket, String.valueOf(tsBucket), DEFAULT_TTL_SEC, TimeUnit.SECONDS);
  }

  public String getStreamCohortConcurrencyBucket(String contentId) {
    String key = getStreamCohortConcurrencyBucketKey(contentId);
    Object currentBucket = redisTemplate.opsForValue().get(key);
    return Objects.isNull(currentBucket) ? String.valueOf(getDefaultTsBucket()) : String.valueOf(currentBucket);
  }

  private static String getStreamCohortConcurrencyBucketKey(String contentId) {
    return LIVE_CONTENT_STEAM_COHORT_CONCURRENCY_BUCKET_KEY_PREFIX + contentId;
  }

  private static String getStreamCohortConcurrencyKey(String contentId, String tsBucket) {
    return LIVE_CONTENT_STREAM_COHORT_CONCURRENCY_KEY_PREFIX + contentId + DEFAULT_KEY_SPLITTER + tsBucket;
  }
}
