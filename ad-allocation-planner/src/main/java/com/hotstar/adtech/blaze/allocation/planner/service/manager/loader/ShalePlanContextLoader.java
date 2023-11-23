package com.hotstar.adtech.blaze.allocation.planner.service.manager.loader;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.PLAN_DATA_LOAD;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.config.launchdarkly.BlazeDynamicConfig;
import com.hotstar.adtech.blaze.allocation.planner.ingester.ReachService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.DegradationReachStorage;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;
import com.hotstar.adtech.blaze.allocationdata.client.model.ShalePlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!sim && !worker")
public class ShalePlanContextLoader {

  private final GeneralPlanContextLoader generalPlanContextLoader;
  private final ReachService reachService;
  private final BlazeDynamicConfig blazeDynamicConfig;

  @Timed(value = PLAN_DATA_LOAD, extraTags = {"algorithm", "shale"})
  public Pair<Map<String, Integer>, ShalePlanContext> getShalePlanContext(Match match, AdModel adModel) {
    GeneralPlanContext generalPlanContext = generalPlanContextLoader.getGeneralPlanContext(match, adModel);

    Map<String, Integer> supplyIdMap = generalPlanContext.getConcurrencyData().getCohorts()
      .stream()
      .collect(Collectors.toMap(ContentCohort::getPlayoutIdKey, ContentCohort::getConcurrencyId,
        this::processDuplicateStreamKey));

    Map<Long, Integer> adSetIdToDemandId = generalPlanContext.getAdSets().stream()
      .filter(adSet -> adSet.getMaximizeReach() == 1)
      .collect(Collectors.toMap(AdSet::getId, AdSet::getDemandId));
    ShalePlanContext shalePlanContext = ShalePlanContext.builder()
      .generalPlanContext(generalPlanContext)
      .reachStorage(loadReach(match.getContentId(), supplyIdMap, adSetIdToDemandId))
      .penalty(ShaleConstant.PENALTY)
      .build();
    return Pair.of(supplyIdMap, shalePlanContext);
  }

  private Integer processDuplicateStreamKey(Integer a, Integer b) {
    log.error("Duplicate stream key found in shale plan, concurrency id is {},{}", a, b);
    return b;
  }

  ReachStorage loadReach(String contentId, Map<String, Integer> concurrencyIdMap,
                         Map<Long, Integer> adSetIdToDemandId) {
    if (blazeDynamicConfig.getEnableMaximiseReach()) {
      return reachService.getUnReachRatio(contentId, concurrencyIdMap, adSetIdToDemandId);
    } else {
      return new DegradationReachStorage();
    }
  }
}
