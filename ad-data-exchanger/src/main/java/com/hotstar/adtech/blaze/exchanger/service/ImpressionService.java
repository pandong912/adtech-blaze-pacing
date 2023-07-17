package com.hotstar.adtech.blaze.exchanger.service;

import static com.hotstar.adtech.blaze.exchanger.metric.MetricNames.CONTENT_IMPRESSION_FETCH;

import com.hotstar.adtech.blaze.adserver.data.redis.service.ImpressionRepository;
import com.hotstar.adtech.blaze.exchanger.api.response.AdImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImpressionService {

  private final ImpressionRepository impressionRepository;

  @Timed(CONTENT_IMPRESSION_FETCH)
  public List<AdSetImpressionResponse> getAdSetImpression(String contentId) {
    Map<String, Long> adSetImpressions = impressionRepository.getAdSetImpressions(contentId);
    return adSetImpressions.entrySet().stream()
      .filter(entry -> keyIsValid(entry.getKey()))
      .map(entry -> AdSetImpressionResponse.builder()
        .adSetId(Long.parseLong(entry.getKey()))
        .impression(entry.getValue())
        .build())
      .collect(Collectors.toList());
  }

  public AdSetImpressionResponse getAdSetImpression(String contentId, Long adSetId) {
    Long adSetImpression = impressionRepository.getAdSetImpression(contentId, adSetId);
    return AdSetImpressionResponse.builder()
      .adSetId(adSetId)
      .impression(adSetImpression)
      .build();
  }

  private boolean keyIsValid(String key) {
    try {
      Long.parseLong(key);
      return true;
    } catch (Exception e) {
      log.error("Error parsing adSetId from key: {}", key);
      return false;
    }
  }

  public List<AdImpressionResponse> getAdImpression(String contentId) {
    Map<String, Long> adImpressions = impressionRepository.getAdImpressions(contentId);
    return adImpressions.entrySet().stream()
      .map(entry -> AdImpressionResponse.builder()
        .creativeId(entry.getKey())
        .impression(entry.getValue())
        .build())
      .collect(Collectors.toList());
  }

  public AdImpressionResponse getAdImpression(String contentId, String creativeId) {
    Long adImpression = impressionRepository.getAdImpression(contentId, creativeId);
    return AdImpressionResponse.builder()
      .creativeId(creativeId)
      .impression(adImpression)
      .build();
  }

}
