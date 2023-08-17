package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.GeneralPlanContextLoader;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.source.context.GeneralPlanContext;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Profile("!sim && !worker")
public class HwmModePublisher {

  private final GeneralPlanContextLoader generalPlanContextLoader;
  private final TaskPublisher taskPublisher;

  public void publishPlan(Match match, AdModel adModel) {
    try {
      Instant version = Instant.now();
      GeneralPlanContext generalPlanContext = generalPlanContextLoader.getGeneralPlanContext(match, adModel);
      taskPublisher.uploadAndPublish(match, generalPlanContext, version, AlgorithmType.HWM, AlgorithmType.HWM);
    } catch (Exception e) {
      throw new ServiceException("Failed to publish hwm mode task for match: " + match.getContentId(), e);
    }
  }


}
