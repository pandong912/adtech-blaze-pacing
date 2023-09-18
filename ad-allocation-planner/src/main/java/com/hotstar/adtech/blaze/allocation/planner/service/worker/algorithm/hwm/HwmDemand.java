package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.Demand;
import com.hotstar.adtech.blaze.allocationdata.client.model.Response;
import lombok.Getter;

public class HwmDemand extends Demand {
  @Getter
  private final int order;
  @Getter
  private final Long adSetId;


  public HwmDemand(Response response) {
    super(response.getDemandId(), response.getDemand(), response.getAdDuration());
    this.adSetId = response.getAdSetId();
    this.order = response.getOrder();
  }
}
