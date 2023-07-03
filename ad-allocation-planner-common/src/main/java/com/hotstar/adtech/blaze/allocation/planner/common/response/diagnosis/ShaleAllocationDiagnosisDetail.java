package com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleAllocationDiagnosisDetail {
  PlanInfo planInfo;
  List<ShaleAdSetDiagnosis> adSetDiagnoses;
}
