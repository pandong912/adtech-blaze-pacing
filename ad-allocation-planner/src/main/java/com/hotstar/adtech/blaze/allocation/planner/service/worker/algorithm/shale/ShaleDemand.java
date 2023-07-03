package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.Demand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ShaleDemand extends Demand {
  private final Long adSetId;
  private final int order;
  private double alpha;
  private double theta;
  private double sigma;
  private double reachOffset;
  private double std;
  private int reachEnabled;

  public ShaleDemand(Response response) {
    super(response.getDemandId(), response.getDemand(), response.getAdDuration());
    this.adSetId = response.getAdSetId();
    this.alpha = 0;
    this.order = response.getOrder();
    this.reachEnabled = response.getMaximizeReach();
  }
}
