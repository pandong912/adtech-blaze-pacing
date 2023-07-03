package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale;

import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class ShaleGraph {
  List<ShaleDemand> demands;
  List<ShaleSupply> supplies;
  Map<Integer, List<ShaleSupply>> demandToSupply;
  Map<Integer, List<ShaleDemand>> supplyToDemand;
  ReachStorage reachStorage;
  double penalty;

  public ShaleGraph(List<ShaleDemand> demands, List<ShaleSupply> supplies, ReachStorage reachStorage, double penalty) {
    this.demands = demands;
    this.supplies = supplies;
    this.demandToSupply = new HashMap<>();
    this.supplyToDemand = new HashMap<>();
    this.reachStorage = reachStorage;
    this.penalty = penalty;
  }


  public void buildEdge(Map<Integer, List<Long>> supplyToDemandEdge) {
    Map<Long, ShaleDemand> adSetIdMap =
      demands.stream().collect(Collectors.toMap(ShaleDemand::getAdSetId, Function.identity()));
    supplies.forEach(supply -> supplyToDemandEdge.get(supply.getId()).forEach(adSetId -> {
      ShaleDemand demand = adSetIdMap.get(adSetId);
      supplyToDemand.computeIfAbsent(supply.getId(), k -> new ArrayList<>()).add(demand);
      demandToSupply.computeIfAbsent(demand.getId(), k -> new ArrayList<>()).add(supply);
    }));
  }

  public List<ShaleSupply> getEdgesForDemand(ShaleDemand demand) {
    return demandToSupply.getOrDefault(demand.getId(), Collections.emptyList());
  }

  public List<ShaleDemand> getEdgesForSupply(ShaleSupply supply) {
    return supplyToDemand.getOrDefault(supply.getId(), Collections.emptyList());
  }

  public void initParams() {
    demands.forEach(demand -> {
      List<ShaleSupply> supplies = getEdgesForDemand(demand);

      long totalSupply = supplies.stream().mapToLong(ShaleSupply::getConcurrency).sum();
      demand.setTheta(totalSupply == 0 ? 1 : Math.min(1, demand.getDemand() / totalSupply));
      // reachOffset is mean of reach ratio
      demand.setReachOffset(supplies.stream()
        .mapToDouble(supply -> getUnReachRatio(demand, supply))
        .average()
        .orElse(0));
      demand.setStd(Math.sqrt(supplies.stream()
        .mapToDouble(supply -> Math.pow(getUnReachRatio(demand, supply) - demand.getReachOffset(), 2))
        .average()
        .orElse(0)));
    });
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
