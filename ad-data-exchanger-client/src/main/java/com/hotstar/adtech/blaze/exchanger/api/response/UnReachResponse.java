package com.hotstar.adtech.blaze.exchanger.api.response;

import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import com.hotstar.adtech.blaze.exchanger.api.entity.UnReachData;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UnReachResponse {
  String ssaiTag;
  StreamDetail streamDetail;
  List<UnReachData> unReachDataList;

  public String getKey() {
    return streamDetail.getKey() + "|" + ssaiTag;
  }
}
