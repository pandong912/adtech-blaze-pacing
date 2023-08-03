package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.client.AdModelClient;
import com.hotstar.adtech.blaze.admodel.client.AdModelUri;
import com.hotstar.adtech.blaze.admodel.client.entity.LiveEntities;
import com.hotstar.adtech.blaze.admodel.client.model.GoalMatchInfo;
import com.hotstar.adtech.blaze.adserver.data.redis.service.ReachDataRepository;
import com.hotstar.adtech.blaze.exchanger.api.entity.UnReachData;
import com.hotstar.adtech.blaze.exchanger.api.response.AdModelResultUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import com.hotstar.adtech.blaze.exchanger.config.CacheConfig;
import com.hotstar.adtech.blaze.exchanger.util.PlayoutIdValidator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnReachService {

  private final ReachDataRepository reachDataRepository;
  private final AdModelResultService adModelResultService;
  private final AdModelClient adModelClient;


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

  @Cacheable(cacheNames = CacheConfig.REACH_AD_SET,
    cacheManager = CacheConfig.AD_MODEL_CACHE_MANAGER,
    sync = true)
  public Set<Long> getEnableReach() {
    AdModelResultUriResponse adModelResultUriResponse = adModelResultService.queryAdModelUriByVersion(-1)
      .orElseThrow(() -> new RuntimeException("AdModelResultUriResponse is empty"));
    AdModelUri adModelUri = buildAdModelUri(adModelResultUriResponse);
    LiveEntities liveEntities = adModelClient.loadLiveAdModel(adModelUri);
    return liveEntities.getGoalMatches().stream()
      .filter(GoalMatchInfo::isMaximiseReach)
      .map(GoalMatchInfo::getAdSetId)
      .collect(Collectors.toSet());
  }

  private AdModelUri buildAdModelUri(AdModelResultUriResponse adModelResultUriResponse) {
    return AdModelUri.builder()
      .id(adModelResultUriResponse.getId())
      .path(adModelResultUriResponse.getPath())
      .version(adModelResultUriResponse.getVersion())
      .build();
  }
}