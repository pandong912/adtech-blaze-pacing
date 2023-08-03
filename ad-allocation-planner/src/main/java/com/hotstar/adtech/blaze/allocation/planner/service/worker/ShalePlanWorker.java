package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleSupplyAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.ShaleSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.PlanInfo;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAdSetDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleDemandResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleSupplyResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSolver;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationExecutor;
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
  private final DiagnosisService diagnosisService;
  private final QualificationExecutor qualificationExecutor;
  private final ShaleSolver solver;

  @Timed(value = MetricNames.WORKER, extraTags = {"type", "shale"})
  public List<ShaleSolveResult> generatePlans(ShalePlanContext shalePlanContext, PlanType planType) {
    List<GraphContext> graphContexts =
      qualificationExecutor.doQualification(planType, shalePlanContext.getGeneralPlanContext());

    return graphContexts.parallelStream()
      .map(graphContext -> solveGraph(shalePlanContext, graphContext))
      .collect(Collectors.toList());
  }


  private ShaleSolveResult solveGraph(ShalePlanContext shalePlanContext, GraphContext graphContext) {
    GeneralPlanContext generalPlanContext = shalePlanContext.getGeneralPlanContext();
    ReachStorage reachStorage = shalePlanContext.getReachStorage();
    ShaleResult shaleResult = solver.solve(graphContext, reachStorage, shalePlanContext.getPenalty());

    List<DemandDiagnosis> demandDiagnosisList = generalPlanContext.getDemandDiagnosisList();

    List<ShaleAdSetDiagnosis> adSetDiagnoses =
      diagnosisService.getShaleAdSetDiagnosisList(demandDiagnosisList, shaleResult.getDemandResults());
    PlanInfo planInfo = diagnosisService.buildPlanInfo(graphContext, generalPlanContext.getBreakContext());
    ShaleAllocationPlan shaleAllocationPlan =
      buildShaleAllocationResult(generalPlanContext, graphContext, shaleResult);
    return ShaleSolveResult.builder()
      .shaleAllocationPlan(shaleAllocationPlan)
      .shaleAllocationDiagnosisDetail(ShaleAllocationDiagnosisDetail.builder()
        .adSetDiagnoses(adSetDiagnoses)
        .planInfo(planInfo)
        .build())
      .build();
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
      .breakTypeIds(graphContext.getBreakTypeGroup().getBreakTypeIds())
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
