package com.hotstar.adtech.blaze.ingester.repository;

import static com.hotstar.adtech.blaze.ingester.config.MasterReplicaRedisConfig.MASTER_REPLICA_TEMPLATE;

import com.hotstar.adtech.blaze.ingester.config.MasterReplicaRedisConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


@Repository
@ConditionalOnBean(MasterReplicaRedisConfig.class)
public class StreamConcurrencyRepository {

  public static final long DEFAULT_TTL_SEC = Duration.ofDays(3).getSeconds();

  public static final String DEFAULT_KEY_SPLITTER = ":";

  public static final long DEFAULT_CONCURRENCY_BUFFER_TIME_SEC = Duration.ofMinutes(4).getSeconds();

  private static final String LIVE_CONTENT_STREAM_CONCURRENCY_BUCKET_KEY_PREFIX =
    "content" + DEFAULT_KEY_SPLITTER + "stream-concurrency-bucket" + DEFAULT_KEY_SPLITTER;
  private static final String LIVE_CONTENT_STREAM_CONCURRENCY_KEY_PREFIX =
    "content" + DEFAULT_KEY_SPLITTER + "stream-concurrency" + DEFAULT_KEY_SPLITTER;

  private final RedisTemplate<String, Object> redisTemplate;
  private final HashOperations<String, String, Long> hashOperations;

  public StreamConcurrencyRepository(@Qualifier(MASTER_REPLICA_TEMPLATE) RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.hashOperations = redisTemplate.opsForHash();
  }

  public void setContentAllStreamConcurrency(String contentId, String tsBucket, Map<String, Long> concurrencyValues) {
    String key = getStreamConcurrencyKey(contentId, tsBucket);
    hashOperations.putAll(key, concurrencyValues);
    redisTemplate.expire(key, DEFAULT_TTL_SEC, TimeUnit.SECONDS);
  }

  public void setStreamConcurrencyBucket(String contentId, String tsBucket) {
    String currentBucket = getStreamConcurrencyBucketKey(contentId);
    redisTemplate.opsForValue().set(currentBucket, String.valueOf(tsBucket), DEFAULT_TTL_SEC, TimeUnit.SECONDS);
  }

  private static String getStreamConcurrencyBucketKey(String contentId) {
    return LIVE_CONTENT_STREAM_CONCURRENCY_BUCKET_KEY_PREFIX + contentId;
  }

  private static String getStreamConcurrencyKey(String contentId, String tsBucket) {
    return LIVE_CONTENT_STREAM_CONCURRENCY_KEY_PREFIX + contentId + DEFAULT_KEY_SPLITTER + tsBucket;
  }

  public Map<String, Long> getContentAllStreamConcurrency(String contentId) {
    String tsBucket = getStreamConcurrencyBucket(contentId);
    return getContentAllStreamConcurrency(contentId, tsBucket);
  }

  public Map<String, Long> getContentAllStreamConcurrency(String contentId, String tsBucket) {
    String key = getStreamConcurrencyKey(contentId, tsBucket);
    return hashOperations.entries(key);
  }

  private String getStreamConcurrencyBucket(String contentId) {
    String key = getStreamConcurrencyBucketKey(contentId);
    Object currentBucket = redisTemplate.opsForValue().get(key);
    return Objects.isNull(currentBucket) ? String.valueOf(getDefaultTsBucket()) : String.valueOf(currentBucket);
  }

  private static long getDefaultTsBucket() {
    return ((Instant.now().getEpochSecond() - DEFAULT_CONCURRENCY_BUFFER_TIME_SEC) / 60) * 60;
  }

}
