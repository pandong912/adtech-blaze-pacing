package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.allocation.planner.qualification.result.QualificationResult;
import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;
import java.util.List;
import lombok.Value;

@Value
public class ShaleGraph {

  List<ShaleDemand> demands;
  List<ShaleSupply> supplies;
  QualificationResult edges;
  ReachStorage reachStorage;
  double penalty;

  public ShaleGraph(List<ShaleDemand> demands, List<ShaleSupply> supplies, ReachStorage reachStorage, double penalty,
                    QualificationResult edges) {
    this.demands = demands;
    this.supplies = supplies;
    this.reachStorage = reachStorage;
    this.penalty = penalty;
    this.edges = edges;
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
      demand.setTheta(totalSupply == 0 ? 1 : Math.max(0.000001, Math.min(1, demand.getDemand() / totalSupply)));
    });
  }

  public boolean isQualified(ShaleDemand demand, ShaleSupply supply) {
    return edges.get(supply.getId(), demand.getId());
  }

  public double getTd(ShaleDemand demand, ShaleSupply supply) {
    double reachRatio = getUnReachRatio(demand, supply);
    return demand.getTheta() + Math.max(0, reachRatio - demand.getReachOffset());
  }

  public double getRd(ShaleDemand demand, ShaleSupply supply) {
    double reachRatio = getUnReachRatio(demand, supply);
    return Math.min(1, Math.max(reachRatio - demand.getReachOffset(), 0) / (demand.getReachOffset() + 0.000001));
  }

  public double getUnReachRatio(ShaleDemand demand, ShaleSupply supply) {
    if (demand.getReachEnabled() == 1 && supply.getStreamType() == StreamType.SSAI_Spot) {
      return reachStorage.getUnReachRatioFromStorage(demand.getReachIndex(), supply.getId());
    } else {
      return 0;
    }
  }
}
