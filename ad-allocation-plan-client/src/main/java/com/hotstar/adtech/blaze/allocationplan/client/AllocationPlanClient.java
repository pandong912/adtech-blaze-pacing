package com.hotstar.adtech.blaze.allocationplan.client;

import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.AllocationDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import java.util.List;

@SuppressWarnings("unused")
public interface AllocationPlanClient {
  UploadResult uploadShalePlan(String path, ShaleAllocationPlan allocationPlan);

  List<UploadResult> batchUploadShalePlan(String path, List<ShaleAllocationPlan> allocationPlans);

  UploadResult uploadHwmPlan(String path, HwmAllocationPlan allocationPlan);

  List<UploadResult> batchUploadHwmPlan(String path, List<HwmAllocationPlan> allocationPlans);

  List<ShaleAllocationPlan> loadShaleAllocationPlans(List<LoadRequest> loadRequests);

  List<HwmAllocationPlan> loadHwmAllocationPlans(List<LoadRequest> loadRequests);

  ShaleAllocationPlan loadShaleAllocationPlan(LoadRequest loadRequest);

  HwmAllocationPlan loadHwmAllocationPlan(LoadRequest loadRequest);

  void uploadAllocationDiagnosis(AllocationDiagnosis allocationDiagnosis);

  AllocationDiagnosis loadAllocationDiagnosis(String path);
}

