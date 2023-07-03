package com.hotstar.adtech.blaze.allocation.planner.common.response;

import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.HwmAllocationDiagnosisDetail;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HwmSolveResult {
  HwmAllocationPlan hwmAllocationPlan;
  HwmAllocationDiagnosisDetail hwmAllocationDiagnosisDetail;

}
