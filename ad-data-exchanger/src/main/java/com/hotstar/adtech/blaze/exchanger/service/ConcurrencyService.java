package com.hotstar.adtech.blaze.exchanger.service;

import static com.hotstar.adtech.blaze.exchanger.metric.MetricNames.CONTENT_COHORT_CONCURRENCY_FETCH;
import static com.hotstar.adtech.blaze.exchanger.metric.MetricNames.CONTENT_STREAM_CONCURRENCY_FETCH;

import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.config.CacheConfig;
import com.hotstar.adtech.blaze.exchanger.util.PlayoutIdValidator;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcurrencyService {

  private final StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;

  private final StreamConcurrencyRepository streamConcurrencyRepository;

  @Timed(CONTENT_COHORT_CONCURRENCY_FETCH)
  @Cacheable(cacheNames = CacheConfig.COHORT_CONCURRENCY,
    cacheManager = CacheConfig.COHORT_CONCURRENCY_MANAGER,
    sync = true)
  public List<ContentCohortConcurrencyResponse> getContentCohortWiseConcurrency(String contentId) {
    Map<String, Long> streamCohortConcurrency =
      streamCohortConcurrencyRepository.getContentStreamAllCohortConcurrency(contentId);

    return streamCohortConcurrency.entrySet().stream()
      .map(entry -> buildCohortConcurrencyResponse(entry.getKey(), entry.getValue()))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private ContentCohortConcurrencyResponse buildCohortConcurrencyResponse(String tag, Long concurrency) {
    String[] tags = tag.split("\\|", -1);
    String stream = tags[0];
    String ssaiTag = tags[1];
    if (PlayoutIdValidator.notValidate(stream)) {
      return null;
    }
    return ContentCohortConcurrencyResponse.builder()
      .ssaiTag(ssaiTag)
      .playoutId(stream)
      .concurrencyValue(concurrency)
      .build();
  }

  @Timed(CONTENT_STREAM_CONCURRENCY_FETCH)
  @Cacheable(cacheNames = CacheConfig.STREAM_CONCURRENCY,
    cacheManager = CacheConfig.STREAM_CONCURRENCY_MANAGER,
    sync = true)
  public List<ContentStreamConcurrencyResponse> getContentStreamWiseConcurrency(String contentId) {
    Map<String, Long> streamConcurrency =
      streamConcurrencyRepository.getContentAllStreamConcurrency(contentId);

    return streamConcurrency
      .entrySet()
      .stream()
      .map(entry -> ContentStreamConcurrencyResponse.builder()
        .concurrencyValue(entry.getValue())
        .playoutId(entry.getKey())
        .build())
      .collect(Collectors.toList());
  }

  @Cacheable(cacheNames = CacheConfig.SINGLE_PLATFORM_STREAM_CONCURRENCY,
    cacheManager = CacheConfig.SINGLE_PLATFORM_STREAM_CONCURRENCY_MANAGER,
    sync = true)
  public Long getContentStreamConcurrencyWithPlayoutId(String contentId, String playoutId) {
    return streamConcurrencyRepository.getContentStreamConcurrency(contentId, playoutId);
  }

}
