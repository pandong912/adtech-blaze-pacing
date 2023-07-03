package com.hotstar.adtech.blaze.allocation.planner.common.response.hwm;

import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.model.HwmAllocationDetail;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HwmAllocationPlan {
  private PlanType planType;
  private String contentId;
  private int nextBreakIndex;
  private int totalBreakNumber;
  private List<Integer> breakTypeIds;
  private int duration;
  private List<HwmAllocationDetail> hwmAllocationDetails;
}
