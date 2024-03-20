package com.hotstar.adtech.blaze.allocation.planner.service.manager;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.exception.BusinessException;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.allocation.planner.ErrorCodes;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.AdModel;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Match;
import com.hotstar.adtech.blaze.allocation.planner.service.manager.loader.GeneralPlanContextLoader;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
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
      throw new BusinessException(ErrorCodes.HWM_MODE_PUBLISH_FAILED, e, match.getContentId());
    }
  }


}
