package com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification;

import java.util.BitSet;
import lombok.Getter;

@Getter
public class BitSetQualificationResult implements QualificationResult {
  private final int alignedSize;
  private final BitSet bitSet;

  public BitSetQualificationResult(int supplySize, int demandSize) {
    int size = getSize(demandSize);
    this.alignedSize = size;
    bitSet = new BitSet(supplySize * size);
  }

  // make adSet size to be multiple of 64, so that we can concurrently and safely update bitSet
  private int getSize(int size) {
    int floor = size / 64;
    return (size % 64 == 0 ? floor : floor + 1) * 64;
  }

  @Override
  public boolean get(int supplyIndex, int demandIndex) {
    return bitSet.get(supplyIndex * alignedSize + demandIndex);
  }

  @Override
  public void set(int supplyIndex, int demandIndex) {
    bitSet.set(supplyIndex * alignedSize + demandIndex);
  }
}
