package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.Supply;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString(callSuper = true)
public class ShaleSupply extends Supply {
  @Setter
  private double beta;
  private final StreamType streamType;

  public ShaleSupply(Request request, int breakDuration) {
    super(request.getConcurrencyId(), breakDuration, request.getConcurrency());
    this.streamType = request.getStreamType();
  }
}
