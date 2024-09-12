package com.hotstar.adtech.blaze.allocation.planner.qualification.result;

import com.hotstar.adtech.blaze.allocation.planner.qualification.index.RequestFeasible;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

/**
 * the capacity of normal BitSet is Integer.MaxValue. But when we have more cohorts, it will exceed the capacity
 * limit, so we need to shard to data into different bitSet.
 * In each shard, we store qualification result of 200k cohorts.
 */
@Getter
@ToString
public class ShardBitSetQualificationResult implements QualificationResult {
  private static final int MAX_SIZE = 200000;
  private final int alignedSize;
  private final BitSet[] bitSet;

  public ShardBitSetQualificationResult(int supplySize, int demandSize) {
    this.alignedSize = getSize(demandSize);
    int shardNum = supplySize / MAX_SIZE + 1;
    this.bitSet = new BitSet[shardNum];
    Arrays.fill(bitSet, new BitSet(MAX_SIZE * alignedSize));
  }

  public ShardBitSetQualificationResult(int supplySize, int demandSize, List<RequestFeasible> results) {
    this.alignedSize = getSize(demandSize);
    int shardNum = supplySize / MAX_SIZE + 1;
    this.bitSet = new BitSet[shardNum];
    int shardMaxOffset = MAX_SIZE * alignedSize / 64;
    long[][] allShards = new long[shardNum][shardMaxOffset];
    for (RequestFeasible result : results) {
      //this cohort is stored in which shard
      int shardIndex = result.getSupplyId() / MAX_SIZE;
      // cohort index in this shard
      int shardOffset = (result.getSupplyId() % MAX_SIZE) * alignedSize / 64;
      long[] shardBitSetValue = allShards[shardIndex];
      long[] requestResultArray = result.getBitSet().toLongArray();
      System.arraycopy(requestResultArray, 0, shardBitSetValue, shardOffset,
        requestResultArray.length);
    }
    for (int i = 0; i < shardNum; i++) {
      bitSet[i] = BitSet.valueOf(allShards[i]);
    }
  }

  // make adSet size to be multiple of 64, so that we can concurrently and safely update bitSet
  private int getSize(int size) {
    int floor = size / 64;
    return (size % 64 == 0 ? floor : floor + 1) * 64;
  }

  @Override
  public boolean get(int supplyIndex, int demandIndex) {
    int shardIndex = supplyIndex / MAX_SIZE;
    int supplyIndexInShard = supplyIndex % MAX_SIZE;
    return bitSet[shardIndex].get(supplyIndexInShard * alignedSize + demandIndex);
  }

  @Override
  public void set(int supplyIndex, int demandIndex) {
    int shardIndex = supplyIndex / MAX_SIZE;
    int supplyIndexInShard = supplyIndex % MAX_SIZE;
    bitSet[shardIndex].set(supplyIndexInShard * alignedSize + demandIndex);
  }
}
