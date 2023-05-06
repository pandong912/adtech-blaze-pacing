package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Response {
  long adSetId;
  int demandId;
  int order;
  long target;
  long delivered;
  double demand;
  int adDuration;
  int maximizeReach;
}
