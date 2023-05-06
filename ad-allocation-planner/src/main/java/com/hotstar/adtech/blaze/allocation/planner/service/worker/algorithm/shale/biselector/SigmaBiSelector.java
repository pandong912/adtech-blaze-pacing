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
public class SigmaBiSelector {
  private final ShaleGraph graph;

  public void updateParams(List<ShaleDemand> demands) {
    demands.forEach(demand -> {
      double sigmaMin = 0;
      double sigmaMax = 0;
      double calculatedZMax =
        graph.getEdgesForDemand(demand).stream().mapToDouble(supply -> calculateZMax(supply, demand)).max().orElse(0d);
      sigmaMax = Math.max(calculatedZMax, sigmaMax);
      double sigma = bisectSigma(sigmaMin, sigmaMax + 1, demand);
      demand.setSigma(sigma);
    });
  }

  private double calculateZMax(ShaleSupply supply, ShaleDemand demand) {
    return supply.getBreakDuration() * V / demand.getAdDuration() / demand.getTheta() + supply.getBeta()
      - V;
  }

  private double bisectSigma(double sigmaMin, double sigmaMax, ShaleDemand demand) {
    List<ShaleSupply> supplies = graph.getEdgesForDemand(demand);
    while (sigmaMin + ERR < sigmaMax) {
      double z = (sigmaMin + sigmaMax) / 2;
      double ktt = 0;
      for (ShaleSupply supply : supplies) {
        ktt += calculateKttForSigma(supply, z, demand);
      }
      if (ktt > (1 + EPS) * demand.getDemand() * demand.getAdDuration()) {
        sigmaMax = z;
      } else if (ktt < (1 - EPS) * demand.getDemand() * demand.getAdDuration()) {
        sigmaMin = z;
      } else {
        return z;
      }
    }
    return sigmaMax;
  }

  private double calculateKttForSigma(ShaleSupply supply, double z, ShaleDemand demand) {
    double x = Math.min(1, Math.max(0, demand.getTheta()
      * (1 + (z - supply.getBeta()) / V)));
    double s = supply.getConcurrency() * x * demand.getAdDuration();
    return Math.min(s, supply.getRawInventory());
  }
}
