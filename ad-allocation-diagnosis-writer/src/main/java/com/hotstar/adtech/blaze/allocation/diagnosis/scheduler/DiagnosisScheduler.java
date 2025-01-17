package com.hotstar.adtech.blaze.allocation.diagnosis.scheduler;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.TaskStatus;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultDetailRepository;
import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.admodel.repository.AllocationSyncPointRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationSyncPoint;
import com.hotstar.adtech.blaze.allocation.diagnosis.service.AllocationConcurrencyService;
import com.hotstar.adtech.blaze.allocation.diagnosis.service.HwmPlanService;
import com.hotstar.adtech.blaze.allocation.diagnosis.service.ShalePlanService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosisScheduler {
  private final HwmPlanService hwmPlanService;
  private final ShalePlanService shalePlanService;
  private final AllocationConcurrencyService allocationConcurrencyService;
  private final AllocationSyncPointRepository allocationSyncPointRepository;
  private final AllocationPlanResultRepository allocationPlanResultRepository;
  private final AllocationPlanResultDetailRepository allocationPlanResultDetailRepository;

  @Scheduled(fixedDelayString = "10000")
  public void update() {
    AllocationSyncPoint syncPoint = allocationSyncPointRepository
      .findFirstByOrderByIdDesc()
      .orElseGet(this::buildDefaultSyncPoint);
    List<AllocationPlanResult> rawAllocationPlanResults =
      allocationPlanResultRepository.findFirst20ByIdGreaterThanOrderById(syncPoint.getAllocationPlanResultId());
    List<AllocationPlanResult> finishedAllocationPlanResults =
      findFinishedAllocationPlanResults(rawAllocationPlanResults);
    List<AllocationPlanResult> successAllocationPlanResults = finishedAllocationPlanResults.stream()
      .filter((result) -> result.getTaskStatus() == TaskStatus.SUCCESS)
      .toList();
    Long maxId = finishedAllocationPlanResults.stream()
      .mapToLong(AllocationPlanResult::getId)
      .max()
      .orElse(syncPoint.getAllocationPlanResultId());
    if (maxId <= syncPoint.getAllocationPlanResultId()) {
      return;
    }
    // write to clickhouse
    successAllocationPlanResults
      .parallelStream()
      .forEach(this::write);
    // save progress
    AllocationSyncPoint newSyncPoint = AllocationSyncPoint.builder()
      .allocationPlanResultId(maxId)
      .build();
    allocationSyncPointRepository.save(newSyncPoint);
    log.info("update allocation sync point to {}", maxId);
  }

  private List<AllocationPlanResult> findFinishedAllocationPlanResults(
    List<AllocationPlanResult> rawAllocationPlanResults) {
    List<AllocationPlanResult> finishedAllocationPlanResults = new ArrayList<>();
    for (AllocationPlanResult result : rawAllocationPlanResults) {
      if (!result.getTaskStatus().isFinished()
        && result.getCreatedAt().isAfter(Instant.now().minus(15, ChronoUnit.MINUTES))) {
        break;
      }
      finishedAllocationPlanResults.add(result);
    }
    return finishedAllocationPlanResults;
  }

  private AllocationSyncPoint buildDefaultSyncPoint() {
    return AllocationSyncPoint.builder()
      .allocationPlanResultId(-1L)
      .build();
  }

  private void write(AllocationPlanResult result) {
    List<AllocationPlanResultDetail> allocationPlanResultDetails =
      allocationPlanResultDetailRepository.findAllByAllocationPlanResultId(result.getId());
    List<AllocationPlanResultDetail> hwmPlans =
      allocationPlanResultDetails.stream().filter(detail -> AlgorithmType.HWM.equals(detail.getAlgorithmType()))
        .collect(Collectors.toList());
    List<AllocationPlanResultDetail> shalePlans =
      allocationPlanResultDetails.stream().filter(detail -> AlgorithmType.SHALE.equals(detail.getAlgorithmType()))
        .collect(Collectors.toList());
    allocationConcurrencyService.writeConcurrency(result);
    hwmPlanService.writeClickhouse(result, hwmPlans);
    shalePlanService.writeClickhouse(result, shalePlans);
  }
}
