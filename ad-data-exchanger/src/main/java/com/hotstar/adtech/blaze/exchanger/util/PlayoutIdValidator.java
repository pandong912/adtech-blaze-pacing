package com.hotstar.adtech.blaze.exchanger.util;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;

public class PlayoutIdValidator {
  public static String validate(String playoutId) {
    if (StringUtils.isEmpty(playoutId)) {
      throw new ServiceException("playout id is empty");
    }
    if (!playoutId.startsWith("P")) {
      throw new ServiceException("playout id is not valid");
    }
    return playoutId;
  }

  public static boolean notValidate(String playoutId) {
    if (StringUtils.isEmpty(playoutId)) {
      return true;
    }
    return !playoutId.startsWith("P");
  }
}
