package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultDetailRepository;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.HwmAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleSupplyAllocationDetail;
import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import com.hotstar.adtech.blaze.exchanger.api.entity.AllocationPlanDetail;
import com.hotstar.adtech.blaze.exchanger.api.entity.ShaleResultDetail;
import com.hotstar.adtech.blaze.exchanger.api.response.AllocationPlanResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.AllocationPlanUriResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.HwmAllocationPlanResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.ShaleAllocationPlanResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationPlanService {

  private final AllocationPlanClient allocationPlanClient;

  private final AllocationPlanResultRepository allocationPlanResultRepository;

  private final AllocationPlanResultDetailRepository allocationPlanResultDetailRepository;


  public Optional<AllocationPlanUriResponse> getAllocationPlanUri(String contentId, Long version) {
    return allocationPlanResultRepository
      .findFirstByContentIdAndVersionGreaterThanOrderByVersionDesc(contentId, Instant.ofEpochMilli(version))
      .map(this::buildAllocationPlanUriResponse);
  }

  private AllocationPlanUriResponse buildAllocationPlanUriResponse(AllocationPlanResult allocationPlanResult) {
    List<AllocationPlanDetail> allocationPlanDetails =
      allocationPlanResultDetailRepository.findAllByAllocationPlanResultId(allocationPlanResult.getId())
        .stream()
        .map(this::buildAllocationPlanDetailResponse)
        .collect(Collectors.toList());

    return AllocationPlanUriResponse.builder()
      .version(allocationPlanResult.getVersion().toEpochMilli())
      .path(allocationPlanResult.getPath())
      .allocationPlanDetails(allocationPlanDetails)
      .build();
  }

  private AllocationPlanDetail buildAllocationPlanDetailResponse(
    AllocationPlanResultDetail allocationPlanResultDetail) {
    return AllocationPlanDetail.builder()
      .planType(allocationPlanResultDetail.getPlanType())
      .duration(allocationPlanResultDetail.getDuration())
      .fileName(allocationPlanResultDetail.getFileName())
      .md5(allocationPlanResultDetail.getMd5())
      .breakTypeIds(allocationPlanResultDetail.getBreakTypeIds())
      .totalBreakNumber(allocationPlanResultDetail.getTotalBreakNumber())
      .nextBreakIndex(allocationPlanResultDetail.getNextBreakIndex())
      .planId(allocationPlanResultDetail.getId())
      .algorithmType(allocationPlanResultDetail.getAlgorithmType())
      .build();
  }

  public Optional<AllocationPlanResponse> getAllocationPlanResult(String contentId, Long version) {

    return allocationPlanResultRepository
      .findByContentIdAndVersion(contentId, Instant.ofEpochMilli(version))
      .map(this::buildLoadRequests)
      .map(this::loadAllocationPlan);
  }

  private List<LoadRequest> buildLoadRequests(AllocationPlanResult allocationPlanResult) {
    return allocationPlanResult.getAllocationPlanResultDetails().stream()
      .map(allocationPlanResultDetail -> LoadRequest.builder()
        .path(allocationPlanResult.getPath())
        .fileName(allocationPlanResultDetail.getFileName())
        .algorithmType(allocationPlanResultDetail.getAlgorithmType())
        .build())
      .collect(Collectors.toList());
  }

  private AllocationPlanResponse loadAllocationPlan(List<LoadRequest> loadRequests) {
    Map<AlgorithmType, List<LoadRequest>> algorithmTypeListMap =
      loadRequests.stream().collect(Collectors.groupingBy(LoadRequest::getAlgorithmType));
    List<HwmAllocationPlanResponse> hwmAllocationPlanResponses = allocationPlanClient
      .loadHwmAllocationPlans(algorithmTypeListMap.get(AlgorithmType.HWM))
      .stream()
      .map(allocationPlan -> HwmAllocationPlanResponse.builder()
        .contentId(allocationPlan.getContentId())
        .nextBreak(allocationPlan.getNextBreakIndex())
        .totalBreaks(allocationPlan.getTotalBreakNumber())
        .breakTypeIds(allocationPlan.getBreakTypeIds())
        .planType(allocationPlan.getPlanType())
        .duration(allocationPlan.getDuration())
        .allocationResults(allocationPlan.getHwmAllocationDetails().stream()
          .collect(Collectors.toMap(HwmAllocationDetail::getAdSetId, HwmAllocationDetail::getProbability)))
        .build())
      .collect(Collectors.toList());
    List<ShaleAllocationPlanResponse> shaleAllocationPlanResponses = allocationPlanClient
      .loadShaleAllocationPlans(algorithmTypeListMap.get(AlgorithmType.SHALE))
      .stream()
      .map(allocationPlan -> ShaleAllocationPlanResponse.builder()
        .contentId(allocationPlan.getContentId())
        .nextBreak(allocationPlan.getNextBreakIndex())
        .totalBreaks(allocationPlan.getTotalBreakNumber())
        .duration(allocationPlan.getDuration())
        .results(allocationPlan.getShaleAllocationDetails().stream().map(this::getDetail).collect(Collectors.toList()))
        .cohortAllocationMap(allocationPlan.getShaleSupplyAllocationDetails().stream()
          .collect(Collectors.toMap(ShaleSupplyAllocationDetail::getId, ShaleSupplyAllocationDetail::getBeta)))
        .build())
      .collect(Collectors.toList());
    return AllocationPlanResponse.builder()
      .shaleAllocationPlanResponses(shaleAllocationPlanResponses)
      .hwmAllocationPlanResponses(hwmAllocationPlanResponses)
      .build();
  }

  private ShaleResultDetail getDetail(ShaleAllocationDetail shaleAllocationDetail) {
    return ShaleResultDetail.builder()
      .adSetId(shaleAllocationDetail.getAdSetId())
      .alpha(shaleAllocationDetail.getAlpha())
      .theta(shaleAllocationDetail.getTheta())
      .sigma(shaleAllocationDetail.getSigma())
      .mean(shaleAllocationDetail.getMean())
      .std(shaleAllocationDetail.getStd())
      .build();
  }
}
