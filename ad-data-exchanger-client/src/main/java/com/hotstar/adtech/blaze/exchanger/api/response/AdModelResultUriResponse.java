package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.admodel.common.exception.ServiceException;
import com.hotstar.adtech.blaze.exchanger.api.entity.AdModelDetail;
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

  List<AdModelDetail> adModelDetails;

  public String getMd5(String fileName) {
    return adModelDetails.stream()
      .filter(adModelDetail -> Objects.equals(adModelDetail.getFileName(), fileName))
      .findFirst()
      .map(AdModelDetail::getMd5)
      .orElseThrow(() -> new ServiceException("No md5 found for file name " + fileName));
  }
}
