package com.hotstar.adtech.blaze.exchanger.service;

import static com.hotstar.adtech.blaze.exchanger.metric.MetricNames.CONTENT_IMPRESSION_FETCH;

import com.hotstar.adtech.blaze.adserver.data.redis.service.ImpressionRepository;
import com.hotstar.adtech.blaze.exchanger.api.response.AdImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AdSetImpressionResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.CompareResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.CompareResponseDetail;
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

  public CompareResponse compareImpression(String contentId) {
    Map<String, Long> beaconAdSetImpressions = impressionRepository.getAdSetImpressions(contentId);
    Map<String, Long> pulseAdSetImpressions = impressionRepository.getPulseAdSetImpressions(contentId);
    return CompareResponse.builder()
      .compareResponseDetailList(buildList(pulseAdSetImpressions, beaconAdSetImpressions))
      .beacon(beaconAdSetImpressions)
      .pulse(pulseAdSetImpressions)
      .build();
  }

  private List<CompareResponseDetail> buildList(Map<String, Long> adSetImpressions,
                                                Map<String, Long> beaconAdSetImpressions) {
    return beaconAdSetImpressions.entrySet().stream().map(entry -> CompareResponseDetail.builder()
      .pulse(adSetImpressions.getOrDefault(entry.getKey(), 0L))
      .beacon(entry.getValue())
      .adSetId(entry.getKey())
      .percentage((entry.getValue() - adSetImpressions.getOrDefault(entry.getKey(), 0L)) * 1.0 / entry.getValue())
      .build()).collect(Collectors.toList());
  }

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
