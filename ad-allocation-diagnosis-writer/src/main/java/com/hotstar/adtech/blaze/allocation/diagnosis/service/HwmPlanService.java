package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.ALLOCATION_PLAN;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationAdSet;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationPlan;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AllocationAdSetSink;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AllocationPlanSink;
import com.hotstar.adtech.blaze.allocation.planner.common.model.HwmAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
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
public class HwmPlanService {
  private final AllocationPlanClient allocationPlanClient;
  private final AllocationDataClient allocationDataClient;
  private final AllocationPlanSink allocationPlanSink;
  private final AllocationAdSetSink allocationAdSetSink;

  public void writeClickhouse(AllocationPlanResult result, List<AllocationPlanResultDetail> hwmPlans) {
    String[] split = result.getPath().split("/");
    String contentId = split[1];
    String versionString = split[2];
    GeneralPlanContext generalPlanContext = allocationDataClient.loadHwmData(contentId, versionString);
    List<LoadRequest> loadRequests = hwmPlans.stream()
      .map(hwmPlan -> buildLoadRequest(hwmPlan, result.getPath()))
      .collect(Collectors.toList());
    Map<Long, HwmAllocationPlan> ssaiPlans = allocationPlanClient.loadHwmAllocationPlans(PlanType.SSAI, loadRequests);
    Map<Long, HwmAllocationPlan> spotPlans = allocationPlanClient.loadHwmAllocationPlans(PlanType.SPOT, loadRequests);

    ssaiPlans.forEach((planId, plan) -> processPlan(contentId, result, generalPlanContext, planId, plan));
    spotPlans.forEach((planId, plan) -> processPlan(contentId, result, generalPlanContext, planId, plan));
    log.info("hwm plan size:{}", hwmPlans.size());
  }

  @Timed(ALLOCATION_PLAN)
  public void processPlan(String contentId, AllocationPlanResult result,
                          GeneralPlanContext generalPlanContext, Long planId, HwmAllocationPlan plan) {
    try {
      // write allocationPlan to clickhouse
      AllocationPlan allocationPlan =
        AllocationBuilder.getHwmAllocationPlan(contentId, result, generalPlanContext, planId, plan);
      allocationPlanSink.writePlan(allocationPlan);

      // write allocationAdSet to clickhouse
      Map<Long, HwmAllocationDetail> resultMap = plan
        .getHwmAllocationDetails()
        .stream()
        .collect(Collectors.toMap(HwmAllocationDetail::getAdSetId, Function.identity()));

      List<AllocationAdSet> allocationAdSets = generalPlanContext.getDemandDiagnosisList().stream()
        .map(demandDiagnosis -> buildAllocationAdSet(demandDiagnosis, resultMap, contentId, result, planId))
        .collect(Collectors.toList());
      allocationAdSetSink.writeAdSet(allocationAdSets);
    } catch (Exception e) {
      log.error("write HWM plan to clickhouse error", e);
    }
  }


  private AllocationAdSet buildAllocationAdSet(DemandDiagnosis demandDiagnosis,
                                               Map<Long, HwmAllocationDetail> resultMap, String contentId,
                                               AllocationPlanResult result, Long planId) {
    HwmAllocationDetail hwmAllocationDetail = resultMap.computeIfAbsent(demandDiagnosis.getAdSetId(),
      (id) -> HwmAllocationDetail.builder().build());
    return AllocationAdSet.builder()
      .version(result.getVersion())
      .demand(demandDiagnosis.getDemand())
      .adSetId(demandDiagnosis.getAdSetId())
      .order(demandDiagnosis.getOrder())
      .planId(planId)
      .probability(hwmAllocationDetail.getProbability())
      .campaignId(demandDiagnosis.getCampaignId())
      .siMatchId(contentId)
      .delivered(demandDiagnosis.getDelivered())
      .target(demandDiagnosis.getTarget())
      .theta(-1d)
      .mean(-1d)
      .sigma(-1d)
      .alpha(-1d)
      .build();
  }


  private LoadRequest buildLoadRequest(AllocationPlanResultDetail detail, String path) {
    return LoadRequest.builder()
      .algorithmType(detail.getAlgorithmType())
      .fileName(detail.getFileName())
      .path(path)
      .planId(detail.getId())
      .planType(detail.getPlanType())
      .build();
  }

}
