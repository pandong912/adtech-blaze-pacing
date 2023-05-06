package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.hwm;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.GRAPH_SOLVE;

import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.HwmResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GraphContext;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;


@Service
public class HwmSolver {

  @Timed(value = GRAPH_SOLVE, extraTags = {"algorithm", "hwm"})
  public List<HwmResult> solve(GraphContext context) {

    Map<Integer, List<Long>> supplyToDemand = context.getRequests().stream().collect(
      Collectors.toMap(Request::getConcurrencyId, this::collectAdSetId));

    List<HwmSupply> supplies =
      context.getRequests().stream().map(request -> new HwmSupply(request, context.getBreakDuration()))
        .collect(Collectors.toList());

    List<HwmDemand> demands =
      context.getResponses().stream().map(HwmDemand::new).collect(Collectors.toList());

    buildEdge(supplies, demands, supplyToDemand);

    // Data is ordered by order field
    TreeMap<Integer, List<HwmDemand>> demandGroup = demands.stream()
      .collect(Collectors.groupingBy(HwmDemand::getOrder, TreeMap::new, Collectors.toList()));

    return demandGroup.values().stream().map(this::allocateOnOrder).flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  private void buildEdge(List<HwmSupply> supplies, List<HwmDemand> demands, Map<Integer, List<Long>> supplyToDemand) {
    Map<Long, HwmDemand> adSetIdMap =
      demands.stream().collect(Collectors.toMap(HwmDemand::getAdSetId, Function.identity()));
    supplies.forEach(supply -> supplyToDemand.get(supply.getId()).forEach(adSetId -> {
      HwmDemand demand = adSetIdMap.get(adSetId);
      demand.getSupplies().add(supply);
    }));
  }


  private List<Long> collectAdSetId(Request request) {
    return request.getQualifiedAdSets().stream().map(QualifiedAdSet::getId).collect(Collectors.toList());
  }

  private Collection<HwmResult> allocateOnOrder(List<HwmDemand> demands) {

    Map<Long, HwmResult> allocations = demands.stream()
      .collect(Collectors.toMap(HwmDemand::getAdSetId, this::allocate));

    demands.forEach(demand -> {
      double probability = allocations.get(demand.getAdSetId()).getProbability();
      demand.getSupplies().forEach(supply -> {
        long needs = (long) (supply.getConcurrency() * probability * demand.getAdDuration());
        supply.updateInventory(needs);
      });
    });

    return allocations.values();
  }


  private HwmResult allocate(HwmDemand demand) {
    long supply =
      demand.getSupplies().stream().mapToLong(s -> s.getInventory(demand.getAdDuration())).sum();
    double probability = supply == 0 ? 1 : Math.min(1, demand.getDemand() / supply);
    return HwmResult.builder()
      .id(demand.getAdSetId())
      .probability(probability)
      .build();
  }
}
