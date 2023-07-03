package com.hotstar.adtech.blaze.exchanger.service;

import static com.hotstar.adtech.blaze.exchanger.metric.MetricNames.CONTENT_COHORT_CONCURRENCY_FETCH;
import static com.hotstar.adtech.blaze.exchanger.metric.MetricNames.CONTENT_STREAM_CONCURRENCY_FETCH;

import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.exchanger.api.entity.LanguageMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.PlatformMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentCohortConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamConcurrencyResponse;
import com.hotstar.adtech.blaze.exchanger.config.CacheConfig;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcurrencyService {

  private final StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;

  private final StreamConcurrencyRepository streamConcurrencyRepository;
  private final MetaDataService metaDataService;

  @Timed(CONTENT_COHORT_CONCURRENCY_FETCH)
  @Cacheable(cacheNames = CacheConfig.COHORT_CONCURRENCY,
    cacheManager = CacheConfig.COHORT_CONCURRENCY_MANAGER,
    sync = true)
  public List<ContentCohortConcurrencyResponse> getContentCohortWiseConcurrency(String contentId) {
    Map<String, Long> streamCohortConcurrency =
      streamCohortConcurrencyRepository.getContentStreamAllCohortConcurrency(contentId);
    PlatformMapping platformMapping = metaDataService.getPlatformMapping();
    LanguageMapping languageMapping = metaDataService.getLanguageMapping();
    return streamCohortConcurrency.entrySet().stream()
      .map(entry -> buildCohortConcurrencyResponse(entry.getKey(), entry.getValue(), platformMapping, languageMapping))
      .collect(Collectors.toList());
  }

  private ContentCohortConcurrencyResponse buildCohortConcurrencyResponse(String tag, Long concurrency,
                                                                          PlatformMapping platformMapping,
                                                                          LanguageMapping languageMapping) {
    String[] tags = tag.split("\\|", -1);
    String stream = tags[0];
    String ssaiTag = tags[1];
    StreamDetail streamDetail = StreamDetail.fromString(stream, platformMapping, languageMapping);
    return ContentCohortConcurrencyResponse.builder().ssaiTag(ssaiTag).streamDetail(streamDetail)
      .concurrencyValue(concurrency).build();
  }

  @Timed(CONTENT_STREAM_CONCURRENCY_FETCH)
  @Cacheable(cacheNames = CacheConfig.STREAM_CONCURRENCY,
    cacheManager = CacheConfig.STREAM_CONCURRENCY_MANAGER,
    sync = true)
  public List<ContentStreamConcurrencyResponse> getContentStreamWiseConcurrency(String contentId) {
    Map<String, Long> streamConcurrency =
      streamCohortConcurrencyRepository.getContentStreamAllCohortConcurrency(contentId);
    Map<String, Long> aggregatedStreamConcurrency = streamConcurrency.entrySet().stream()
      .collect(Collectors.groupingBy(entry -> entry.getKey().split("\\|", -1)[0],
        Collectors.summingLong(Map.Entry::getValue)));
    PlatformMapping platformMapping = metaDataService.getPlatformMapping();
    LanguageMapping languageMapping = metaDataService.getLanguageMapping();
    return aggregatedStreamConcurrency
      .entrySet()
      .stream()
      .map(entry -> ContentStreamConcurrencyResponse.builder()
        .concurrencyValue(entry.getValue())
        .streamDetail(StreamDetail.fromString(entry.getKey(), platformMapping, languageMapping))
        .build())
      .collect(Collectors.toList());
  }

  @Cacheable(cacheNames = CacheConfig.SINGLE_PLATFORM_STREAM_CONCURRENCY,
    cacheManager = CacheConfig.SINGLE_PLATFORM_STREAM_CONCURRENCY_MANAGER,
    sync = true)
  public Long getContentSingleStreamConcurrency(String contentId, Tenant tenant, String language, String platform) {
    String key = getStreamHashKeyForConcurrency(tenant, language, platform);
    return streamConcurrencyRepository.getContentStreamConcurrency(contentId, key);
  }

  private String getStreamHashKeyForConcurrency(Tenant tenant, String language, String platform) {
    return tenant.getName() + "-" + language + "-" + platform;
  }

}
