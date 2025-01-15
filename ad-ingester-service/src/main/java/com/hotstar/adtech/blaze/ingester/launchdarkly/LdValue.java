package com.hotstar.adtech.blaze.ingester.launchdarkly;

import lombok.Getter;

@Getter
public enum LdValue {
  MODIFY_SSAI_STREAM_MAPPING("blaze.ingester.ssai-stream-include-spot-user.enable", "false");

  private final String ldKey;
  private final String ldDefaultValue;

  LdValue(String ldKey, String ldDefaultValue) {
    this.ldKey = ldKey;
    this.ldDefaultValue = ldDefaultValue;
  }

}
