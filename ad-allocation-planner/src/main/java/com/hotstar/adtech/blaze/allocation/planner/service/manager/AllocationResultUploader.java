package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResultDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllocationResultUploader {
  private final AllocationPlanClient allocationPlanClient;

  public List<AllocationPlanResultDetail> uploadShalePlan(AllocationPlanResult result,
                                                          List<ShaleAllocationPlan> shaleAllocationPlans) {

    return allocationPlanClient.batchUploadShalePlan(result.getPath(), shaleAllocationPlans).stream()
      .map(uploadResult -> AllocationPlanResultDetail.builder()
        .planType(uploadResult.getPlanType())
        .allocationPlanResult(result)
        .fileName(uploadResult.getFileName())
        .breakTypeIds(uploadResult.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
        .duration(uploadResult.getDuration())
        .totalBreakNumber(uploadResult.getTotalBreakNumber())
        .nextBreakIndex(uploadResult.getNextBreakIndex())
        .algorithmType(uploadResult.getAlgorithmType())
        .md5(uploadResult.getMd5())
        .build()).collect(Collectors.toList());
  }

  public List<AllocationPlanResultDetail> uploadHwmPlan(AllocationPlanResult result,
                                                        List<HwmAllocationPlan> hwmAllocationPlans) {
    return
      allocationPlanClient.batchUploadHwmPlan(result.getPath(), hwmAllocationPlans).stream()
        .map(uploadResult -> AllocationPlanResultDetail.builder()
          .planType(uploadResult.getPlanType())
          .allocationPlanResult(result)
          .fileName(uploadResult.getFileName())
          .breakTypeIds(uploadResult.getBreakTypeIds().stream().map(String::valueOf).collect(Collectors.joining(",")))
          .duration(uploadResult.getDuration())
          .totalBreakNumber(uploadResult.getTotalBreakNumber())
          .nextBreakIndex(uploadResult.getNextBreakIndex())
          .md5(uploadResult.getMd5())
          .algorithmType(uploadResult.getAlgorithmType())
          .build()).collect(Collectors.toList());
  }
}
