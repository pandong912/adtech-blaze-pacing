package com.hotstar.adtech.blaze.allocation.planner.controller;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.TargetingEvaluatorsProtocol;
import com.hotstar.adtech.blaze.allocation.planner.common.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.request.AllocationRequest;
import com.hotstar.adtech.blaze.allocation.planner.common.request.ShaleAllocationRequest;
import com.hotstar.adtech.blaze.allocation.planner.common.request.UnReachData;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.ingester.AdModelLoader;
import com.hotstar.adtech.blaze.allocation.planner.qualification.BreakTypeGroupFactory;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.DataProcessService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.HwmPlanWorker;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.ShalePlanWorker;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.BreakTypeGroup;
import com.hotstar.adtech.blaze.allocationdata.client.model.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;
import com.hotstar.adtech.blaze.allocationdata.client.model.RedisReachStorage;
import com.hotstar.adtech.blaze.allocationdata.client.model.RequestData;
import com.hotstar.adtech.blaze.allocationdata.client.model.Response;
import com.hotstar.adtech.blaze.allocationdata.client.model.ShalePlanContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("allocation-plan")
@RequiredArgsConstructor
public class AllocationPlanController {
  private final HwmPlanWorker hwmPlanWorker;
  private final ShalePlanWorker shalePlanWorker;
  private final AdModelLoader adModelLoader;
  private final DataProcessService dataProcessService;
  private final BreakTypeGroupFactory breakTypeGroupFactory;

  @PostMapping("/hwm")
  public StandardResponse<List<HwmAllocationPlan>> generateHwmPlan(@RequestBody AllocationRequest allocationRequest) {
    GeneralPlanContext generalPlanContext = buildGeneralPlanContext(allocationRequest, Collections.emptyList());
    setConcurrencyId(generalPlanContext);
    List<BreakTypeGroup> breakTypeList = generalPlanContext.getBreakTypeList();
    List<HwmAllocationPlan> hwmSolveResults = breakTypeList.stream()
      .flatMap(breakTypeGroup -> breakTypeGroup
        .getAllBreakDurations()
        .stream()
        .map(duration -> hwmPlanWorker.generatePlans(generalPlanContext, allocationRequest.getPlanType(),
          breakTypeGroup.getBreakTypeIds(), duration)))
      .collect(Collectors.toList());
    return StandardResponse.success(hwmSolveResults);
  }

  @PostMapping("/shale")
  public StandardResponse<ShaleResponse> generateShalePlan(
    @RequestBody ShaleAllocationRequest shaleAllocationRequest) {
    GeneralPlanContext generalPlanContext = buildGeneralPlanContext(shaleAllocationRequest.getAllocationRequest(),
      shaleAllocationRequest.getReachAdSetIds());
    setConcurrencyId(generalPlanContext);
    Map<String, Integer> concurrencyIdMap = generalPlanContext.getConcurrencyData().getCohorts().stream()
      .collect(Collectors.toMap(ContentCohort::getPlayoutIdKey, ContentCohort::getConcurrencyId));
    Map<Long, Integer> adSetIdMap =
      generalPlanContext.getAdSets().stream().collect(Collectors.toMap(AdSet::getId, AdSet::getReachIndex));
    ReachStorage reachStorage = buildReachStorage(concurrencyIdMap, adSetIdMap, shaleAllocationRequest);
    ShalePlanContext shalePlanContext = ShalePlanContext.builder()
      .generalPlanContext(generalPlanContext)
      .reachStorage(reachStorage)
      .penalty(shaleAllocationRequest.getPenalty())
      .build();

    List<BreakTypeGroup> breakTypeList = generalPlanContext.getBreakTypeList();
    List<ShaleAllocationPlan> shaleSolveResults = breakTypeList.stream()
      .flatMap(breakTypeGroup -> breakTypeGroup
        .getAllBreakDurations()
        .stream()
        .map(duration -> shalePlanWorker.generatePlans(shalePlanContext,
          shaleAllocationRequest.getAllocationRequest().getPlanType(),
          breakTypeGroup.getBreakTypeIds(), duration)))
      .collect(Collectors.toList());

    return StandardResponse.success(ShaleResponse.builder()
      .allocationPlans(shaleSolveResults)
      .concurrencyIdMap(concurrencyIdMap)
      .build());
  }


  private ReachStorage buildReachStorage(Map<String, Integer> concurrencyIdMap,
                                         Map<Long, Integer> adSetIdMap, ShaleAllocationRequest shaleAllocationRequest) {
    List<UnReachData> unReachDataList = shaleAllocationRequest.getUnReachDataList();
    double[][] unReachStore = new double[adSetIdMap.size()][concurrencyIdMap.size()];
    for (double[] row : unReachStore) {
      Arrays.fill(row, 1.0);
    }
    unReachDataList.stream()
      .filter(unReachData -> concurrencyIdMap.containsKey(unReachData.getKey()))
      .forEach(unReachData -> unReachData.getUnReachRatio().entrySet().stream()
        .filter(entry -> adSetIdMap.containsKey(entry.getKey()))
        .forEach(entry -> unReachStore[adSetIdMap.get(entry.getKey())][concurrencyIdMap.get(unReachData.getKey())] =
          entry.getValue()
        ));
    return new RedisReachStorage(unReachStore);
  }


  private GeneralPlanContext buildGeneralPlanContext(AllocationRequest request, Collection<Long> reachAdSetIds) {
    AdModel adModel = adModelLoader.loadAdModel(request.getAdModelVersion());
    String contentId = request.getContentId();
    List<AdSet> originAdSets = adModel.getAdSetGroup().getOrDefault(contentId, Collections.emptyList());
    List<AdSet> adSets = originAdSets.stream()
      .map(adSet -> adSet
        .toBuilder()
        .maximizeReach(reachAdSetIds.contains(adSet.getId()) ? 1 : 0)
        .build())
      .collect(Collectors.toList());
    BreakContext breakContext =
      dataProcessService.getBreakContext(request.getTotalBreakNumber(), request.getCurrentBreakIndex(),
        new StandardMatchProgressModel(request.getMatchBreakProgressRatios()));
    List<DemandDiagnosis> demandDiagnosisList =
      dataProcessService.getDemandDiagnosisList(adSets, breakContext, request.getAdSetImpressions());
    List<Response> responses = demandDiagnosisList.stream()
      .map(dataProcessService::convertFromDemand)
      .collect(Collectors.toList());

    RequestData requestData = new RequestData(request.getConcurrencyData());
    TargetingEvaluatorsProtocol targetingEvaluators = adModel.getTargetingEvaluatorsMap().getOrDefault(contentId,
      TargetingEvaluatorsProtocol.EMPTY);
    List<BreakTypeGroup> breakTypeList =
      breakTypeGroupFactory.getBreakTypeList(targetingEvaluators.getBreakTargeting(), request.getBreakDetails());

    return GeneralPlanContext.builder()
      .contentId(contentId)
      .concurrencyData(request.getConcurrencyData())
      .adSets(adSets)
      .attributeId2TargetingTagMap(adModel.getAttributeId2TargetingTags())
      .demandDiagnosisList(demandDiagnosisList)
      .responses(responses)
      .breakContext(breakContext)
      .requestData(requestData)
      .breakTypeList(breakTypeList)
      .targetingEvaluators(targetingEvaluators)
      .build();
  }

  private void setConcurrencyId(
    GeneralPlanContext generalPlanContext) {
    List<ContentStream> streams = generalPlanContext.getConcurrencyData().getStreams();
    List<ContentCohort> cohorts = generalPlanContext.getConcurrencyData().getCohorts();
    IntStream.range(0, streams.size()).forEach(i -> streams.get(i).setConcurrencyId(i, cohorts.size()));
    IntStream.range(0, cohorts.size()).forEach(i -> cohorts.get(i).setConcurrencyId(i));
  }
}
