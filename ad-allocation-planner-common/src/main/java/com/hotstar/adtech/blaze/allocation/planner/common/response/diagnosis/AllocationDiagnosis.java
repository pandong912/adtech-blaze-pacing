package com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationDiagnosis {
  Instant version;
  String contentId;
  List<HwmAllocationDiagnosisDetail> hwmAllocationDiagnosisDetails;
  List<ShaleAllocationDiagnosisDetail> shaleAllocationDiagnosisDetails;
  ConcurrencyDiagnosis concurrencyDiagnosis;
}