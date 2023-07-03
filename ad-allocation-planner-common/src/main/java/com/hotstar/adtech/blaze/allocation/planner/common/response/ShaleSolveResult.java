package com.hotstar.adtech.blaze.allocation.planner.common.response;

import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.ShaleAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleSolveResult {
  ShaleAllocationPlan shaleAllocationPlan;
  ShaleAllocationDiagnosisDetail shaleAllocationDiagnosisDetail;
}
