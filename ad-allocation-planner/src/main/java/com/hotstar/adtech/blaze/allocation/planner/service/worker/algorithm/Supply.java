package com.hotstar.adtech.blaze.allocation.planner.service.worker.algorithm;

import lombok.Getter;

public class Supply {
  @Getter
  private final int id;
  private long inventory;
  @Getter
  private final long concurrency;
  @Getter
  private final int breakDuration;

  public Supply(Integer id, int breakDuration, long concurrency) {
    this.id = id;
    this.inventory = breakDuration * concurrency;
    this.concurrency = concurrency;
    this.breakDuration = breakDuration;
  }

  public void updateInventory(long needs) {
    inventory -= needs;
  }

  public long getInventory(long timeNeeds) {
    if (concurrency * Math.min(breakDuration, timeNeeds) <= inventory) {
      return concurrency;
    } else {
      return 0L;
    }
  }

  public long getRawInventory() {
    return Math.max(0, inventory);
  }
}
