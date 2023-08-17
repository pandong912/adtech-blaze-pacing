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
          .collect(Collectors.groupingBy(entry -> mapStreamKeyToPlayoutId(contentId, converter, entry),
            Collectors.summingLong(Map.Entry::getValue)));

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
          .collect(Collectors.groupingBy(entry -> mapCohortKeyToPlayoutId(contentId, converter, entry),
            Collectors.summingLong(Map.Entry::getValue)));

      streamCohortConcurrencyRepository
        .setContentAllStreamCohortConcurrency(contentId, tsBucket, aggregatedCohortConcurrency);
      streamCohortConcurrencyRepository.setStreamCohortConcurrencyBucket(contentId, tsBucket);
    } catch (Exception e) {
      Metrics.counter(MetricNames.CONCURRENCY_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content streamCohort concurrency, content id: " + contentId, e);
    }
  }

  private String mapCohortKeyToPlayoutId(String contentId, Map<String, String> converter,
                                         Map.Entry<String, Long> entry) {
    String[] tags = entry.getKey().split("\\|", -1);
    String stream = tags.length > 0 ? tags[0] : "";
    String ssaiTag = tags.length > 1 ? tags[1] : "";
    return getPlayoutId(contentId, converter, stream, entry.getValue()) + STREAM_COHORT_HASH_KEY_SPLITTER + ssaiTag;
  }

  private String mapStreamKeyToPlayoutId(String contentId, Map<String, String> converter,
                                         Map.Entry<String, Long> entry) {
    String[] tags = entry.getKey().split("\\|", -1);
    String stream = tags.length > 0 ? tags[0] : "";
    return getPlayoutId(contentId, converter, stream, entry.getValue());
  }

  private String getPlayoutId(String contentId, Map<String, String> converter, String stream, Long concurrency) {
    String playoutId = converter.get(stream);
    if (playoutId == null) {
      log.warn("content stream mapping converter is not existed, contentId: {}, concurrency: {}", contentId,
        concurrency);
      Metrics.counter(INVALID_CONCURRENCY, "stream", stream, "contentId", contentId).increment();
      return stream;
    }

    return playoutId;
  }
}
