package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.common.enums.GlobalConfigKey;
import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.admodel.repository.GlobalConfigRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.GlobalConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobalConfigService {
  private final GlobalConfigRepository globalConfigRepository;

  public Double getFlinkHeartbeatSample() {
    GlobalConfig globalConfig = globalConfigRepository.findByKey(GlobalConfigKey.FLINK_HEARTBEAT_SAMPLE)
      .orElseThrow(() -> new ServiceException("Flink sample not found"));
    return Double.valueOf(globalConfig.getValue());
  }

  public Double getFlinkTrackerSample() {
    GlobalConfig globalConfig = globalConfigRepository.findByKey(GlobalConfigKey.FLINK_TRACKER_SAMPLE)
      .orElseThrow(() -> new ServiceException("Flink sample not found"));
    return Double.valueOf(globalConfig.getValue());
  }
}
