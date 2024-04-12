package com.hotstar.adtech.blaze.allocationdata.client;

import com.hotstar.adtech.blaze.admodel.common.domain.ErrorCode;

public class ErrorCodes {
  public static final ErrorCode ALLOCATION_DATA_LOAD_FAILED =
    error(140101, "Failed to load allocation data from: %s");

  public static final ErrorCode ALLOCATION_DATA_UPLOAD_FAILED =
    error(140102, "Failed to upload allocation data to: %s");

  public static final ErrorCode COMPRESS_DATA_FAILED = error(140103, "Fail to compress data");
  public static final ErrorCode DECOMPRESS_DATA_FAILED = error(140104, "Fail to decompress data");

  private static ErrorCode error(int code, String message) {
    return ErrorCode.builder()
      .code(code)
      .message(message)
      .build();
  }

}

