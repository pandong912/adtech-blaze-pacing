package com.hotstar.adtech.blaze.allocation.planner.service.worker;

import static com.hotstar.adtech.blaze.allocation.planner.metric.MetricNames.WORKER_DATA_LOAD;

import com.hotstar.adtech.blaze.allocation.planner.config.CacheConfig;
import com.hotstar.adtech.blaze.allocationdata.client.AllocationDataClient;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.ShalePlanContext;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllocationDataLoader {
  private final AllocationDataClient allocationDataClient;

  @Cacheable(cacheNames = CacheConfig.SHALE_DATA,
    cacheManager = CacheConfig.CACHE_MANAGER, sync = true)
  @Timed(value = WORKER_DATA_LOAD, extraTags = {"algorithm", "shale"})
  public ShalePlanContext loadShaleData(String path) {
    return allocationDataClient.loadShaleData(path);
  }

  @Timed(value = WORKER_DATA_LOAD, extraTags = {"algorithm", "hwm"})
  @Cacheable(cacheNames = CacheConfig.HWM_DATA,
    cacheManager = CacheConfig.CACHE_MANAGER, sync = true)
  public GeneralPlanContext loadGeneralData(String path) {
    return allocationDataClient.loadHwmData(path);
  }
}



