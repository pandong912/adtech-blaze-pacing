package com.hotstar.adtech.blaze.allocation.planner.qualification.index;

import java.util.BitSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class RequestFeasible {
  int supplyId;
  BitSet bitSet;
}
