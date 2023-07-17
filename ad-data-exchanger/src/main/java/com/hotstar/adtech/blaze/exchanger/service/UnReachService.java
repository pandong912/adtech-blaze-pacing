package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.adserver.data.redis.service.ReachDataRepository;
import com.hotstar.adtech.blaze.exchanger.api.entity.UnReachData;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import com.hotstar.adtech.blaze.exchanger.util.PlayoutIdValidator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnReachService {

  private final ReachDataRepository reachDataRepository;


  public List<UnReachResponse> batchGetCohortReach(String contentId) {
    return reachDataRepository.batchGetContentCohortReachRatio(contentId).entrySet()
      .stream()
      .map(this::buildUnReachResponse)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private UnReachResponse buildUnReachResponse(Map.Entry<String, Map<String, Double>> entry) {
    String cohort = entry.getKey();
    String[] tags = StringUtils.split(cohort, "|");
    String playoutId = tags[0];
    String ssaiTag = tags.length > 1 ? tags[1] : StringUtils.EMPTY;
    if (PlayoutIdValidator.notValidate(playoutId)) {
      return null;
    }

    return UnReachResponse.builder()
      .ssaiTag(ssaiTag)
      .playoutId(playoutId)
      .unReachDataList(buildUnReachDataList(entry.getValue()))
      .build();
  }

  public UnReachResponse getCohortReach(String contentId, String playoutId, String ssaiTag) {
    Map<String, Double> contentAllCohortReachRatio =
      reachDataRepository.getContentCohortReachRatio(contentId, playoutId + "|" + ssaiTag);
    return UnReachResponse.builder()
      .playoutId(playoutId)
      .ssaiTag(ssaiTag)
      .unReachDataList(buildUnReachDataList(contentAllCohortReachRatio))
      .build();
  }

  private List<UnReachData> buildUnReachDataList(Map<String, Double> contentAllCohortReachRatio) {
    return contentAllCohortReachRatio.entrySet().stream()
      .map(entry -> UnReachData.builder()
        .unReachRatio(entry.getValue())
        .adSetId(Long.parseLong(entry.getKey()))
        .build())
      .collect(Collectors.toList());
  }

  public List<UnReachResponse> batchGetCohortReachInShard(String contentId, int shard) {
    return reachDataRepository.batchGetContentCohortReachRatio(contentId, shard).entrySet()
      .stream()
      .map(this::buildUnReachResponse)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
}