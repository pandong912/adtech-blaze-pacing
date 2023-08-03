package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.BUILD_FAILING;
import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_PLAN_UPDATE;

import com.hotstar.adtech.blaze.allocation.planner.config.launchdarkly.BlazeDynamicConfig;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
@Slf4j
public class AllocationPlanManager {

  private final HwmModeGenerator hwmModeGenerator;
  private final ShaleAndHwmModeGenerator shaleAndHwmModeGenerator;
  private final BlazeDynamicConfig blazeDynamicConfig;
  private final DataLoader dataLoader;

  @Scheduled(fixedRateString = "30000", initialDelayString = "5000")
  @Timed(value = MATCH_PLAN_UPDATE, histogram = true)
  public void generatePlan() {
    AdModel adModel = dataLoader.getAdModel();
    adModel.getMatches().values().parallelStream()
      .forEach(match -> generateAndUploadAllocationPlan(match, adModel));
  }

  private void generateAndUploadAllocationPlan(Match match, AdModel adModel) {
    try {
      doGeneratePlan(match, adModel);
    } catch (Exception e) {
      log.error("fail to build plan for content:" + match.getContentId(), e);
      Metrics.counter(BUILD_FAILING, "contentId", match.getContentId()).increment();
    }
  }

  private void doGeneratePlan(Match match, AdModel adModel) {
    if (blazeDynamicConfig.getEnableShale()) {
      shaleAndHwmModeGenerator.generateAndUploadAllocationPlan(match, adModel);
    } else {
      hwmModeGenerator.generateAndUploadAllocationPlan(match, adModel);
    }
  }
}
