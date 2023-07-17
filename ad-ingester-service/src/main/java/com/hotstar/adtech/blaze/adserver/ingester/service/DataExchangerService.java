package com.hotstar.adtech.blaze.adserver.ingester.service;

import com.hotstar.adtech.blaze.adserver.ingester.entity.SingleStream;
import com.hotstar.adtech.blaze.exchanger.api.DataExchangerClient;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDefinition;
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
    //convert stream to playoutId
    //Map.KEY: in-eng-phone-ssai -> Map.Value: P1
    List<StreamDefinition> streamDefinitions = dataExchangerClient.getStreamDefinitionV2(contentId).getData();
    return buildConvertMap(streamDefinitions);
  }

  private Map<String, String> buildConvertMap(List<StreamDefinition> streamDefinitions) {
    return streamDefinitions.stream()
      .flatMap(streamDefinition -> buildStream(streamDefinition).stream())
      .collect(Collectors.toMap(SingleStream::getKey, SingleStream::getPlayoutId));
  }

  private List<SingleStream> buildStream(StreamDefinition streamDefinition) {
    return streamDefinition.getLadders().stream()
      .map(ladder ->
        SingleStream.builder()
          .tenant(streamDefinition.getTenant().getName())
          .language(streamDefinition.getLanguage().getAbbreviation())
          .ladder(ladder.name())
          .ads(streamDefinition.getStreamType().getAds())
          .playoutId(streamDefinition.getPlayoutId())
          .build()
      ).collect(Collectors.toList());
  }

}
