package com.hotstar.adtech.blaze.allocation.planner.ingester;

import com.hotstar.adtech.blaze.allocation.planner.common.model.AdModelVersion;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.algomodel.StandardMatchProgressModel;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
@Slf4j
public class DataLoader {

  private final DataExchangerService dataExchangerService;

  private final AdModelLoader adModelLoader;

  private final AtomicReference<StandardMatchProgressModel> standardMatchProgressModelRef = new AtomicReference<>();
  private final AtomicReference<List<BreakDetail>> breakDetailRef = new AtomicReference<>();

  private final AtomicReference<AdModel> adModelReference = new AtomicReference<>(AdModel.EMPTY);


  @PostConstruct
  public void init() {
    loadBreakProgressModel();
    loadBreakDefinition();
    loadAdModel();
  }

  @Scheduled(fixedDelayString = "${blaze.ad-allocation-planner.schedule.load.break-progress-model:3600000}")
  public void loadBreakProgressModel() {
    List<Double> breakProgressModel = dataExchangerService.getMatchBreakProgressModel();
    StandardMatchProgressModel standardMatchProgressModel = new StandardMatchProgressModel(breakProgressModel);
    standardMatchProgressModelRef.set(standardMatchProgressModel);
  }

  @Scheduled(fixedDelayString = "${blaze.ad-allocation-planner.schedule.load.break-definition:300000}")
  public void loadBreakDefinition() {
    List<BreakDetail> breakDetails = dataExchangerService.getBreakDefinition();
    breakDetailRef.set(breakDetails);
  }

  @Scheduled(fixedDelayString = "${blaze.ad-allocation-planner.schedule.load.ad-model:10000}")
  public void loadAdModel() {
    AdModelVersion adModelVersion = adModelReference.get().getAdModelVersion();
    AdModelVersion newVersion = dataExchangerService.getLatestAdModelVersion(adModelVersion);
    if (Objects.equals(newVersion, adModelVersion)) {
      return;
    }
    adModelReference.set(adModelLoader.loadAdModel(newVersion));
  }

  public StandardMatchProgressModel getStandardMatchProgressModel() {
    return standardMatchProgressModelRef.get();
  }

  public List<BreakDetail> getBreakDetail() {
    return breakDetailRef.get();
  }

  public AdModel getAdModel() {
    return adModelReference.get();
  }

}
