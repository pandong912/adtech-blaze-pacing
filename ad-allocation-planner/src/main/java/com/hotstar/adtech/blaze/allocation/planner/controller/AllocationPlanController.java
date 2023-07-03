package com.hotstar.adtech.blaze.allocation.planner.controller;

import com.hotstar.adtech.blaze.admodel.common.domain.StandardResponse;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentCohort;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.request.AllocationRequest;
import com.hotstar.adtech.blaze.allocation.planner.common.request.ShaleAllocationRequest;
import com.hotstar.adtech.blaze.allocation.planner.common.request.UnReachData;
import com.hotstar.adtech.blaze.allocation.planner.common.response.HwmSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.ShaleSolveResult;
import com.hotstar.adtech.blaze.allocation.planner.ingester.AdModelLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.DataProcessService;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.DemandDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.HwmPlanWorker;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.ShalePlanWorker;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.RedisReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocation.planner.source.context.BreakContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;
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

  @PostMapping("/hwm")
  public StandardResponse<List<HwmSolveResult>> generateHwmPlan(@RequestBody AllocationRequest allocationRequest) {
    GeneralPlanContext generalPlanContext = buildGeneralPlanContext(allocationRequest, Collections.emptyList());
    setConcurrencyId(generalPlanContext);
    List<HwmSolveResult> ssaiHwmSolveResults =
      hwmPlanWorker.generatePlans(generalPlanContext, allocationRequest.getPlanType());
    return StandardResponse.success(ssaiHwmSolveResults);
  }

  @PostMapping("/shale")
  public StandardResponse<List<ShaleSolveResult>> generateShalePlan(
    @RequestBody ShaleAllocationRequest shaleAllocationRequest) {
    GeneralPlanContext generalPlanContext = buildGeneralPlanContext(shaleAllocationRequest.getAllocationRequest(),
      shaleAllocationRequest.getReachAdSetIds());
    setConcurrencyId(generalPlanContext);
    Map<String, Integer> concurrencyIdMap = generalPlanContext.getConcurrencyData().getCohorts().stream()
      .collect(Collectors.toMap(ContentCohort::getKey, ContentCohort::getConcurrencyId));
    Map<Long, Integer> adSetIdMap =
      generalPlanContext.getAdSets().stream().collect(Collectors.toMap(AdSet::getId, AdSet::getDemandId));
    ReachStorage reachStorage = buildReachStorage(concurrencyIdMap, adSetIdMap, shaleAllocationRequest);
    ShalePlanContext shalePlanContext = ShalePlanContext.builder()
      .generalPlanContext(generalPlanContext)
      .reachStorage(reachStorage)
      .penalty(shaleAllocationRequest.getPenalty())
      .build();

    List<ShaleSolveResult> ssaiHwmSolveResults =
      shalePlanWorker.generatePlans(shalePlanContext, shaleAllocationRequest.getAllocationRequest().getPlanType());
    return StandardResponse.success(ssaiHwmSolveResults);
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
    return GeneralPlanContext.builder()
      .contentId(contentId)
      .concurrencyData(request.getConcurrencyData())
      .adSets(adSets)
      .attributeId2TargetingTagMap(adModel.getAttributeId2TargetingTags())
      .demandDiagnosisList(demandDiagnosisList)
      .responses(responses)
      .breakContext(breakContext)
      .breakDetails(request.getBreakDetails())
      .build();
  }

  private void setConcurrencyId(GeneralPlanContext generalPlanContext) {
    List<ContentStream> streams = generalPlanContext.getConcurrencyData().getStreams();
    List<ContentCohort> cohorts = generalPlanContext.getConcurrencyData().getCohorts();
    IntStream.range(0, streams.size()).forEach(i -> streams.get(i).setConcurrencyId(i));
    IntStream.range(0, cohorts.size()).forEach(i -> cohorts.get(i).setConcurrencyId(i));
  }
}
