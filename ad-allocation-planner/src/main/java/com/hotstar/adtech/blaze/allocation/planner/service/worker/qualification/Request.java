package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Request {
  // concurrencyId should be sames as the index of the List<Request>!
  int concurrencyId;
  long concurrency;
  StreamType streamType;
}
