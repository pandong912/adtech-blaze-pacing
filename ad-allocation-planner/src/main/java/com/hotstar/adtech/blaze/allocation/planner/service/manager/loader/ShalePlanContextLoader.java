package com.hotstar.adtech.blaze.allocation.planner.service.manager.loader;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.PLAN_DATA_LOAD;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.config.launchdarkly.BlazeDynamicConfig;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.DegradationReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor
@Slf4j
public class ShalePlanContextLoader {

  private final GeneralPlanContextLoader generalPlanContextLoader;
  private final DataExchangerService dataExchangerService;
  private final BlazeDynamicConfig blazeDynamicConfig;

  @Timed(value = PLAN_DATA_LOAD, extraTags = {"algorithm", "shale"})
  public ShalePlanContext getShalePlanContext(Match match, AdModel adModel) {
    GeneralPlanContext generalPlanContext = generalPlanContextLoader.getGeneralPlanContext(match, adModel);

    Map<String, Integer> concurrencyIdMap = generalPlanContext.getConcurrencyData().getCohorts()
      .stream()
      .filter(cohort -> cohort.getStreamType() == StreamType.SSAI_Spot)
      .collect(
        Collectors.toMap(ContentCohort::getKey, ContentCohort::getConcurrencyId, this::processDuplicateStreamKey));

    Map<Long, Integer> adSetIdMap = generalPlanContext.getAdSets().stream()
      .collect(Collectors.toMap(AdSet::getId, AdSet::getDemandId));
    return ShalePlanContext.builder()
      .generalPlanContext(generalPlanContext)
      .reachStorage(loadReach(match.getContentId(), concurrencyIdMap, adSetIdMap))
      .penalty(ShaleConstant.PENALTY)
      .build();
  }

  private Integer processDuplicateStreamKey(Integer a, Integer b) {
    log.error("Duplicate stream key found in shale plan, concurrency id is {},{}", a, b);
    return b;
  }

  ReachStorage loadReach(String contentId, Map<String, Integer> concurrencyIdMap,
                         Map<Long, Integer> adSetIdMap) {
    if (blazeDynamicConfig.getEnableMaximiseReach()) {
      return dataExchangerService.getUnReachRatio(contentId, concurrencyIdMap, adSetIdMap);
    } else {
      return new DegradationReachStorage();
    }
  }
}
