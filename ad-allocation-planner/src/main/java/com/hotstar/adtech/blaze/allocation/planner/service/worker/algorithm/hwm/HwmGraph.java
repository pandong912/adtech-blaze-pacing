package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm;

import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import java.util.BitSet;
import java.util.List;
import lombok.Value;

@Value
public class HwmGraph {

  List<HwmDemand> demands;
  List<HwmSupply> supplies;
  BitSet edges;
  int alignedDemandSize;

  public HwmGraph(List<HwmDemand> demands, List<HwmSupply> supplies, BitSet edges) {
    this.demands = demands;
    this.supplies = supplies;
    this.edges = edges;
    this.alignedDemandSize = MemoryAlignment.getSize(demands);
  }

  public int getIndex(int supplyIndex, int demandIndex) {
    return supplyIndex * alignedDemandSize + demandIndex;
  }

  public boolean isQualified(HwmDemand demand, HwmSupply supply) {
    return edges.get(getIndex(supply.getId(), demand.getId()));
  }
}
