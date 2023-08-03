package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.util.MemoryAlignment;
import java.util.BitSet;
import java.util.List;
import lombok.Value;

@Value
public class ShaleGraph {

  List<ShaleDemand> demands;
  List<ShaleSupply> supplies;
  BitSet edges;
  ReachStorage reachStorage;
  double penalty;
  int alignedDemandSize;

  public ShaleGraph(List<ShaleDemand> demands, List<ShaleSupply> supplies, ReachStorage reachStorage, double penalty,
                    BitSet edges) {
    this.demands = demands;
    this.supplies = supplies;
    this.reachStorage = reachStorage;
    this.penalty = penalty;
    this.edges = edges;
    this.alignedDemandSize = MemoryAlignment.getSize(demands);
  }

  public int getIndex(int supplyIndex, int demandIndex) {
    return supplyIndex * alignedDemandSize + demandIndex;
  }

  public void initParams() {
    demands.parallelStream().forEach(demand -> {
      long count = 0;
      long totalSupply = 0;
      double unReachSum = 0;
      for (ShaleSupply shaleSupply : supplies) {
        if (isQualified(demand, shaleSupply)) {
          totalSupply += shaleSupply.getConcurrency();
          if (demand.getReachEnabled() == 1) {
            unReachSum += getUnReachRatio(demand, shaleSupply);
            count++;
          }
        }
      }

      demand.setReachOffset(count == 0 ? 0 : unReachSum / count);
      demand.setTheta(totalSupply == 0 ? 1 : Math.min(1, demand.getDemand() / totalSupply));
    });
  }

  public boolean isQualified(ShaleDemand demand, ShaleSupply supply) {
    return edges.get(getIndex(supply.getId(), demand.getId()));
  }

  public double getTd(ShaleDemand demand, ShaleSupply supply) {
    return reachStorage.getTd(demand, supply);
  }

  public double getRd(ShaleDemand demand, ShaleSupply supply) {
    return reachStorage.getRd(demand, supply);
  }

  public double getUnReachRatio(ShaleDemand demand, ShaleSupply supply) {
    return reachStorage.getUnReachRatio(demand.getId(), supply.getId());
  }
}
