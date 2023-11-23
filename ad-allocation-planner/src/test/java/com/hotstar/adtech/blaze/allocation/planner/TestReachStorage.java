package com.hotstar.adtech.blaze.allocation.planner;

import com.hotstar.adtech.blaze.allocationdata.client.model.ReachStorage;

public class TestReachStorage implements ReachStorage {
  //  Map<Integer, Map<Integer, Double>> unReachData = new HashMap<>();
  private final double[][] reachData;
  // use OFFSET to verify demandId allocation
  private static final int OFFSET = 10;
  private static final Double reachRatio = 1d;

  public TestReachStorage(int adSetSize, int concurrencySize) {
    //    for (int i = 0; i < 50000; i++) {
    //      for (int j = 0; j < 1000; j++) {
    //        unReachData.computeIfAbsent(j, k -> new HashMap<>()).put(i, (j % 10 + i % 10 + 1) % 10 * 0.1);
    //      }
    //    }
    reachData = new double[adSetSize + OFFSET][concurrencySize];
    for (int i = 0; i < adSetSize + OFFSET; i++) {
      for (int j = 0; j < concurrencySize; j++) {
        reachData[i][j] = (j % 10 + i % 10 + 1) % 10 * 0.1;
      }
    }
  }

  @Override
  public double getUnReachRatioFromStorage(int adSetId, int concurrencyId) {
    // return unReachData.getOrDefault(adSetId, Collections.emptyMap()).getOrDefault(concurrencyId,reachRatio);
    return reachData[adSetId][concurrencyId];
  }
}

