package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.Demand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class HwmDemand extends Demand {
  @Getter
  private final int order;
  @Getter
  private final Long adSetId;

  @Getter
  private final List<HwmSupply> supplies;

  public HwmDemand(Response response) {
    super(response.getDemandId(), response.getDemand(), response.getAdDuration());
    this.adSetId = response.getAdSetId();
    this.order = response.getOrder();
    this.supplies = new ArrayList<>();
  }
}
