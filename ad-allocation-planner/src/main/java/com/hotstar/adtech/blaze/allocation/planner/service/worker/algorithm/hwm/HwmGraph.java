package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm;

import com.hotstar.adtech.blaze.allocation.planner.qualification.result.QualificationResult;
import java.util.List;
import lombok.Value;

@Value
public class HwmGraph {

  List<HwmDemand> demands;
  List<HwmSupply> supplies;
  QualificationResult edges;

  public HwmGraph(List<HwmDemand> demands, List<HwmSupply> supplies, QualificationResult edges) {
    this.demands = demands;
    this.supplies = supplies;
    this.edges = edges;
  }


  public boolean isQualified(HwmDemand demand, HwmSupply supply) {
    return edges.get(supply.getId(), demand.getId());
  }
}
