package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import com.hotstar.adtech.blaze.allocation.planner.config.CacheConfig;
import com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames;
import com.hotstar.adtech.blaze.allocationdata.client.AllocationDataClient;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.ShalePlanContext;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllocationDataLoader {
  private final AllocationDataClient allocationDataClient;

  @Cacheable(cacheNames = CacheConfig.SHALE_DATA,
    cacheManager = CacheConfig.CACHE_MANAGER, sync = true)
  @Timed(value = MetricNames.WORKER_DATA_LOAD, extraTags = {"algorithm", "shale"})
  public ShalePlanContext loadShaleData(String path) {
    return allocationDataClient.loadShaleData(path);
  }

  @Cacheable(cacheNames = CacheConfig.HWM_DATA,
    cacheManager = CacheConfig.CACHE_MANAGER, sync = true)
  @Timed(value = MetricNames.WORKER_DATA_LOAD, extraTags = {"algorithm", "hwm"})
  public GeneralPlanContext loadGeneralData(String path) {
    return allocationDataClient.loadHwmData(path);
  }

}