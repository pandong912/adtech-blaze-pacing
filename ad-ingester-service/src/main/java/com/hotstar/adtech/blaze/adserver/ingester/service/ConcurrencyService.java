package com.hotstar.adtech.blaze.adserver.ingester.service;

import static com.hotstar.adtech.blaze.adserver.ingester.metric.MetricNames.INVALID_CONCURRENCY;
import static com.hotstar.adtech.blaze.adserver.ingester.metric.MetricNames.TOTAL_CONCURRENCY;

import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.adserver.ingester.entity.Match;
import com.hotstar.adtech.blaze.adserver.ingester.metric.MetricNames;
import com.hotstar.adtech.blaze.adserver.ingester.metric.MetricTags;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcurrencyService {

  private final PulseService pulseService;

  private final DataExchangerService dataExchangerService;

  private final StreamConcurrencyRepository streamConcurrencyRepository;

  private final StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;
  public static final String STREAM_COHORT_HASH_KEY_SPLITTER = "|";

  @Async("concurrencyExecutor")
  @Timed(MetricNames.CONTENT_CONCURRENCY_SYNC)
  public void updateMatchConcurrency(Match match) {
    String contentId = match.getContentId();
    Map<String, String> converter = dataExchangerService.getPlayoutStreamMapping(contentId);
    updateStreamConcurrency(contentId);
    updateStreamCohortConcurrency(contentId, converter);
  }


  private void updateStreamConcurrency(String contentId) {
    try {
      ConcurrencyGroup concurrencyGroup = pulseService.getLiveContentStreamConcurrency(contentId);
      String tsBucket = String.valueOf(concurrencyGroup.getTsBucket());
      streamConcurrencyRepository
        .setContentAllStreamConcurrency(contentId, tsBucket, concurrencyGroup.getConcurrencyValues());
      streamConcurrencyRepository.setStreamConcurrencyBucket(contentId, tsBucket);
    } catch (Exception e) {
      Metrics.counter(MetricNames.CONCURRENCY_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content stream concurrency, content id: " + contentId, e);
    }
  }

  private void updateStreamCohortConcurrency(String contentId, Map<String, String> converter) {
    try {
      //ConcurrencyGroup.concurrencyValues:
      //key: in-Hindi-iOS|SSAI:
      //value: concurrency number
      ConcurrencyGroup concurrencyGroup = pulseService.getLiveContentStreamCohortConcurrency(contentId);
      String tsBucket = String.valueOf(concurrencyGroup.getTsBucket());

      Map<String, Long> aggregatedStreamCohortConcurrency =
        concurrencyGroup.getConcurrencyValues().entrySet()
          .stream()
          .collect(Collectors.groupingBy(entry -> mapKeyToMultiPlatformKey(contentId, converter, entry),
            Collectors.summingLong(Map.Entry::getValue)));

      streamCohortConcurrencyRepository
        .setContentAllStreamCohortConcurrency(contentId, tsBucket, aggregatedStreamCohortConcurrency);
      streamCohortConcurrencyRepository.setStreamCohortConcurrencyBucket(contentId, tsBucket);
    } catch (Exception e) {
      Metrics.counter(MetricNames.CONCURRENCY_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content streamCohort concurrency, content id: " + contentId, e);
    }
  }

  private String mapKeyToMultiPlatformKey(String contentId, Map<String, String> converter,
                                          Map.Entry<String, Long> entry) {
    String[] tags = entry.getKey().split("\\|", -1);
    String stream = tags.length > 0 ? tags[0] : "";
    String ssaiTag = tags.length > 1 ? tags[1] : "";
    Metrics.counter(TOTAL_CONCURRENCY, "contentId", contentId).increment(entry.getValue());
    if (!converter.containsKey(stream)) {
      Metrics.counter(INVALID_CONCURRENCY, "stream", stream, "contentId", contentId).increment(entry.getValue());
      return entry.getKey();
    }
    return converter.get(stream) + STREAM_COHORT_HASH_KEY_SPLITTER + ssaiTag;
  }
}
