package com.hotstar.adtech.blaze.ingester.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hotstar.adtech.blaze.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.ingester.repository.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.ingester.repository.StreamConcurrencyRepository;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

public class InjectConcurrencyService extends ConcurrencyService {

  private final Cache<String, Map<String, Long>> injectedCohortCache = Caffeine.newBuilder()
    .maximumSize(20)
    .expireAfterWrite(Duration.ofDays(2))
    .build();

  private final Cache<String, Map<String, Long>> injectedStreamCache = Caffeine.newBuilder()
    .maximumSize(20)
    .expireAfterWrite(Duration.ofDays(2))
    .build();

  public InjectConcurrencyService(PulseService pulseService,
                                  StreamConcurrencyRepository streamConcurrencyRepository,
                                  StreamCohortConcurrencyRepository streamCohortConcurrencyRepository) {
    super(pulseService, streamConcurrencyRepository, streamCohortConcurrencyRepository);
  }

  @Override
  protected Pair<String, Map<String, Long>> tsBucketAndStreamConcurrency(String contentId,
                                                                         Map<String, String> converter) {
    return Optional.ofNullable(injectedStreamCache.getIfPresent(contentId))
      .map(concurrency -> Pair.of(currentBucket(), concurrency))
      .orElseGet(() -> super.tsBucketAndStreamConcurrency(contentId, converter));
  }

  @Override
  protected Pair<String, Map<String, Long>> tsBucketAndCohortConcurrency(String contentId,
                                                                         Map<String, String> converter) {
    return Optional.ofNullable(injectedCohortCache.getIfPresent(contentId))
      .map(concurrency -> Pair.of(currentBucket(), concurrency))
      .orElseGet(() -> super.tsBucketAndCohortConcurrency(contentId, converter));
  }

  public void putCohort(String contentId, Map<String, Long> concurrencyGroup) {
    injectedCohortCache.put(contentId, concurrencyGroup);
  }

  public void clearCohort(String contentId) {
    injectedCohortCache.invalidate(contentId);
  }

  public Set<String> injectedCohortKeys() {
    return injectedCohortCache.asMap().keySet();
  }

  public void putStream(String contentId, Map<String, Long> concurrencyGroup) {
    injectedStreamCache.put(contentId, concurrencyGroup);
  }

  public void clearStream(String contentId) {
    injectedStreamCache.invalidate(contentId);
  }

  public Set<String> injectedStreamKeys() {
    return injectedStreamCache.asMap().keySet();
  }

  private String currentBucket() {
    return String.valueOf((long) (Math.floor((double) System.currentTimeMillis() / 1000 / 60) * 60));
  }
}
