package com.hotstar.adtech.blaze.allocation.planner.common.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShaleAllocationDetail {
  long adSetId;
  double alpha;
  double theta;
  double sigma;
  double mean;
  double std;
  int reachEnabled;
  int adDuration;
}
