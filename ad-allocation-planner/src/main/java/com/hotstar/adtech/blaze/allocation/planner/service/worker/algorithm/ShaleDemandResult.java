package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleDemandResult {
  long id;
  double mean;
  double alpha;
  double theta;
  double sigma;
  int reachEnabled;
  int adDuration;
}
