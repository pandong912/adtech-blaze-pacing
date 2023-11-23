package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.Inspector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DurationInspector implements Inspector<Ad> {

  private final int maxDuration;

  @Override
  public boolean qualify(Ad ad) {
    return ad.getDurationMs() <= maxDuration;
  }
}
