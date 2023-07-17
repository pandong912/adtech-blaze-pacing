package com.hotstar.adtech.blaze.reach.synchronizer.service;

import com.hotstar.adtech.blaze.adserver.data.redis.service.DecisionReachDataRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.ReachDataRepository;
import com.hotstar.adtech.blaze.reach.synchronizer.entity.Match;
import com.hotstar.adtech.blaze.reach.synchronizer.metric.MetricNames;
import com.hotstar.adtech.blaze.reach.synchronizer.metric.MetricTags;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReachService {
  private static final int SHARD = 50;

  private final DecisionReachDataRepository decisionReachDataRepository;
  private final ReachDataRepository originReachDataRepository;
  private final ForkJoinPool customThreadPool = new ForkJoinPool(1);

  @Timed(MetricNames.CONTENT_REACH_SHARD_SYNC)
  public void updateMatchReachMatch(Match match, Map<Long, Boolean> adSetMaximiseReach) {
    try {
      String contentId = match.getContentId();
      customThreadPool.submit(() -> syncReachData(adSetMaximiseReach, contentId)).get();
    } catch (Exception e) {
      Metrics.counter(MetricNames.REACH_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content stream concurrency, content id: " + match.getContentId(), e);
    }
  }

  private void syncReachData(Map<Long, Boolean> adSetMaximiseReach, String contentId) {
    String defaultTsBucket = getDefaultTsBucket();
    int sum = IntStream.range(0, SHARD)
      .parallel()
      .mapToObj(shard -> originReachDataRepository.batchGetContentCohortReachRatio(contentId, shard))
      .flatMap(map -> map.entrySet().stream())
      .mapToInt(entry -> writeToRedis(contentId, defaultTsBucket, entry, adSetMaximiseReach))
      .sum();
    if (sum > 0) {
      log.info("Synced reach data for content id: {}, cohort * adSet number: {}", contentId, sum);
      decisionReachDataRepository.setContentReachBucket(contentId, defaultTsBucket);
    }
  }

  private int writeToRedis(String contentId, String defaultTsBucket, Map.Entry<String, Map<String, Double>> entry,
                           Map<Long, Boolean> adSetMaximiseReach) {
    Map<String, Double> afterFilter = entry.getValue()
      .entrySet()
      .stream()
      .filter(reach -> adSetMaximiseReach.getOrDefault(Long.valueOf(reach.getKey()), false))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    decisionReachDataRepository.setContentCohortReachRatio(contentId, entry.getKey(), defaultTsBucket, afterFilter);
    return afterFilter.size();
  }

  private String getDefaultTsBucket() {
    return String.valueOf((Instant.now().getEpochSecond() / 60) * 60);
  }
}
