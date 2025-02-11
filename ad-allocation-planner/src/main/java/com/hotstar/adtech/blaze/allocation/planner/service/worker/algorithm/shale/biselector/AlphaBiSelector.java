package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector;

import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.EPS;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.ERR;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.V;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleGraph;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AlphaBiSelector {
  private final ShaleGraph shaleGraph;

  public double updateParams() {
    return shaleGraph.getDemands().parallelStream().mapToDouble(demand -> {
      double alphaMin = 0;
      double alphaMax = shaleGraph.getPenalty() / demand.getAdDuration();
      List<ShaleSupply> qualified = new ArrayList<>(shaleGraph.getSupplies().size() / 2);
      for (ShaleSupply supply : shaleGraph.getSupplies()) {
        if (shaleGraph.isQualified(demand, supply)) {
          qualified.add(supply);
        }
      }
      double alpha = bisectAlpha(alphaMin, alphaMax, demand, qualified);
      double da = Math.abs(alpha - demand.getAlpha());
      demand.setAlpha(Math.min(alpha, alphaMax));
      return da;
    }).sum();
  }

  private double bisectAlpha(double alphaMin, double alphaMax, ShaleDemand demand, List<ShaleSupply> qualified) {
    while (alphaMin + ERR < alphaMax) {
      double a = (alphaMin + alphaMax) / 2;
      double ktt = 0;
      for (ShaleSupply supply : qualified) {
        ktt += calculateKttForAlpha(supply, demand, a);
      }
      if (ktt > (1 + EPS) * demand.getDemand()) {
        alphaMax = a;
      } else if (ktt < (1 - EPS) * demand.getDemand()) {
        alphaMin = a;
      } else {
        return a;
      }
    }
    return shaleGraph.getPenalty() / demand.getAdDuration();
  }

  private double calculateKttForAlpha(ShaleSupply supply, ShaleDemand demand, double a) {
    double x = Math.min(1,
      Math.max(0, shaleGraph.getTd(demand, supply)
        * (1 + (a - supply.getBeta() + shaleGraph.getRd(demand, supply)) / V)));
    return x * supply.getConcurrency();
  }
}
