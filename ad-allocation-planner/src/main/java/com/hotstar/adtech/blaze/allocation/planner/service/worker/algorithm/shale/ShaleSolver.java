package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.GRAPH_SOLVE;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.V;

import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleDemandResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleSupplyResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.AlphaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.BetaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.SigmaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShaleSolver {
  private static final int MAX_ITER = 2;

  @Timed(value = GRAPH_SOLVE, extraTags = {"algorithm", "shale"})
  public ShaleResult solve(GraphContext context, ReachStorage reachStorage, double penalty) {

    List<ShaleSupply> supplies =
      context.getRequests().stream()
        .map(request -> new ShaleSupply(request, context.getBreakDuration()))
        .collect(Collectors.toList());

    List<ShaleDemand> demands =
      context.getResponses().stream()
        .map(ShaleDemand::new)
        .collect(Collectors.toList());

    ShaleGraph shaleGraph = new ShaleGraph(demands, supplies, reachStorage, penalty, context.getEdges());
    shaleGraph.initParams();

    stageOne(shaleGraph);

    List<ShaleDemandResult> shaleDemandResults = stageTwo(shaleGraph);
    return ShaleResult.builder()
      .demandResults(shaleDemandResults)
      .supplyResults(shaleGraph.getSupplies().stream()
        .map(supply -> ShaleSupplyResult.builder()
          .id(supply.getId())
          .beta(supply.getBeta())
          .build())
        .collect(Collectors.toList()))
      .build();
  }


  private void stageOne(ShaleGraph shaleGraph) {
    int count = MAX_ITER;
    double cda = 0;
    long totalTime = 0;
    while (count > 0) {
      count--;
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      new BetaBiSelector(shaleGraph).updateParams();
      cda = new AlphaBiSelector(shaleGraph).updateParams();
      stopWatch.stop();
      Metrics.timer(MetricNames.STAGE_ONG_LOOP).record(stopWatch.getTime(), TimeUnit.MILLISECONDS);
      totalTime += stopWatch.getTime();
      if (cda < 0.000001 || totalTime > 40000) {
        break;
      }
    }
    if (cda >= 0.000001) {
      new BetaBiSelector(shaleGraph).updateParams();
    }
  }

  private List<ShaleDemandResult> stageTwo(ShaleGraph shaleGraph) {

    TreeMap<Integer, List<ShaleDemand>> demandGroup = shaleGraph.getDemands().stream()
      .collect(Collectors.groupingBy(ShaleDemand::getOrder, TreeMap::new, Collectors.toList()));

    return demandGroup.values().stream().map(demands -> allocate(shaleGraph, demands))
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }


  private Collection<ShaleDemandResult> allocate(ShaleGraph shaleGraph, List<ShaleDemand> demands) {
    new SigmaBiSelector(shaleGraph).updateParams(demands);
    demands.forEach(demand -> updateInventory(shaleGraph, demand));
    return demands.stream().map(demand -> ShaleDemandResult.builder()
        .id(demand.getAdSetId())
        .alpha(demand.getAlpha())
        .theta(demand.getTheta())
        .sigma(demand.getSigma())
        .mean(demand.getReachOffset())
        .reachEnabled(demand.getReachEnabled())
        .adDuration(demand.getAdDuration())
        .build())
      .collect(Collectors.toList());
  }

  private void updateInventory(ShaleGraph shaleGraph, ShaleDemand demand) {
    for (ShaleSupply supply : shaleGraph.getSupplies()) {
      if (shaleGraph.isQualified(demand, supply)) {
        double prob = Math.min(1, Math.max(0, shaleGraph.getTd(demand, supply)
          * (1 + (demand.getSigma() - supply.getBeta() + shaleGraph.getRd(demand, supply)) / V)));
        long needs = (long) (supply.getConcurrency() * demand.getAdDuration() * prob);
        supply.updateInventory(needs);
      }
    }
  }
}
