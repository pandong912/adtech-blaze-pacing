package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleSupplyAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleDemandResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleSupplyResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSolver;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SpotQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.SsaiQualificationExecutor;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShalePlanWorker {
  private final SsaiQualificationExecutor ssaiQualificationExecutor;
  private final SpotQualificationExecutor spotQualificationExecutor;
  private final ShaleSolver solver;

  @Timed(value = MetricNames.PLAN_WORKER, extraTags = {"type", "shale"})
  public ShaleAllocationPlan generatePlans(ShalePlanContext shalePlanContext, PlanType planType,
                                           List<Integer> breakTypeIds, Integer duration) {
    GraphContext graphContext = planType == PlanType.SSAI
      ? ssaiQualificationExecutor.executeQualify(shalePlanContext.getGeneralPlanContext(), breakTypeIds, duration) :
      spotQualificationExecutor.executeQualify(shalePlanContext.getGeneralPlanContext(), breakTypeIds, duration);
    GeneralPlanContext generalPlanContext = shalePlanContext.getGeneralPlanContext();
    ReachStorage reachStorage = shalePlanContext.getReachStorage();
    ShaleResult shaleResult = solver.solve(graphContext, reachStorage, shalePlanContext.getPenalty());
    return buildShaleAllocationResult(generalPlanContext, graphContext, shaleResult);
  }

  private ShaleAllocationDetail buildShaleAllocationDetail(ShaleDemandResult shaleDemandResult) {
    return ShaleAllocationDetail.builder()
      .adSetId(shaleDemandResult.getId())
      .sigma(shaleDemandResult.getSigma())
      .mean(shaleDemandResult.getMean())
      .alpha(shaleDemandResult.getAlpha())
      .theta(shaleDemandResult.getTheta())
      .reachEnabled(shaleDemandResult.getReachEnabled())
      .adDuration(shaleDemandResult.getAdDuration())
      .build();
  }

  public ShaleAllocationPlan buildShaleAllocationResult(GeneralPlanContext generalPlanContext,
                                                        GraphContext graphContext,
                                                        ShaleResult shaleResult) {
    String contentId = generalPlanContext.getContentId();
    BreakContext breakContext = generalPlanContext.getBreakContext();
    return ShaleAllocationPlan.builder()
      .contentId(contentId)
      .breakTypeIds(graphContext.getBreakTypeIds())
      .nextBreakIndex(breakContext.getNextBreakIndex())
      .totalBreakNumber(breakContext.getTotalBreakNumber())
      .duration(graphContext.getBreakDuration())
      .shaleAllocationDetails(
        shaleResult.getDemandResults().stream()
          .map(this::buildShaleAllocationDetail)
          .collect(Collectors.toList()))
      .shaleSupplyAllocationDetails(
        shaleResult.getSupplyResults().stream()
          .map(this::buildShaleSupplyAllocationDetail)
          .collect(Collectors.toList()))
      .build();
  }

  private ShaleSupplyAllocationDetail buildShaleSupplyAllocationDetail(ShaleSupplyResult shaleSupplyResult) {
    return ShaleSupplyAllocationDetail.builder()
      .beta(shaleSupplyResult.getBeta())
      .id(shaleSupplyResult.getId())
      .build();
  }
}
