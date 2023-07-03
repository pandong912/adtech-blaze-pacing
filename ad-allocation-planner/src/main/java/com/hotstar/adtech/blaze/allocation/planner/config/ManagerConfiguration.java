package com.hotstar.adtech.blaze.allocation.planner.config;

import com.hotstar.adtech.blaze.allocation.planner.config.launchdarkly.BlazeDynamicConfig;
import com.hotstar.adtech.blaze.allocation.planner.ingester.AdModelLoader;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.AllocationPlanManager;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.HwmModeGenerator;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.ShaleAndHwmModeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This configuration will generate plans periodically with the latest data if enabled.
 */
@Configuration
@Profile("!worker")
public class ManagerConfiguration {

  @Bean
  public DataLoader dataLoader(DataExchangerService dataExchangerService,
                               AdModelLoader adModelLoader) {
    return new DataLoader(dataExchangerService, adModelLoader);
  }

  @Bean
  public AllocationPlanManager allocationPlanManager(HwmModeGenerator hwmModeGenerator,
                                                     ShaleAndHwmModeGenerator shaleAndHwmModeGenerator,
                                                     BlazeDynamicConfig blazeDynamicConfig, DataLoader dataLoader) {
    return new AllocationPlanManager(hwmModeGenerator, shaleAndHwmModeGenerator, blazeDynamicConfig, dataLoader);
  }
}
