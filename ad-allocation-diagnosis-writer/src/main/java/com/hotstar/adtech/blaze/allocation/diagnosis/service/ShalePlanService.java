package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.ALLOCATION_PLAN;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationAdSet;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationPlan;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AllocationAdSetSink;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AllocationPlanSink;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocationdata.client.AllocationDataClient;
import com.hotstar.adtech.blaze.allocationdata.client.model.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShalePlanService {
  private final AllocationPlanClient allocationPlanClient;
  private final AllocationDataClient allocationDataClient;
  private final AllocationPlanSink allocationPlanSink;
  private final AllocationAdSetSink allocationAdSetSink;

  public void writeClickhouse(AllocationPlanResult result, List<AllocationPlanResultDetail> shalePlans) {
    if (shalePlans.isEmpty()) {
      return;
    }
    String contentId = result.getContentId();
    GeneralPlanContext generalPlanContext =
      allocationDataClient.loadShaleData(result.getPath()).getGeneralPlanContext();
    List<LoadRequest> loadRequests = shalePlans.stream()
      .map(shalePlan -> buildLoadRequest(shalePlan, result.getPath()))
      .collect(Collectors.toList());

    Map<Long, ShaleAllocationPlan> plans = allocationPlanClient.loadShaleAllocationPlans(PlanType.SSAI, loadRequests);
    plans.forEach((planId, plan) -> processPlan(contentId, result, generalPlanContext, planId, plan));

    log.info("shale plan size:{}", shalePlans.size());
  }

  @Timed(ALLOCATION_PLAN)
  public void processPlan(String contentId, AllocationPlanResult result,
                          GeneralPlanContext generalPlanContext, Long planId, ShaleAllocationPlan plan) {
    try {
      // write allocationPlan to clickhouse
      AllocationPlan allocationPlan =
        AllocationBuilder.getShaleAllocationPlan(contentId, result, generalPlanContext, planId, plan);
      allocationPlanSink.writePlan(allocationPlan);

      // write allocationAdSet to clickhouse
      Map<Long, ShaleAllocationDetail> resultMap = plan
        .getShaleAllocationDetails()
        .stream()
        .collect(Collectors.toMap(ShaleAllocationDetail::getAdSetId, Function.identity()));

      List<AllocationAdSet> allocationAdSets = generalPlanContext.getDemandDiagnosisList().stream()
        .map(demandDiagnosis -> buildAllocationAdSet(demandDiagnosis, resultMap, contentId, result, planId))
        .collect(Collectors.toList());
      allocationAdSetSink.writeAdSet(allocationAdSets);
    } catch (Exception e) {
      log.error("write SHALE plan to clickhouse error", e);
    }
  }

  private AllocationAdSet buildAllocationAdSet(DemandDiagnosis demandDiagnosis,
                                               Map<Long, ShaleAllocationDetail> resultMap, String contentId,
                                               AllocationPlanResult result, Long planId) {
    ShaleAllocationDetail shaleAllocationDetail =
      resultMap.computeIfAbsent(demandDiagnosis.getAdSetId(), k -> ShaleAllocationDetail.builder().build());
    return AllocationAdSet.builder()
      .version(result.getVersion())
      .demand(demandDiagnosis.getDemand())
      .adSetId(demandDiagnosis.getAdSetId())
      .order(demandDiagnosis.getOrder())
      .planId(planId)
      .probability(-1d)
      .alpha(shaleAllocationDetail.getAlpha())
      .sigma(shaleAllocationDetail.getSigma())
      .mean(shaleAllocationDetail.getMean())
      .theta(shaleAllocationDetail.getTheta())
      .campaignId(demandDiagnosis.getCampaignId())
      .siMatchId(contentId)
      .delivered(demandDiagnosis.getDelivered())
      .target(demandDiagnosis.getTarget())
      .build();
  }


  private LoadRequest buildLoadRequest(AllocationPlanResultDetail detail, String path) {
    return LoadRequest.builder()
      .algorithmType(detail.getAlgorithmType())
      .fileName(detail.getFileName())
      .path(path)
      .planType(detail.getPlanType())
      .planId(detail.getId())
      .build();
  }

}
