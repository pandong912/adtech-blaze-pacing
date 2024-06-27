package com.hotstar.adtech.blaze.allocationplan.client;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.SupplyInfo;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public interface AllocationPlanClient {
  UploadResult uploadShalePlan(String path, ShaleAllocationPlan allocationPlan);

  UploadResult uploadHwmPlan(String path, HwmAllocationPlan allocationPlan);

  void uploadSupplyIdMap(String path, Map<String, Integer> supplyIdMap);

  Map<Long, ShaleAllocationPlan> loadShaleAllocationPlans(PlanType planType, List<LoadRequest> loadRequests);

  Map<Long, HwmAllocationPlan> loadHwmAllocationPlans(PlanType planType, List<LoadRequest> loadRequests);

  ShaleAllocationPlan loadShaleAllocationPlan(LoadRequest loadRequest);

  HwmAllocationPlan loadHwmAllocationPlan(LoadRequest loadRequest);

  SupplyInfo loadSupplyIdMap(String path);

}

