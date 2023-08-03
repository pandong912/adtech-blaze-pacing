package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.GRAPH_SOLVE;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.HwmResult;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;


@Service
public class HwmSolver {

  @Timed(value = GRAPH_SOLVE, extraTags = {"algorithm", "hwm"})
  public List<HwmResult> solve(GraphContext context) {

    List<HwmSupply> supplies =
      context.getRequests().stream().map(request -> new HwmSupply(request, context.getBreakDuration()))
        .collect(Collectors.toList());

    List<HwmDemand> demands =
      context.getResponses().stream().map(HwmDemand::new).collect(Collectors.toList());

    HwmGraph hwmGraph = new HwmGraph(demands, supplies, context.getEdges());

    // Data is ordered by order field
    TreeMap<Integer, List<HwmDemand>> demandGroup = demands.stream()
      .collect(Collectors.groupingBy(HwmDemand::getOrder, TreeMap::new, Collectors.toList()));

    return demandGroup.values().stream()
      .map(hwmDemands -> allocateOnOrder(hwmDemands, hwmGraph))
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }


  private Collection<HwmResult> allocateOnOrder(List<HwmDemand> demands, HwmGraph hwmGraph) {

    Map<Long, HwmResult> allocations = demands.stream()
      .collect(Collectors.toMap(HwmDemand::getAdSetId, demand -> allocate(demand, hwmGraph)));

    for (HwmDemand demand : demands) {
      double probability = allocations.get(demand.getAdSetId()).getProbability();
      for (HwmSupply supply : hwmGraph.getSupplies()) {
        if (hwmGraph.isQualified(demand, supply)) {
          long needs = (long) (supply.getConcurrency() * probability * demand.getAdDuration());
          supply.updateInventory(needs);
        }
      }
    }

    return allocations.values();
  }


  private HwmResult allocate(HwmDemand demand, HwmGraph hwmGraph) {
    long totalSupply = 0;
    for (HwmSupply supply : hwmGraph.getSupplies()) {
      if (hwmGraph.isQualified(demand, supply)) {
        totalSupply += supply.getInventory(demand.getAdDuration());
      }
    }
    double probability = totalSupply == 0 ? 1 : Math.min(1, demand.getDemand() / totalSupply);
    return HwmResult.builder()
      .id(demand.getAdSetId())
      .probability(probability)
      .build();
  }
}
