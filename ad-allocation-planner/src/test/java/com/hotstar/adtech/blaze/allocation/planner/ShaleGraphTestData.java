package com.hotstar.adtech.blaze.allocation.planner;

import com.hotstar.adtech.blaze.allocation.planner.qualification.result.BitSetQualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.qualification.result.QualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleConstant;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleDemand;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleGraph;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleSupply;
import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;
import com.hotstar.adtech.blaze.allocationdata.client.model.Request;
import com.hotstar.adtech.blaze.allocationdata.client.model.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShaleGraphTestData {

  public static ShaleGraph getShaleGraph() {

    List<Response> responses = Arrays.asList(
      Response.builder().adSetId(1L).demandId(0).order(1).demand(4.0).maximizeReach(1).adDuration(10000).build(),
      Response.builder().adSetId(2L).demandId(1).order(1).demand(2.0).maximizeReach(1).adDuration(10000).build(),
      Response.builder().adSetId(3L).demandId(2).order(1).demand(3.0).maximizeReach(1).adDuration(10000).build()
    );
    List<Request> requests = Arrays.asList(
      Request.builder().concurrencyId(0).concurrency(3).build(),
      Request.builder().concurrencyId(1).concurrency(1).build(),
      Request.builder().concurrencyId(2).concurrency(2).build()
    );


    List<ShaleDemand> shaleDemands = responses.stream()
      .map(ShaleDemand::new)
      .collect(Collectors.toList());
    List<ShaleSupply> shaleSupplies = requests.stream()
      .map(shaleSupply -> new ShaleSupply(shaleSupply, 20000))
      .collect(Collectors.toList());
    QualificationResult bitSet = new BitSetQualificationResult(requests.size(), responses.size());
    bitSet.set(0, 0);
    bitSet.set(0, 1);
    bitSet.set(0, 2);
    bitSet.set(1, 0);
    bitSet.set(2, 1);


    ShaleGraph shaleGraph =
      new ShaleGraph(shaleDemands, shaleSupplies, new MockReachStorage(), ShaleConstant.PENALTY, bitSet);
    shaleGraph.initParams();
    return shaleGraph;
  }

  public static class MockReachStorage implements ReachStorage {
    @Override
    public double getUnReachRatioFromStorage(int adSetId, int concurrencyId) {
      return 0.5;
    }
  }
}
