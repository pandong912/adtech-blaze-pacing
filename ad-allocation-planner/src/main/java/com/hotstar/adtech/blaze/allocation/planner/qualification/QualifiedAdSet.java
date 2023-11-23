package com.hotstar.adtech.blaze.allocation.planner.qualification;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.BreakTargetingRule;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QualifiedAdSet {
  Long id;
  BreakTargetingRule breakTargetingRule;
  List<Ad> qualifiedAds;
}
