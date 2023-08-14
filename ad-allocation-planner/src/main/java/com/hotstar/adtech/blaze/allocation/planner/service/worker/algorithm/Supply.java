package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import lombok.Getter;

public class Supply {
  @Getter
  private final int id;
  private long inventory;
  @Getter
  private final long concurrency;

  public Supply(Integer id, long inventory, long concurrency) {
    this.id = id;
    this.inventory = inventory;
    this.concurrency = concurrency;
  }

  public void updateInventory(long needs) {
    inventory -= needs;
  }

  public long getInventory(long timeNeeds) {
    if (concurrency * timeNeeds <= inventory) {
      return concurrency;
    } else {
      return 0L;
    }
  }

  public long getRawInventory() {
    return Math.max(0, inventory);
  }
}
