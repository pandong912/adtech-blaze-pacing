package com.hotstar.adtech.blaze.allocation.planner;

import com.hotstar.adtech.blaze.allocation.planner.qualification.QualifiedAdSet;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleGraph;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.reach.ReachStorage;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Request;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShaleGraphTestData {

  public static ShaleGraph getShaleGraph() {

    List<Response> responses = Arrays.asList(
      Response.builder().adSetId(1L).demandId(2).order(1).demand(4.0).maximizeReach(1).adDuration(10000).build(),
      Response.builder().adSetId(2L).demandId(5).order(1).demand(2.0).maximizeReach(1).adDuration(10000).build(),
      Response.builder().adSetId(3L).demandId(4).order(1).demand(3.0).maximizeReach(1).adDuration(10000).build()
    );
    List<Request> requests = Arrays.asList(
      Request.builder().concurrencyId(1).concurrency(3).qualifiedAdSets(
        Arrays.asList(
          QualifiedAdSet.builder().id(1L).qualifiedAds(Collections.emptyList()).build(),
          QualifiedAdSet.builder().id(2L).qualifiedAds(Collections.emptyList()).build(),
          QualifiedAdSet.builder().id(3L).qualifiedAds(Collections.emptyList()).build()
        )
      ).build(),
      Request.builder().concurrencyId(2).concurrency(1).qualifiedAdSets(
        Collections.singletonList(
          QualifiedAdSet.builder().id(1L).qualifiedAds(Collections.emptyList()).build()
        )
      ).build(),
      Request.builder().concurrencyId(3).concurrency(2).qualifiedAdSets(
        Collections.singletonList(
          QualifiedAdSet.builder().id(2L).qualifiedAds(Collections.emptyList()).build()
        )
      ).build()
    );

    Map<Integer, List<Long>> supplyToDemand = requests.stream()
      .collect(Collectors.toMap(Request::getConcurrencyId, ShaleGraphTestData::collectAdSetId));

    List<ShaleDemand> shaleDemands = responses.stream()
      .map(ShaleDemand::new)
      .collect(Collectors.toList());
    List<ShaleSupply> shaleSupplies = requests.stream()
      .map(shaleSupply -> new ShaleSupply(shaleSupply, 20000))
      .collect(Collectors.toList());

    ShaleGraph shaleGraph = new ShaleGraph(shaleDemands, shaleSupplies, new MockReachStorage(), ShaleConstant.PENALTY);
    shaleGraph.buildEdge(supplyToDemand);
    shaleGraph.initParams();
    return shaleGraph;
  }

  private static List<Long> collectAdSetId(Request request) {
    return request.getQualifiedAdSets().stream().map(QualifiedAdSet::getId).collect(Collectors.toList());
  }

  public static class MockReachStorage implements ReachStorage {
    @Override
    public double getUnReachRatio(int adSetId, int concurrencyId) {
      return 0.5;
    }
  }
}
