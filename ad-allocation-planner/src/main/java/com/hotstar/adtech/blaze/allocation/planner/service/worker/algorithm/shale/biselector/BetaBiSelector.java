package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector;

import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.EPS;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.ERR;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.V;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleGraph;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BetaBiSelector {
  private final ShaleGraph graph;


  public void updateParams() {
    graph.getSupplies().parallelStream()
      .forEach(supply -> {
        double betaMin = 0;
        double betaMax = 0;

        double calculateBMax = calculateBMax(supply);
        betaMax = Math.max(betaMax, calculateBMax);
        double beta = bisectBeta(betaMin, betaMax + 1, supply);
        supply.setBeta(beta);
      });
  }

  private double calculateBMax(ShaleSupply supply) {
    double max = 0;
    for (ShaleDemand demand : graph.getDemands()) {
      if (graph.isQualified(demand, supply)) {
        max = Math.max(max, demand.getAlpha() + graph.getRd(demand, supply) + V);
      }
    }
    return max;
  }

  private double bisectBeta(double betaMin, double betaMax, ShaleSupply supply) {
    while (betaMin + ERR < betaMax) {
      double b = (betaMin + betaMax) / 2;
      double ktt = 0;
      for (ShaleDemand demand : graph.getDemands()) {
        if (graph.isQualified(demand, supply)) {
          ktt += calculateKttForBeta(demand, b, supply);
        }
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
