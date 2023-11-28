package com.hotstar.adtech.blaze.allocationdata.client.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DegradationReachStorage implements ReachStorage {

  @Override
  public double getUnReachRatioFromStorage(int demandId, int concurrencyId) {
    return 0;
  }
}

