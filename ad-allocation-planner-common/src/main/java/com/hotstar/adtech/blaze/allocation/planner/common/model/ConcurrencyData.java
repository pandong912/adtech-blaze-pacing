package com.hotstar.adtech.blaze.allocation.planner.common.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConcurrencyData {
  List<ContentCohort> cohorts;
  List<ContentStream> streams;
}