package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.GRAPH_SOLVE;
import static com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant.V;

import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.ShaleResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.AlphaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.BetaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.SigmaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShaleSolver {
  //todo dynamic calculate max iter
  private static final int MAX_ITER = 2;

  @Timed(value = GRAPH_SOLVE, extraTags = {"algorithm", "shale"})
  public List<ShaleResult> solve(GraphContext context, ReachStorage reachStorage, double penalty) {
    Map<Integer, List<Long>> supplyToDemand = context.getRequests().stream()
      .collect(Collectors.toMap(Request::getConcurrencyId, this::collectAdSetId));

    List<ShaleSupply> supplies =
      context.getRequests().stream()
        .map(request -> new ShaleSupply(request, context.getBreakDuration()))
        .collect(Collectors.toList());

    List<ShaleDemand> demands =
      context.getResponses().stream()
        .map(ShaleDemand::new)
        .collect(Collectors.toList());


    ShaleGraph shaleGraph = new ShaleGraph(demands, supplies, reachStorage, penalty);
    shaleGraph.buildEdge(supplyToDemand);
    shaleGraph.initParams();

    stageOne(shaleGraph);

    return stageTwo(shaleGraph);
  }

  private List<Long> collectAdSetId(Request request) {
    return request.getQualifiedAdSets().stream().map(QualifiedAdSet::getId).collect(Collectors.toList());
  }


  private void stageOne(ShaleGraph shaleGraph) {
    int count = MAX_ITER;
    double cda = 0;
    while (count > 0) {
      count--;
      new BetaBiSelector(shaleGraph).updateParams();
      cda = new AlphaBiSelector(shaleGraph).updateParams();
      if (cda < 0.000001) {
        break;
      }
    }
    if (cda >= 0.000001) {
      new BetaBiSelector(shaleGraph).updateParams();
    }
  }

  private List<ShaleResult> stageTwo(ShaleGraph shaleGraph) {

    TreeMap<Integer, List<ShaleDemand>> demandGroup = shaleGraph.getDemands().stream()
      .collect(Collectors.groupingBy(ShaleDemand::getOrder, TreeMap::new, Collectors.toList()));

    return demandGroup.values().stream().map(demands -> allocate(shaleGraph, demands))
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }


  private Collection<ShaleResult> allocate(ShaleGraph shaleGraph, List<ShaleDemand> demands) {
    new SigmaBiSelector(shaleGraph).updateParams(demands);
    demands.forEach(demand -> updateInventory(shaleGraph, demand));
    return demands.stream().map(demand -> ShaleResult.builder()
        .id(demand.getAdSetId())
        .alpha(demand.getAlpha())
        .theta(demand.getTheta())
        .sigma(demand.getSigma())
        .std(demand.getStd())
        .mean(demand.getReachOffset())
        .reachEnabled(demand.getReachEnabled())
        .adDuration(demand.getAdDuration())
        .build())
      .collect(Collectors.toList());
  }

  private void updateInventory(ShaleGraph shaleGraph, ShaleDemand demand) {
    shaleGraph.getEdgesForDemand(demand).forEach(supply -> {
      double prob = Math.min(1, Math.max(0, shaleGraph.getTd(demand, supply)
        * (1 + (demand.getSigma() - supply.getBeta() + shaleGraph.getRd(demand, supply)) / V)));
      long needs = (long) (supply.getConcurrency() * demand.getAdDuration() * prob);
      supply.updateInventory(needs);
    });
  }
}
