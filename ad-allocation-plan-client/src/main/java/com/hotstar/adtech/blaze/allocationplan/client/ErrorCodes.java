package com.hotstar.adtech.blaze.allocationplan.client;

import com.hotstar.adtech.blaze.admodel.common.domain.ErrorCode;

public class ErrorCodes {
  public static final ErrorCode ALLOCATION_DATA_LOAD_FAILED =
    error(140301, "Failed to load allocation data from: %s");

  public static final ErrorCode ALLOCATION_DATA_UPLOAD_FAILED =
    error(140302, "Failed to upload allocation data to: %s");

  private static ErrorCode error(int code, String message) {
    return ErrorCode.builder()
      .code(code)
      .message(message)
      .build();
  }

}

