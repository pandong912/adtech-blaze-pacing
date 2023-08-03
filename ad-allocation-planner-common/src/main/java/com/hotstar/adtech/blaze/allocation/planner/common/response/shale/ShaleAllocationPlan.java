package com.hotstar.adtech.blaze.allocation.planner.common.response.shale;

import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleAllocationDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ShaleSupplyAllocationDetail;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShaleAllocationPlan {
  private String contentId;
  private List<Integer> breakTypeIds;
  private int nextBreakIndex;
  private int totalBreakNumber;
  private int duration;
  private List<ShaleAllocationDetail> shaleAllocationDetails;
  private List<ShaleSupplyAllocationDetail> shaleSupplyAllocationDetails;
}
