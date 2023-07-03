package com.hotstar.adtech.blaze.allocation.planner.config.launchdarkly;

import lombok.Getter;

@Getter
public enum LdValue {
  EnableMaximiseReach("blaze.pacing.maximise.reach.enable", "false"),
  EnableShale("blaze.pacing.shale.enable", "false");

  private final String ldKey;
  private final String ldDefaultValue;

  LdValue(String ldKey, String ldDefaultValue) {
    this.ldKey = ldKey;
    this.ldDefaultValue = ldDefaultValue;
  }

}
