package com.hotstar.adtech.blaze.allocation.planner.config;

import com.hotstar.adtech.blaze.admodel.repository.AllocationPlanResultRepository;
import com.hotstar.adtech.blaze.allocation.planner.ingester.AdModelLoader;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataExchangerService;
import com.hotstar.adtech.blaze.allocation.planner.ingester.DataLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.AllocationDiagnosisService;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.AllocationPlanGenerator;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.AllocationPlanManager;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.AllocationResultUploader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.HwmModeGenerator;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.ShaleAndHwmModeGenerator;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.GeneralPlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.ShalePlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.HwmPlanWorker;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.ShalePlanWorker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
  public AllocationPlanManager allocationPlanManager(AllocationPlanGenerator allocationPlanGenerator,
                                                     DataLoader dataLoader) {
    return new AllocationPlanManager(allocationPlanGenerator, dataLoader);
  }

  @Bean
  @ConditionalOnProperty(prefix = "blaze.ad-allocation-planner", name = "mode", havingValue = "HWM")
  public AllocationPlanGenerator hwmAllocationPlanGenerator(HwmPlanWorker hwmPlanWorker,
                                                            GeneralPlanContextLoader generalPlanContextLoader,
                                                            AllocationDiagnosisService allocationDiagnosisService,
                                                            AllocationPlanResultRepository repository,
                                                            AllocationResultUploader allocationResultUploader) {
    return new HwmModeGenerator(hwmPlanWorker, generalPlanContextLoader, allocationDiagnosisService,
      repository, allocationResultUploader);
  }

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "blaze.ad-allocation-planner", name = "mode", havingValue = "SHALE")
  public AllocationPlanGenerator shaleAndHwmAllocationPlanGenerator(ShalePlanWorker shalePlanWorker,
                                                                    HwmPlanWorker hwmPlanWorker,
                                                                    ShalePlanContextLoader shalePlanContextLoader,
                                                                    AllocationDiagnosisService diagnosisService,
                                                                    AllocationPlanResultRepository repository,
                                                                    AllocationResultUploader resultUploader) {
    return new ShaleAndHwmModeGenerator(shalePlanWorker, hwmPlanWorker, shalePlanContextLoader,
      diagnosisService, resultUploader, repository);
  }

}
