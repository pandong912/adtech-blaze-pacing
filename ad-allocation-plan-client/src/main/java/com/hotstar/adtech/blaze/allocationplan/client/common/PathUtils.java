package com.hotstar.adtech.blaze.allocationplan.client.common;

import java.time.Instant;

public class PathUtils {
  private static final String PREFIX = "allocation-plan";

  private static final String DIAGNOSIS_PREFIX = "allocation-diagnosis";
  private static final String SLASH = "/";

  public static String joinToPath(String contentId, Instant version) {
    return PREFIX + SLASH + contentId + SLASH + version.toString();
  }

  public static String joinToDiagnosisPath(String contentId, Instant version) {
    return DIAGNOSIS_PREFIX + SLASH + contentId + SLASH + version.toString();
  }

}
