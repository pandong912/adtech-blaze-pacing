package com.hotstar.adtech.blaze.allocation.planner.ingester;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.common.algomodel.StandardMatchProgressModel;
import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
@Profile("!sim && !worker")
public class DataLoader {

  private final DataExchangerService dataExchangerService;
  private final AdModelLoader adModelLoader;
  private final AtomicReference<StandardMatchProgressModel> standardMatchProgressModelRef = new AtomicReference<>();
  private final AtomicReference<AdModel> adModelReference = new AtomicReference<>(AdModel.EMPTY);


  @PostConstruct
  public void init() {
    loadBreakProgressModel();
    loadAdModel();
  }

  @Scheduled(fixedDelayString = "${blaze.ad-allocation-planner.schedule.load.break-progress-model:3600000}")
  public void loadBreakProgressModel() {
    List<Double> breakProgressModel = dataExchangerService.getMatchBreakProgressModel();
    StandardMatchProgressModel standardMatchProgressModel = new StandardMatchProgressModel(breakProgressModel);
    standardMatchProgressModelRef.set(standardMatchProgressModel);
  }


  @Scheduled(fixedDelayString = "${blaze.ad-allocation-planner.schedule.load.ad-model:10000}")
  public void loadAdModel() {
    AdModelVersion adModelVersion = adModelReference.get().getAdModelVersion();
    AdModelVersion newVersion = dataExchangerService.getLatestAdModelVersion(adModelVersion);
    if (newVersion.getVersion() <= adModelVersion.getVersion()) {
      return;
    }
    adModelReference.set(adModelLoader.loadAdModel(newVersion));
  }

  public StandardMatchProgressModel getStandardMatchProgressModel() {
    return standardMatchProgressModelRef.get();
  }

  public AdModel getAdModel() {
    return adModelReference.get();
  }

}
