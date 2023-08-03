package com.hotstar.adtech.blaze.allocation.planner.service.manager.loader;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.PLAN_DATA_LOAD;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.DataProcessService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class GeneralPlanContextLoader {
  private final DataExchangerService dataExchangerService;
  private final DataLoader dataLoader;
  private final DataProcessService dataProcessService;

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

    long count =
      concurrencyData.getCohorts().stream().filter(c -> StreamType.SSAI_Spot.equals(c.getStreamType())).count();
    log.info("total cohort size: {} ,ssai cohorts size: {}", concurrencyData.getCohorts().size(), count);
    log.info("break index: {}, total break number: {}", breakIndex, totalBreakNumber);
    log.info("adSet size: {}", adSets.size());

    List<Response> responses = demandDiagnosisList.stream()
      .map(dataProcessService::convertFromDemand)
      .collect(Collectors.toList());
    Map<String, Integer> attributeId2TargetingTagMap = adModel.getAttributeId2TargetingTags();
    List<Request> ssaiAndSpotRequests = concurrencyData.getCohorts().stream()
      .map(this::buildRequest)
      .collect(Collectors.toList());
    List<Request> spotRequests = concurrencyData.getStreams().stream()
      .map(this::buildRequest)
      .collect(Collectors.toList());


    return GeneralPlanContext.builder()
      .contentId(contentId)
      .concurrencyData(concurrencyData)
      .adSets(adSets)
      .attributeId2TargetingTagMap(attributeId2TargetingTagMap)
      .demandDiagnosisList(demandDiagnosisList)
      .ssaiAndSpotRequests(ssaiAndSpotRequests)
      .spotRequests(spotRequests)
      .responses(responses)
      .breakContext(breakContext)
      .breakDetails(dataLoader.getBreakDetail())
      .build();
  }

  private Request buildRequest(ContentCohort contentCohort) {
    return Request.builder()
      .concurrency(contentCohort.getConcurrency())
      .concurrencyId(contentCohort.getConcurrencyId())
      .build();
  }

  private Request buildRequest(ContentStream contentStream) {
    return Request.builder()
      .concurrency(contentStream.getConcurrency())
      .concurrencyId(contentStream.getConcurrencyId())
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

    List<ContentStream> streams = getContentStreams(contentId, playoutStreamMap);

    return ConcurrencyData.builder()
      .cohorts(cohorts)
      .streams(streams)
      .build();
  }

  private List<ContentCohort> getContentCohorts(String contentId, Map<String, PlayoutStream> playoutStreamMap) {
    List<ContentCohort> cohorts = dataExchangerService.getContentCohortConcurrency(contentId, playoutStreamMap);
    List<ContentCohort> ssaiCohorts = cohorts.stream()
      .filter(cohort -> cohort.getStreamType() == StreamType.SSAI_Spot)
      .collect(Collectors.toList());

    IntStream.range(0, ssaiCohorts.size()).forEach(i -> ssaiCohorts.get(i).setConcurrencyId(i));
    return ssaiCohorts;
  }

  private List<ContentStream> getContentStreams(String contentId, Map<String, PlayoutStream> playoutStreamMap) {
    List<ContentStream> streams = dataExchangerService.getContentStreamConcurrency(contentId, playoutStreamMap);
    IntStream.range(0, streams.size()).forEach(i -> streams.get(i).setConcurrencyId(i));
    return streams;
  }


}
