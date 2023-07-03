package com.hotstar.adtech.blaze.adserver.ingester.service;

import com.hotstar.adtech.blaze.adserver.ingester.entity.SingleStream;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.PlayoutStreamResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExchangerService {

  private final DataExchangerClient dataExchangerClient;

  public Map<String, String> getPlayoutStreamMapping(String contentId) {
    //convert stream to playout streams
    //Map.KEY: in-Hindi-iOS -> Map.Value: in-Hindi-iOS+Android+mWeb:
    ContentStreamResponse contentStreamResponse = dataExchangerClient.getStreamDefinition(contentId).getData();
    return buildConvertMap(contentStreamResponse);
  }

  private Map<String, String> buildConvertMap(ContentStreamResponse contentStreamResponse) {
    return contentStreamResponse.getPlayoutStreamResponses().stream()
      .map(PlayoutStreamResponse::getStreamDetail)
      .flatMap(streamDetail -> buildStream(streamDetail).stream())
      .collect(Collectors.toMap(SingleStream::getKey, SingleStream::getPlayoutStream));
  }

  private List<SingleStream> buildStream(StreamDetail streamDetail) {
    return streamDetail.getPlatforms().stream()
      .map(platform ->
        SingleStream.builder()
          .tenant(streamDetail.getTenant().getName())
          .language(streamDetail.getLanguage().getName())
          .platform(platform.getName())
          .playoutStream(streamDetail.getKey())
          .build()
      ).collect(Collectors.toList());
  }

}
