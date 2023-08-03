package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Request {
  int concurrencyId;
  long concurrency;
}
