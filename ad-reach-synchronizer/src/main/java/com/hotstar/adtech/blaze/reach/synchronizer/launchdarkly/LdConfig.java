package com.hotstar.adtech.blaze.reach.synchronizer.launchdarkly;

import com.hotstar.launchdarkly.LdConfigStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration("launchDarklyStoreConfig")
@ComponentScan(basePackages = "com.hotstar.launchdarkly")
@RequiredArgsConstructor
public class LdConfig implements DynamicConfig {

  private final LdConfigStore ldConfigStore;


  private Boolean getBooleanValue(LdValue ldValue) {
    return ldConfigStore.getFeatureFlag(ldValue.getLdKey(), Boolean.parseBoolean(ldValue.getLdDefaultValue()));
  }

  private String getStringValue(LdValue ldValue) {
    return ldConfigStore.getFeatureFlag(ldValue.getLdKey(), ldValue.getLdDefaultValue());
  }

  @Override
  public Boolean getEnableMaximiseReach() {
    return getBooleanValue(LdValue.EnableMaximiseReach);
  }
}
