package com.hotstar.adtech.blaze.allocation.planner.service.manager.loader;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.PLAN_DATA_LOAD;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.DataProcessService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BreakTypeGroupFactory;
import com.hotstar.adtech.blaze.allocation.planner.source.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocationdata.client.model.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.RequestData;
import com.hotstar.adtech.blaze.allocationdata.client.model.Response;
import io.micrometer.core.annotation.Timed;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
@Profile("!sim && !worker")
public class GeneralPlanContextLoader {
  private final DataExchangerService dataExchangerService;
  private final DataLoader dataLoader;
  private final DataProcessService dataProcessService;
  private final BreakTypeGroupFactory breakTypeGroupFactory;

  @Timed(value = PLAN_DATA_LOAD, extraTags = {"algorithm", "general"})
  public GeneralPlanContext getGeneralPlanContext(Match match, AdModel adModel) {
    String contentId = match.getContentId();

    Map<Long, Long> adSetImpression = dataExchangerService.getAdSetImpression(contentId);
    Integer totalBreakNumber = dataExchangerService.getTotalBreakNumber(contentId);
    Integer breakIndex = getBreakIndex(contentId);
    StandardMatchProgressModel standardMatchProgressModel = dataLoader.getStandardMatchProgressModel();
    ConcurrencyData concurrencyData = getConcurrencyData(match, adModel);

    Map<String, List<AdSet>> adSetGroup = adModel.getAdSetGroup();
    List<AdSet> adSets = adSetGroup.getOrDefault(contentId, Collections.emptyList());


    BreakContext breakContext =
      dataProcessService.getBreakContext(totalBreakNumber, breakIndex, standardMatchProgressModel);

    List<DemandDiagnosis> demandDiagnosisList =
      dataProcessService.getDemandDiagnosisList(adSets, breakContext, adSetImpression);

    log.info("content:{}, total cohort size: {}", contentId, concurrencyData.getCohorts().size());
    log.info("content:{},total stream size: {}", contentId, concurrencyData.getStreams().size());
    log.info("content:{},break index: {}, total break number: {}", contentId, breakIndex, totalBreakNumber);
    log.info("content:{},ssai adSet size: {}", contentId,
      adSets.stream().filter(adSet -> !adSet.getSsaiAds().isEmpty()).count());
    log.info("content:{},spot adSet size: {}", contentId,
      adSets.stream().filter(adSet -> !adSet.getSpotAds().isEmpty()).count());

    List<Response> responses = demandDiagnosisList.stream()
      .map(dataProcessService::convertFromDemand)
      .collect(Collectors.toList());
    Map<String, Integer> attributeId2TargetingTagMap = adModel.getAttributeId2TargetingTags();

    RequestData requestData = new RequestData(concurrencyData);
    List<BreakDetail> breakDetails = dataLoader.getBreakDetail();
    List<BreakTypeGroup> breakTypeList = breakTypeGroupFactory.getBreakTypeList(adSets, breakDetails);

    return GeneralPlanContext.builder()
      .contentId(contentId)
      .concurrencyData(concurrencyData)
      .adSets(adSets)
      .attributeId2TargetingTagMap(attributeId2TargetingTagMap)
      .demandDiagnosisList(demandDiagnosisList)
      .requestData(requestData)
      .responses(responses)
      .breakContext(breakContext)
      .breakTypeList(breakTypeList)
      .build();
  }

  private Integer getBreakIndex(String contentId) {
    return dataExchangerService.getBreakList(contentId).values().stream()
      .mapToInt(List::size)
      .max().orElse(0);
  }

  private ConcurrencyData getConcurrencyData(Match match, AdModel adModel) {
    String contentId = match.getContentId();
    Map<String, PlayoutStream> playoutStreamMap = adModel.getPlayoutStreamMap(match.getSeasonId());

    List<ContentCohort> cohorts = getContentCohorts(contentId, playoutStreamMap);

    List<ContentStream> streams = getContentStreams(contentId, playoutStreamMap, cohorts.size());

    return ConcurrencyData.builder()
      .cohorts(cohorts)
      .streams(streams)
      .build();
  }

  private List<ContentCohort> getContentCohorts(String contentId, Map<String, PlayoutStream> playoutStreamMap) {
    List<ContentCohort> cohorts = dataExchangerService.getContentCohortConcurrency(contentId, playoutStreamMap);
    List<ContentCohort> ssaiCohorts = cohorts.stream()
      .filter(cohort -> cohort.getPlayoutStream().getStreamType() == StreamType.SSAI_Spot)
      .collect(Collectors.toList());

    IntStream.range(0, ssaiCohorts.size()).forEach(i -> ssaiCohorts.get(i).setConcurrencyId(i));
    return ssaiCohorts;
  }

  private List<ContentStream> getContentStreams(String contentId, Map<String, PlayoutStream> playoutStreamMap,
                                                int size) {
    List<ContentStream> streams = dataExchangerService.getContentStreamConcurrency(contentId, playoutStreamMap).stream()
      .sorted((a, b) -> b.getPlayoutStream().getStreamType().compareTo(a.getPlayoutStream().getStreamType()))
      .collect(Collectors.toList());
    IntStream.range(0, streams.size()).forEach(i -> streams.get(i).setConcurrencyId(i, size));
    return streams;
  }
}
