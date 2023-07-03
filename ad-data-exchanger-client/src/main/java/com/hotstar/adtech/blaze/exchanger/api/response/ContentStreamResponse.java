package com.hotstar.adtech.blaze.exchanger.api.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ContentStreamResponse {
  String contentId;
  List<PlayoutStreamResponse> playoutStreamResponses;
}
