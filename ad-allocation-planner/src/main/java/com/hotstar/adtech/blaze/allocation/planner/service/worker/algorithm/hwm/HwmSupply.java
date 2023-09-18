package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.Supply;
import com.hotstar.adtech.blaze.allocationdata.client.model.Request;

public class HwmSupply extends Supply {

  public HwmSupply(Request request, int breakDuration) {
    super(request.getConcurrencyId(), breakDuration, request.getConcurrency());
  }

}
