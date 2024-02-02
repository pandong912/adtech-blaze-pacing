package com.hotstar.adtech.blaze.allocation.planner.common.admodel;

import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreamNewTargetingRuleClause {

  private final Tenant tenant;
  private final Integer languageId;
  private final Ladder ladder;
  private final StreamType streamType;
}
