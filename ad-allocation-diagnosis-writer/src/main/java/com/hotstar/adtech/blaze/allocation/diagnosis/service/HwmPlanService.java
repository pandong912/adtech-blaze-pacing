package com.hotstar.adtech.blaze.allocation.diagnosis.service;

import static com.hotstar.adtech.blaze.allocation.diagnosis.metric.MetricNames.ALLOCATION_PLAN;

import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationAdSet;
import com.hotstar.adtech.blaze.allocation.diagnosis.model.AllocationPlan;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AllocationAdSetSink;
import com.hotstar.adtech.blaze.allocation.diagnosis.sink.AllocationPlanSink;
import com.hotstar.adtech.blaze.allocation.planner.common.model.HwmAllocationDetail;
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

    hwmPlans
      .forEach(detail -> processPlan(contentId, result, generalPlanContext, detail));
    log.info("hwm plan size:{}", hwmPlans.size());
  }

  @Timed(ALLOCATION_PLAN)
  public void processPlan(String contentId, AllocationPlanResult result,
                          GeneralPlanContext generalPlanContext,
                          AllocationPlanResultDetail detail) {
    try {
      // write allocationPlan to clickhouse
      AllocationPlan allocationPlan =
        AllocationBuilder.getAllocationPlan(contentId, result, generalPlanContext, detail);
      allocationPlanSink.writePlan(allocationPlan);

      // write allocationAdSet to clickhouse
      LoadRequest loadRequest = buildLoadRequest(detail, result.getPath());
      Map<Long, HwmAllocationDetail> resultMap = allocationPlanClient.loadHwmAllocationPlan(loadRequest)
        .getHwmAllocationDetails()
        .stream()
        .collect(Collectors.toMap(HwmAllocationDetail::getAdSetId, Function.identity()));

      List<AllocationAdSet> allocationAdSets = generalPlanContext.getDemandDiagnosisList().stream()
        .map(demandDiagnosis -> buildAllocationAdSet(demandDiagnosis, resultMap, contentId, result, detail))
        .collect(Collectors.toList());
      allocationAdSetSink.writeAdSet(allocationAdSets);
    } catch (Exception e) {
      log.error("write HWM plan to clickhouse error", e);
    }
  }


  private AllocationAdSet buildAllocationAdSet(DemandDiagnosis demandDiagnosis,
                                               Map<Long, HwmAllocationDetail> resultMap, String contentId,
                                               AllocationPlanResult result, AllocationPlanResultDetail detail) {
    HwmAllocationDetail hwmAllocationDetail = resultMap.computeIfAbsent(demandDiagnosis.getAdSetId(),
      (id) -> HwmAllocationDetail.builder().build());
    return AllocationAdSet.builder()
      .version(result.getVersion())
      .demand(demandDiagnosis.getDemand())
      .adSetId(demandDiagnosis.getAdSetId())
      .order(demandDiagnosis.getOrder())
      .planId(detail.getId())
      .probability(hwmAllocationDetail.getProbability())
      .campaignId(demandDiagnosis.getCampaignId())
      .siMatchId(contentId)
      .delivered(demandDiagnosis.getDelivered())
      .target(demandDiagnosis.getTarget())
      .theta(0d)
      .mean(0d)
      .sigma(0d)
      .alpha(0d)
      .build();
  }


  private LoadRequest buildLoadRequest(AllocationPlanResultDetail detail, String path) {
    return LoadRequest.builder()
      .algorithmType(detail.getAlgorithmType())
      .fileName(detail.getFileName())
      .path(path)
      .build();
  }

}
