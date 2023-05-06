package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.MATCH_PLAN_UPDATE;

import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
public class AllocationPlanManager {

  private final AllocationPlanGenerator allocationPlanGenerator;
  private final DataLoader dataLoader;

  @Scheduled(fixedDelayString = "30000", initialDelayString = "5000")
  @Timed(value = MATCH_PLAN_UPDATE, histogram = true)
  public void generatePlan() {
    AdModel adModel = dataLoader.getAdModel();
    adModel.getMatches().values().parallelStream()
      .forEach(match -> allocationPlanGenerator.generateAndUploadAllocationPlan(match, adModel));
  }
}
