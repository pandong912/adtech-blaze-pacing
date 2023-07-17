package com.hotstar.adtech.blaze.allocation.planner.service.manager.loader;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.PLAN_DATA_LOAD;

import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ConcurrencyData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.DataProcessService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDefinition;
import io.micrometer.core.annotation.Timed;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GeneralPlanContextLoader {
  private final DataExchangerService dataExchangerService;
  private final DataLoader dataLoader;
  private final DataProcessService dataProcessService;

  @Timed(value = PLAN_DATA_LOAD, extraTags = {"algorithm", "general"})
  public GeneralPlanContext getGeneralPlanContext(Match match, AdModel adModel) {
    String contentId = match.getContentId();

    Map<Long, Long> adSetImpression = dataExchangerService.getAdSetImpression(contentId);
    List<BreakDetail> breakDetails = dataLoader.getBreakDetail();
    Integer totalBreakNumber = dataExchangerService.getTotalBreakNumber(contentId);
    Integer breakIndex = getBreakIndex(contentId);
    StandardMatchProgressModel standardMatchProgressModel = dataLoader.getStandardMatchProgressModel();
    ConcurrencyData concurrencyData = getConcurrencyData(match.getContentId());

    Map<String, Integer> attributeId2TargetingTagMap = adModel.getAttributeId2TargetingTags();
    Map<String, List<AdSet>> adSetGroup = adModel.getAdSetGroup();
    List<AdSet> adSets = adSetGroup.getOrDefault(contentId, Collections.emptyList());


    BreakContext breakContext =
      dataProcessService.getBreakContext(totalBreakNumber, breakIndex, standardMatchProgressModel);

    List<DemandDiagnosis> demandDiagnosisList =
      dataProcessService.getDemandDiagnosisList(adSets, breakContext, adSetImpression);
    List<Response> responses = demandDiagnosisList.stream()
      .map(dataProcessService::convertFromDemand)
      .collect(Collectors.toList());

    return GeneralPlanContext.builder()
      .contentId(contentId)
      .concurrencyData(concurrencyData)
      .adSets(adSets)
      .attributeId2TargetingTagMap(attributeId2TargetingTagMap)
      .demandDiagnosisList(demandDiagnosisList)
      .responses(responses)
      .breakContext(breakContext)
      .breakDetails(breakDetails)
      .build();
  }

  private Integer getBreakIndex(String contentId) {
    return dataExchangerService.getBreakList(contentId).values().stream()
      .mapToInt(List::size)
      .max().orElse(0);
  }

  private ConcurrencyData getConcurrencyData(String contentId) {

    Map<String, StreamDefinition> streamDefinition = dataExchangerService.getStreamDefinition(contentId);

    List<ContentCohort> cohorts =
      getContentCohorts(contentId, streamDefinition);

    List<ContentStream> streams =
      getContentStreams(contentId, streamDefinition);

    return ConcurrencyData.builder()
      .cohorts(cohorts)
      .streams(streams)
      .build();
  }

  private List<ContentCohort> getContentCohorts(String contentId, Map<String, StreamDefinition> streamDefinition) {
    List<ContentCohort> cohorts = dataExchangerService.getContentCohortConcurrency(contentId, streamDefinition);
    IntStream.range(0, cohorts.size()).forEach(i -> cohorts.get(i).setConcurrencyId(i));
    return cohorts;
  }

  private List<ContentStream> getContentStreams(String contentId, Map<String, StreamDefinition> streamDefinition) {
    List<ContentStream> streams = dataExchangerService.getContentStreamConcurrency(contentId, streamDefinition);
    IntStream.range(0, streams.size()).forEach(i -> streams.get(i).setConcurrencyId(i));
    return streams;
  }


}
