package com.hotstar.adtech.blaze.allocation.planner.qualification.result;

public interface QualificationResult {
  boolean get(int supplyIndex, int demandIndex);

  void set(int supplyIndex, int demandIndex);
}
