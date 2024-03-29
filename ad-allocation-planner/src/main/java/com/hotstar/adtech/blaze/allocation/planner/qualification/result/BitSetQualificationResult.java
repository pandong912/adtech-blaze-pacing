package com.hotstar.adtech.blaze.allocation.planner.qualification.result;

import com.hotstar.adtech.blaze.allocation.planner.qualification.index.RequestFeasible;
import java.util.BitSet;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BitSetQualificationResult implements QualificationResult {
  private final int alignedSize;
  private final BitSet bitSet;

  public BitSetQualificationResult(int supplySize, int demandSize) {
    this.alignedSize = getSize(demandSize);
    bitSet = new BitSet(supplySize * alignedSize);
  }

  public BitSetQualificationResult(int supplySize, int demandSize, List<RequestFeasible> results) {
    this.alignedSize = getSize(demandSize);
    long[] longArray = new long[supplySize * alignedSize / 64];

    for (RequestFeasible result : results) {
      BitSet requestResult = result.getBitSet();
      int index = result.getSupplyId();
      long[] requestResultArray = requestResult.toLongArray();
      System.arraycopy(requestResultArray, 0, longArray, index * alignedSize / 64,
        requestResultArray.length);
    }
    this.bitSet = BitSet.valueOf(longArray);
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
