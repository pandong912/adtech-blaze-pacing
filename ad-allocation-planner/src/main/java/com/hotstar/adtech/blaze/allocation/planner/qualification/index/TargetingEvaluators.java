package com.hotstar.adtech.blaze.allocation.planner.qualification.index;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.evaluator.TargetingEvaluatorsProtocol;
import java.util.BitSet;
import java.util.Map;
import java.util.TreeSet;
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
  private final TargetingEngine language;
  private final TargetingEngine duration;
  private final TargetingEngine aspectRatio;
  private final TreeSet<Integer> durationSet;
  private final int adSetSize;
  private final BitSet activeAdSetBitSet;

  public static TargetingEvaluators buildSsaiTargetingEvaluators(TargetingEvaluatorsProtocol protocol) {
    Map<Integer, TargetingEngine> audience = protocol.getAudience().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey,
        entry -> new TargetingEngine(RuleFeasible.of(entry.getValue()))));
    TargetingEngine streamNew =
      new TargetingEngine(RuleFeasible.of(protocol.getStreamNew()));
    TargetingEngine stream =
      new TargetingEngine(RuleFeasible.of(protocol.getStream()));
    TargetingEngine breakTargeting =
      new TargetingEngine(RuleFeasible.of(protocol.getBreakTargeting()));
    TargetingEngine language =
      new TargetingEngine(RuleFeasible.of(protocol.getLanguage()));
    TargetingEngine duration =
      new TargetingEngine(RuleFeasible.of(protocol.getDuration()));
    TargetingEngine aspectRatio =
      new TargetingEngine(RuleFeasible.of(protocol.getAspectRatio()));
    return TargetingEvaluators.builder()
      .audience(audience)
      .streamNew(streamNew)
      .stream(stream)
      .breakTargeting(breakTargeting)
      .language(language)
      .duration(duration)
      .durationSet(protocol.getDurationSet())
      .aspectRatio(aspectRatio)
      .activeAdSetBitSet(protocol.getActiveAdSetBitSet())
      .adSetSize(protocol.getMaxBitIndex())
      .build();
  }
}
