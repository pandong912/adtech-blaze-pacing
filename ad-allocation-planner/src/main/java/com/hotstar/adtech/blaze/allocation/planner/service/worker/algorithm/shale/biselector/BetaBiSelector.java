package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector;

import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.EPS;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.ERR;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.V;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleGraph;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BetaBiSelector {
  private final ShaleGraph graph;


  public void updateParams() {
    graph.getSupplies().forEach(supply -> {
      double betaMin = 0;
      double betaMax = 0;
      double calculateBMax = graph.getEdgesForSupply(supply).stream()
        .mapToDouble(demand -> demand.getAlpha() + graph.getRd(demand, supply) + V)
        .max()
        .orElse(0d);
      betaMax = Math.max(betaMax, calculateBMax);
      double beta = bisectBeta(betaMin, betaMax + 1, supply);
      supply.setBeta(beta);
    });
  }

  private double bisectBeta(double betaMin, double betaMax, ShaleSupply supply) {
    List<ShaleDemand> demands = graph.getEdgesForSupply(supply);
    while (betaMin + ERR < betaMax) {
      double b = (betaMin + betaMax) / 2;
      double ktt = 0;
      for (ShaleDemand demand : demands) {
        ktt += calculateKttForBeta(demand, b, supply);
      }
      if (ktt > 1 + EPS) {
        betaMin = b;
      } else if (ktt < 1 - EPS) {
        betaMax = b;
      } else {
        return b;
      }
    }
    return 0;
  }

  private double calculateKttForBeta(ShaleDemand demand, double b, ShaleSupply supply) {
    return Math.min(1, Math.max(0,
      graph.getTd(demand, supply) * (1 + (graph.getRd(demand, supply) + demand.getAlpha() - b) / V)))
      * demand.getAdDuration() / supply.getBreakDuration();
  }
}
