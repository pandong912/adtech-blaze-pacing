package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.common.exception.ResourceNotFoundException;
import com.hotstar.adtech.blaze.admodel.repository.ContentStreamRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.ContentStream;
import com.hotstar.adtech.blaze.admodel.repository.model.Stream;
import com.hotstar.adtech.blaze.exchanger.api.entity.LanguageMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.PlatformMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.StreamDetail;
import com.hotstar.adtech.blaze.exchanger.api.response.ContentStreamResponse;
import com.hotstar.adtech.blaze.exchanger.api.response.PlayoutStreamResponse;
import com.hotstar.adtech.blaze.exchanger.config.CacheConfig;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamService {
  private static final String DEFAULT_STREAM_MAPPING_CONTENT_ID = "-1";

  private final ContentStreamRepository contentStreamRepository;
  private final MetaDataService metaDataService;

  @Cacheable(cacheNames = CacheConfig.STREAM_DEFINITION,
    cacheManager = CacheConfig.DATABASE_CACHE_MANAGER,
    sync = true)
  public ContentStreamResponse getStreamDefinition(String contentId) {
    List<ContentStream> defaultContentStreams =
      contentStreamRepository.findByContentId(DEFAULT_STREAM_MAPPING_CONTENT_ID);
    return buildContentStreamResp(contentId, defaultContentStreams);
  }

  private ContentStreamResponse buildContentStreamResp(String contentId,
                                                       List<ContentStream> defaultContentStreams) {
    List<ContentStream> contentStreams = contentStreamRepository.findByContentId(contentId);
    contentStreams = contentStreams.isEmpty() ? defaultContentStreams : contentStreams;
    if (contentStreams.isEmpty()) {
      throw new ResourceNotFoundException("no stream definition found for contentId:" + contentId);
    }
    PlatformMapping platformMapping = metaDataService.getPlatformMapping();
    LanguageMapping languageMapping = metaDataService.getLanguageMapping();
    List<PlayoutStreamResponse> playoutStreamResponses = contentStreams.stream()
      .map(contentStream -> buildStreamDetail(contentStream, platformMapping, languageMapping))
      .collect(Collectors.toList());
    return ContentStreamResponse.builder()
      .contentId(contentId)
      .playoutStreamResponses(playoutStreamResponses)
      .build();
  }

  private PlayoutStreamResponse buildStreamDetail(ContentStream contentStream, PlatformMapping platformMapping,
                                                  LanguageMapping languageMapping) {
    Stream stream = contentStream.getStream();
    return PlayoutStreamResponse.builder()
      .playoutId(contentStream.getPlayoutId())
      .streamType(stream.getStreamType())
      .streamDetail(StreamDetail.builder()
        .tenant(stream.getTenant())
        .language(languageMapping.getById(stream.getLanguageId()))
        .platforms(stream.getPlatformIds()
          .stream()
          .sorted()
          .map(platformMapping::getById)
          .collect(Collectors.toList()))
        .build())
      .build();
  }
}
