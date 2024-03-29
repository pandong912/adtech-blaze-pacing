package com.hotstar.adtech.blaze.allocation.planner.qualification.result;

import java.util.Arrays;
import lombok.Getter;

@Getter
public class ArrayQualificationResult implements QualificationResult {
  private final boolean[][] result;

  public ArrayQualificationResult(int supplySize, int demandSize) {
    result = new boolean[demandSize][supplySize];
    for (boolean[] row : result) {
      Arrays.fill(row, false);
    }
  }

  @Override
  public boolean get(int supplyIndex, int demandIndex) {
    return result[demandIndex][supplyIndex];
  }

  @Override
  public void set(int supplyIndex, int demandIndex) {
    result[demandIndex][supplyIndex] = true;
  }
}
