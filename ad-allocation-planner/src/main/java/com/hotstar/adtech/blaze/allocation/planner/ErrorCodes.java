package com.hotstar.adtech.blaze.allocation.planner;

import com.hotstar.adtech.blaze.admodel.common.domain.ErrorCode;

public class ErrorCodes {
  public static final ErrorCode HWM_MODE_PUBLISH_FAILED =
    error(140401, "Failed to publish hwm mode task for match: %s");

  public static final ErrorCode SHALE_MODE_PUBLISH_FAILED =
    error(140402, "Failed to publish shale mode task for match: %s");

  public static final ErrorCode SHALE_AND_HWM_PUBLISH_FAILED =
    error(140403, "Failed to publish shale and hwm mode task for match: %s");

  public static final ErrorCode MODEL_DATA_EMPTY =
    error(140404, "Match Break Progress Model Data is empty");

  public static final ErrorCode MODEL_DATA_INVALID =
    error(140405, "Match Break Progress Model Data is invalid");

  private static ErrorCode error(int code, String message) {
    return ErrorCode.builder()
      .code(code)
      .message(message)
      .build();
  }

}

