package com.hotstar.adtech.blaze.ingester.service;

import static com.hotstar.adtech.blaze.ingester.metric.MetricNames.INVALID_CONCURRENCY;

import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.metric.MetricNames;
import com.hotstar.adtech.blaze.ingester.metric.MetricTags;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcurrencyService {

  private final PulseService pulseService;

  private final StreamConcurrencyRepository streamConcurrencyRepository;

  private final StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;
  public static final String STREAM_COHORT_HASH_KEY_SPLITTER = "|";

  @Async("concurrencyExecutor")
  @Timed(MetricNames.CONTENT_CONCURRENCY_SYNC)
  public void updateMatchConcurrency(Match match, Map<String, String> streamMappingConverter) {
    String contentId = match.getContentId();
    updateStreamConcurrency(contentId, streamMappingConverter);
    updateCohortConcurrency(contentId, streamMappingConverter);
  }


  private void updateStreamConcurrency(String contentId, Map<String, String> converter) {
    try {
      ConcurrencyGroup concurrencyGroup = pulseService.getLiveContentStreamConcurrency(contentId);
      String tsBucket = String.valueOf(concurrencyGroup.getTsBucket());
      Map<String, Long> aggregatedStreamConcurrency =
        concurrencyGroup.getConcurrencyValues().entrySet()
          .stream()
          .map(entry -> mapStreamKeyToPlayoutId(contentId, converter, entry.getKey(), entry.getValue()))
          .filter(Objects::nonNull)
          .collect(Collectors.groupingBy(ConcurrencyValue::getConcurrencyKey,
            Collectors.summingLong(ConcurrencyValue::getConcurrency)));

      streamConcurrencyRepository
        .setContentAllStreamConcurrency(contentId, tsBucket, aggregatedStreamConcurrency);
      streamConcurrencyRepository.setStreamConcurrencyBucket(contentId, tsBucket);
    } catch (Exception e) {
      Metrics.counter(MetricNames.CONCURRENCY_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content stream concurrency, content id: " + contentId, e);
    }
  }

  private void updateCohortConcurrency(String contentId, Map<String, String> converter) {
    try {
      //ConcurrencyGroup.concurrencyValues:
      //key: in-eng-phone-ssai|SSAI::xxx|English+Android
      //value: concurrency number
      ConcurrencyGroup concurrencyGroup = pulseService.getLiveContentStreamCohortConcurrency(contentId);
      String tsBucket = String.valueOf(concurrencyGroup.getTsBucket());

      Map<String, Long> aggregatedCohortConcurrency =
        concurrencyGroup.getConcurrencyValues().entrySet()
          .stream()
          .map(entry -> mapCohortKeyToPlayoutId(contentId, converter, entry.getKey(), entry.getValue()))
          .filter(Objects::nonNull)
          .collect(Collectors.groupingBy(ConcurrencyValue::getConcurrencyKey,
            Collectors.summingLong(ConcurrencyValue::getConcurrency)));

      streamCohortConcurrencyRepository
        .setContentAllStreamCohortConcurrency(contentId, tsBucket, aggregatedCohortConcurrency);
      streamCohortConcurrencyRepository.setStreamCohortConcurrencyBucket(contentId, tsBucket);
    } catch (Exception e) {
      Metrics.counter(MetricNames.CONCURRENCY_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content streamCohort concurrency, content id: " + contentId, e);
    }
  }

  private ConcurrencyValue mapCohortKeyToPlayoutId(String contentId, Map<String, String> converter,
                                                   String cohort, Long concurrency) {
    String[] tags = cohort.split("\\|", -1);
    String stream = tags.length > 0 ? tags[0] : "";
    String ssaiTag = tags.length > 1 ? tags[1] : "";

    String playoutId = converter.get(stream);
    if (playoutId == null) {
      record(contentId, concurrency, stream, 1000);
      return null;
    }

    return ConcurrencyValue.builder()
      .concurrencyKey(playoutId + STREAM_COHORT_HASH_KEY_SPLITTER + ssaiTag)
      .concurrency(concurrency)
      .build();
  }

  private ConcurrencyValue mapStreamKeyToPlayoutId(String contentId, Map<String, String> converter,
                                                   String streamKey, Long concurrency) {
    String[] tags = streamKey.split("\\|", -1);
    String stream = tags.length > 0 ? tags[0] : "";

    String playoutId = converter.get(stream);
    if (playoutId == null) {
      record(contentId, concurrency, stream, 20000);
      return null;
    }
    return ConcurrencyValue.builder()
      .concurrencyKey(playoutId)
      .concurrency(concurrency)
      .build();
  }

  private static void record(String contentId, Long concurrency, String stream, long threshold) {
    if (concurrency > threshold) {
      log.error("content stream mapping converter is not existed, contentId: {}, concurrency: {}", contentId,
        concurrency);
    } else {
      log.info("content stream mapping converter is not existed, contentId: {}, concurrency: {}", contentId,
        concurrency);
    }
    Metrics.counter(INVALID_CONCURRENCY, "stream", stream, "contentId", contentId).increment(concurrency);
  }

  @Value
  @Builder
  private static class ConcurrencyValue {
    String concurrencyKey;
    Long concurrency;
  }
}
