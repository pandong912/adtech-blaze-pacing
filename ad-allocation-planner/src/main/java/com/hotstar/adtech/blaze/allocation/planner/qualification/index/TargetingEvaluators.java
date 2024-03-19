package com.hotstar.adtech.blaze.allocation.planner.qualification.index;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.TargetingEvaluatorsProtocol;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TargetingEvaluators {

  private final Map<Integer, TargetingEngine> audience;
  private final TargetingEngine stream;
  private final TargetingEngine breakTargeting;
  private final TargetingEngine streamNew;
  private final BitSet activeAdSetBitSet;
  private final int adSetSize;

  public static TargetingEvaluators buildSsaiTargetingEvaluators(TargetingEvaluatorsProtocol protocol) {
    int size = protocol.getAdSetSize();
    Map<Integer, TargetingEngine> audience = protocol.getAudience().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey,
        entry -> new TargetingEngine(RuleFeasible.of(entry.getValue(), size))));
    TargetingEngine streamNew =
      new TargetingEngine(RuleFeasible.of(protocol.getStreamNew(), size));
    TargetingEngine stream =
      new TargetingEngine(RuleFeasible.of(protocol.getStream(), size));
    TargetingEngine breakTargeting =
      new TargetingEngine(RuleFeasible.of(protocol.getBreakTargeting(), size));
    return TargetingEvaluators.builder()
      .audience(audience)
      .streamNew(streamNew)
      .stream(stream)
      .breakTargeting(breakTargeting)
      .activeAdSetBitSet(protocol.getActiveAdSetBitSet())
      .adSetSize(size)
      .build();
  }
}
