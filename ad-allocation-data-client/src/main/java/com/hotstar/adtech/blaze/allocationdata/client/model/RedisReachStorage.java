package com.hotstar.adtech.blaze.allocationdata.client.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisReachStorage implements ReachStorage {
  private final double[][] unReachRatio;

  @Override
  public double getUnReachRatioFromStorage(int demandId, int concurrencyId) {
    return unReachRatio[demandId][concurrencyId];
  }
}

