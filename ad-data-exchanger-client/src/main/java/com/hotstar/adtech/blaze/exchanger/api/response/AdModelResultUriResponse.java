package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class AdModelResultUriResponse {
  Long id;
  String path;
  Long version;

  List<AdModelResultDetailResponse> adModelResultDetailResponses;

  public String getMd5(String fileName) {
    return adModelResultDetailResponses.stream()
      .filter(adModelResultDetailResponse -> Objects.equals(adModelResultDetailResponse.getFileName(), fileName))
      .findFirst()
      .map(AdModelResultDetailResponse::getMd5)
      .orElseThrow(() -> new ServiceException("No md5 found for file name " + fileName));
  }
}
