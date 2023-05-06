package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.allocation.planner.ShaleGraphTestData;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.ShaleGraph;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.AlphaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.BetaBiSelector;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm.shale.biselector.SigmaBiSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BiSelectorTest {

  @Test
  public void testAlphaBiSelector() {
    ShaleGraph shaleGraph = ShaleGraphTestData.getShaleGraph();
    new AlphaBiSelector(shaleGraph).updateParams();
    Assertions.assertEquals(0.5, shaleGraph.getDemands().get(0).getAlpha());
    Assertions.assertEquals(6.103515625E-5, shaleGraph.getDemands().get(1).getAlpha());
    Assertions.assertEquals(0.5, shaleGraph.getDemands().get(2).getAlpha());
  }

  @Test
  public void testBetaBiSelector() {
    ShaleGraph shaleGraph = ShaleGraphTestData.getShaleGraph();
    new BetaBiSelector(shaleGraph).updateParams();
    System.out.println(shaleGraph);
    Assertions.assertEquals(0.166748046875, shaleGraph.getSupplies().get(0).getBeta());
    Assertions.assertEquals(0.0, shaleGraph.getSupplies().get(1).getBeta());
    Assertions.assertEquals(0.0, shaleGraph.getSupplies().get(2).getBeta());
  }

  @Test
  public void testSigmaBiSelector() {
    ShaleGraph shaleGraph = ShaleGraphTestData.getShaleGraph();
    new SigmaBiSelector(shaleGraph).updateParams(shaleGraph.getDemands());
    Assertions.assertEquals(1.0, shaleGraph.getDemands().get(0).getSigma());
    Assertions.assertEquals(7.62939453125E-5, shaleGraph.getDemands().get(1).getSigma());
    Assertions.assertEquals(1.0, shaleGraph.getDemands().get(2).getSigma());
  }

}
