package com.hotstar.adtech.blaze.ingester;

import com.hotstar.adtech.blaze.ingester.launchdarkly.DynamicConfig;
import lombok.Setter;

@Setter
class LdconfigTestClass implements DynamicConfig {
  Boolean enable = false;

  @Override
  public Boolean getEnableSsaiStramIncludeSpotUser() {
    return enable;
  }
}