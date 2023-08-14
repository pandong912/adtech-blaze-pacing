package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.WORKER_RESULT_UPLOAD;

import com.hotstar.adtech.blaze.admodel.repository.model.AllocationPlanResult;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocationplan.client.AllocationPlanClient;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllocationPlanResultService {
  private final AllocationPlanClient allocationPlanClient;

  @Timed(value = WORKER_RESULT_UPLOAD, extraTags = {"type", "shale"})
  public UploadResult uploadShalePlan(AllocationPlanResult result,
                                      ShaleAllocationPlan shaleAllocationPlans) {
    return allocationPlanClient.uploadShalePlan(result.getPath(), shaleAllocationPlans);
  }

  @Timed(value = WORKER_RESULT_UPLOAD, extraTags = {"type", "hwm"})
  public UploadResult uploadHwmPlan(AllocationPlanResult result, HwmAllocationPlan hwmAllocationPlans) {
    return allocationPlanClient.uploadHwmPlan(result.getPath(), hwmAllocationPlans);
  }

}
