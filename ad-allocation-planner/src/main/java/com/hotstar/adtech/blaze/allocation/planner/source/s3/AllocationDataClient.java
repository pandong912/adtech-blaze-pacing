package com.hotstar.adtech.blaze.allocation.planner.source.s3;

import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocation.planner.source.context.ShalePlanContext;

@SuppressWarnings("unused")
public interface AllocationDataClient {
  void uploadShaleData(String contentId, String version, ShalePlanContext shalePlanContext);

  void uploadHwmData(String contentId, String version, GeneralPlanContext generalPlanContext);

  ShalePlanContext loadShaleData(String contentId, String version);

  GeneralPlanContext loadHwmData(String contentId, String version);

}

