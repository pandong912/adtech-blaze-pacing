package com.hotstar.adtech.blaze.allocationdata.client;

import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.ShalePlanContext;

@SuppressWarnings("unused")
public interface AllocationDataClient {
  void uploadShaleData(String contentId, String version, ShalePlanContext shalePlanContext);

  void uploadHwmData(String contentId, String version, GeneralPlanContext generalPlanContext);

  ShalePlanContext loadShaleData(String contentId, String version);

  GeneralPlanContext loadHwmData(String contentId, String version);

}

