package com.hotstar.adtech.blaze.ingester.service;

import static com.hotstar.adtech.blaze.ingester.metric.MetricNames.INVALID_CONCURRENCY;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.launchdarkly.DynamicConfig;
import com.hotstar.adtech.blaze.ingester.metric.MetricNames;
import com.hotstar.adtech.blaze.ingester.metric.MetricTags;
import com.hotstar.adtech.blaze.ingester.repository.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.ingester.repository.StreamConcurrencyRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
@Slf4j
public class ConcurrencyService {

  private final PulseService pulseService;

  private final StreamConcurrencyRepository streamConcurrencyRepository;

  private final StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;
  public static final String STREAM_COHORT_HASH_KEY_SPLITTER = "|";
  private final DynamicConfig ldConfig;

  @Async("concurrencyExecutor")
  @Timed(MetricNames.CONTENT_CONCURRENCY_SYNC)
  public void updateMatchConcurrency(Match match, Map<String, String> streamMappingConverter) {
    String contentId = match.getContentId();
    Map<String, String> streamMappingConverterForStream = buildConverterForStreamConcurrency(streamMappingConverter);
    updateStreamConcurrency(contentId, streamMappingConverterForStream);
    updateCohortConcurrency(contentId, streamMappingConverter);
  }

  public Map<String, String> buildConverterForStreamConcurrency(Map<String, String> oldConverter) {
    try {
      if (!ldConfig.getEnableSsaiStramIncludeSpotUser()) {
        return oldConverter;
      }
      Map<String, String> streamMappingConverterForStream = new HashMap<>(oldConverter);
      oldConverter.forEach(
        (playbackTagStr, playoutId) -> modifyMap(playbackTagStr, playoutId, streamMappingConverterForStream));
      log.info("oldConverter {}, newConverter: {}", oldConverter, streamMappingConverterForStream);
      return streamMappingConverterForStream;
    } catch (Exception e) {
      log.error("Fail to build stream mapping converter for stream concurrency, will use origin stream mapping", e);
      return oldConverter;
    }
  }

  private static void modifyMap(String playbackTagStr, String playoutId, Map<String, String> converter) {
    String[] playbackTags = StringUtils.split(playbackTagStr, "-");
    if (playbackTags.length < 4) {
      return;
    }
    if (StreamType.SSAI_Spot.getAds().equals(playbackTags[3])) {
      playbackTags[3] = StreamType.Spot.getAds();
      String newPlaybackTagStr = StringUtils.join(playbackTags, "-");
      if (converter.containsKey(newPlaybackTagStr)) {
        log.info("stream mapping converter is existed, playbackTagStr: {}, existed playoutId: {}", newPlaybackTagStr,
          converter.get(newPlaybackTagStr));
        return;
      }
      converter.put(newPlaybackTagStr, playoutId);
    }
  }

  private void updateStreamConcurrency(String contentId, Map<String, String> converter) {
    try {
      var tsBucketAndStreamConcurrency = tsBucketAndStreamConcurrency(contentId, converter);
      String tsBucket = tsBucketAndStreamConcurrency.getLeft();
      Map<String, Long> aggregatedStreamConcurrency = tsBucketAndStreamConcurrency.getRight();

      streamConcurrencyRepository
        .setContentAllStreamConcurrency(contentId, tsBucket, aggregatedStreamConcurrency);
      streamConcurrencyRepository.setStreamConcurrencyBucket(contentId, tsBucket);
    } catch (Exception e) {
      Metrics.counter(MetricNames.CONCURRENCY_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content stream concurrency, content id: " + contentId, e);
    }
  }

  protected Pair<String, Map<String, Long>> tsBucketAndStreamConcurrency(String contentId,
                                                                         Map<String, String> converter) {
    ConcurrencyGroup concurrencyGroup = pulseService.getLiveContentStreamConcurrency(contentId);
    String tsBucket = String.valueOf(concurrencyGroup.getTsBucket());
    Map<String, Long> aggregatedStreamConcurrency =
      concurrencyGroup.getConcurrencyValues().entrySet()
        .stream()
        .map(entry -> mapStreamKeyToPlayoutId(contentId, converter, entry.getKey(), entry.getValue()))
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(ConcurrencyValue::getConcurrencyKey,
          Collectors.summingLong(ConcurrencyValue::getConcurrency)));
    return Pair.of(tsBucket, aggregatedStreamConcurrency);
  }

  private void updateCohortConcurrency(String contentId, Map<String, String> converter) {
    try {
      var tsBucketAndCohortConcurrency = tsBucketAndCohortConcurrency(contentId, converter);
      String tsBucket = String.valueOf(tsBucketAndCohortConcurrency.getLeft());
      Map<String, Long> aggregatedCohortConcurrency = tsBucketAndCohortConcurrency.getRight();

      streamCohortConcurrencyRepository
        .setContentAllStreamCohortConcurrency(contentId, tsBucket, aggregatedCohortConcurrency);
      streamCohortConcurrencyRepository.setStreamCohortConcurrencyBucket(contentId, tsBucket);
    } catch (Exception e) {
      Metrics.counter(MetricNames.CONCURRENCY_UPDATE_EXCEPTION, MetricTags.EXCEPTION_CLASS, e.getClass().getName())
        .increment();
      log.error("Fail to update content streamCohort concurrency, content id: " + contentId, e);
    }
  }

  protected Pair<String, Map<String, Long>> tsBucketAndCohortConcurrency(String contentId,
                                                                         Map<String, String> converter) {
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
    return Pair.of(tsBucket, aggregatedCohortConcurrency);
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
