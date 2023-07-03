package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.adserver.data.redis.service.ReachCohortListRepository;
import com.hotstar.adtech.blaze.adserver.data.redis.service.ReachDataRepository;
import com.hotstar.adtech.blaze.exchanger.api.entity.CohortInfo;
import com.hotstar.adtech.blaze.exchanger.api.entity.LanguageMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.PlatformMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import com.hotstar.adtech.blaze.exchanger.api.entity.UnReachData;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.PlayoutStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.UnReachResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnReachService {

  private final ReachDataRepository reachDataRepository;

  private final ReachCohortListRepository reachCohortListRepository;
  private final MetaDataService metaDataService;


  public List<UnReachResponse> batchGetCohortReach(String contentId, List<CohortInfo> cohortInfos) {
    return cohortInfos.parallelStream()
      .map(cohortInfo -> buildUnReachResponse(contentId, cohortInfo))
      .collect(Collectors.toList());
  }

  public UnReachResponse getCohortReach(String contentId, String streamId, String ssaiTag,
                                        ContentStreamResponse streamDefinition) {
    Map<String, StreamDetail> streamId2Stream = streamDefinition.getPlayoutStreamResponses().stream()
      .collect(Collectors.toMap(PlayoutStreamResponse::getPlayoutId, PlayoutStreamResponse::getStreamDetail));
    StreamDetail streamDetail = streamId2Stream.get(streamId);
    Map<Long, Double> contentAllCohortReachRatio =
      reachDataRepository.getContentCohortReachRatio(contentId, streamDetail.getKey() + "|" + ssaiTag);
    return UnReachResponse.builder()
      .streamDetail(streamDetail)
      .ssaiTag(ssaiTag)
      .unReachDataList(buildUnReachDataList(contentAllCohortReachRatio))
      .build();
  }

  public List<CohortInfo> getCohortList(String contentId) {
    Set<String> cohortList = reachCohortListRepository.getContentAllCohort(contentId);
    PlatformMapping platformMapping = metaDataService.getPlatformMapping();
    LanguageMapping languageMapping = metaDataService.getLanguageMapping();
    return cohortList.stream()
      .map(cohort -> parseCohort(cohort, platformMapping, languageMapping))
      .collect(Collectors.toList());
  }

  private CohortInfo parseCohort(String cohort, PlatformMapping platformMapping,
                                 LanguageMapping languageMapping) {
    String[] tags = cohort.split("//|", -1);
    String stream = tags.length > 0 ? tags[0] : "";
    String ssaiTag = tags.length > 1 ? tags[1] : "";
    return CohortInfo.builder()
      .streamDetail(StreamDetail.fromString(stream, platformMapping, languageMapping))
      .ssaiTag(ssaiTag)
      .build();
  }

  private UnReachResponse buildUnReachResponse(String contentId, CohortInfo cohortInfo) {
    Map<Long, Double> contentAllCohortReachRatio =
      reachDataRepository.getContentCohortReachRatio(contentId, cohortInfo.getRedisKey());
    return UnReachResponse.builder()
      .streamDetail(cohortInfo.getStreamDetail())
      .ssaiTag(cohortInfo.getSsaiTag())
      .unReachDataList(buildUnReachDataList(contentAllCohortReachRatio))
      .build();
  }

  private List<UnReachData> buildUnReachDataList(Map<Long, Double> contentAllCohortReachRatio) {
    return contentAllCohortReachRatio.entrySet().stream()
      .map(entry -> UnReachData.builder()
        .unReachRatio(entry.getValue())
        .adSetId(entry.getKey())
        .build())
      .collect(Collectors.toList());
  }

}