package com.hotstar.adtech.blaze.ingester;

import com.hotstar.adtech.blaze.admodel.common.enums.Ladder;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.ingester.entity.ConcurrencyGroup;
import com.hotstar.adtech.blaze.ingester.entity.Match;
import com.hotstar.adtech.blaze.ingester.entity.SingleStream;
import com.hotstar.adtech.blaze.ingester.repository.StreamCohortConcurrencyRepository;
import com.hotstar.adtech.blaze.ingester.repository.StreamConcurrencyRepository;
import com.hotstar.adtech.blaze.ingester.service.ConcurrencyService;
import com.hotstar.adtech.blaze.ingester.service.PulseService;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConcurrencyServiceTest extends TestEnvConfig {
  @Mock
  private PulseService pulseService;
  @Autowired
  private StreamConcurrencyRepository streamConcurrencyRepository;
  @Autowired
  private StreamCohortConcurrencyRepository streamCohortConcurrencyRepository;
  private ConcurrencyService concurrencyService;

  @Test
  public void testUpdateStreamCohortConcurrency() {
    Mockito
      .when(pulseService.getLiveContentStreamCohortConcurrency(Mockito.anyString()))
      .thenReturn(getStreamCohortConcurrency());
    Mockito
      .when(pulseService.getLiveContentStreamConcurrency(Mockito.anyString()))
      .thenReturn(getStreamConcurrency());
    concurrencyService = new ConcurrencyService(pulseService, streamConcurrencyRepository,
      streamCohortConcurrencyRepository, new LdconfigTestClass());
    Match match = Match.builder().contentId("123").tournamentId(123L).build();
    Map<String, String> streamMappingConverter = getStreamMappingConverter();
    concurrencyService.updateMatchConcurrency(match, streamMappingConverter);
    Map<String, Long> contentAllStreamConcurrency = streamConcurrencyRepository.getContentAllStreamConcurrency("123");
    System.out.println("contentAllStreamConcurrency: " + contentAllStreamConcurrency);
    Assertions.assertNull(contentAllStreamConcurrency.get("in-hin--ssai"));
    Assertions.assertEquals(5L, contentAllStreamConcurrency.get("P15"));
    Assertions.assertEquals(10L, contentAllStreamConcurrency.get("P7"));

    Map<String, Long> contentAllStreamCohortConcurrency =
      streamCohortConcurrencyRepository.getContentStreamAllCohortConcurrency("123");
    System.out.println("contentAllStreamCohortConcurrency: " + contentAllStreamCohortConcurrency);
    Assertions.assertNull(contentAllStreamCohortConcurrency.get("in--eng|SSAI::001"));
    Assertions.assertEquals(1L, contentAllStreamCohortConcurrency.get("P15|SSAI::001"));
    Assertions.assertEquals(11L, contentAllStreamCohortConcurrency.get("P1|SSAI::001"));
    Assertions.assertEquals(13L, contentAllStreamCohortConcurrency.get("P15|SSAI::002"));
  }

  private Map<String, String> getStreamMappingConverter() {
    return Stream.of(
      SingleStream.builder()
        .playoutId("P1")
        .ads(StreamType.SSAI_Spot.getAds())
        .ladder(Ladder.phone.toString())
        .language("eng")
        .tenant(Tenant.India.getName())
        .build(),
      SingleStream.builder()
        .playoutId("P7")
        .ads(StreamType.SSAI_Spot.getAds())
        .ladder(Ladder.phone.toString())
        .language("hin")
        .tenant(Tenant.India.getName())
        .build(),
      SingleStream.builder()
        .playoutId("P15")
        .ads(StreamType.Spot.getAds())
        .ladder(Ladder.phone.toString())
        .language("tel")
        .tenant(Tenant.India.getName())
        .build(),
      SingleStream.builder()
        .playoutId("P15")
        .ads(StreamType.Spot.getAds())
        .ladder(Ladder.tv.toString())
        .language("tel")
        .tenant(Tenant.India.getName())
        .build(),
        SingleStream.builder()
          .playoutId("P15")
          .ads(StreamType.Spot.getAds())
          .ladder(Ladder.web.toString())
          .language("tel")
          .tenant(Tenant.India.getName())
          .build()
      )
      .collect(Collectors.toMap(SingleStream::getKey, SingleStream::getPlayoutId));
  }

  private ConcurrencyGroup getStreamConcurrency() {
    Map<String, Long> concurrencyValues = new HashMap<>();
    concurrencyValues.put("in-hin-phone-ssai", 10L);
    concurrencyValues.put("in-hin--ssai|SSAI::001", 1L);
    concurrencyValues.put("in-tel-phone-non_ssai", 1L);
    concurrencyValues.put("in-tel-tv-non_ssai", 2L);
    concurrencyValues.put("in-tel-web-non_ssai", 2L);
    return ConcurrencyGroup.builder()
      .tsBucket(100L)
      .concurrencyValues(concurrencyValues)
      .build();
  }

  private ConcurrencyGroup getStreamCohortConcurrency() {
    Map<String, Long> concurrencyValues = new HashMap<>();
    concurrencyValues.put("in--eng|SSAI::001", 1L);
    concurrencyValues.put("in-eng-phone-ssai|SSAI::001|English+Android", 1L);
    concurrencyValues.put("in-eng-phone-ssai|SSAI::001|English+iOS", 10L);
    concurrencyValues.put("in-eng-phone-ssai|SSAI::002", 1L);
    concurrencyValues.put("in-tel-phone-non_ssai|SSAI::002", 2L);
    concurrencyValues.put("in-tel-tv-non_ssai|SSAI::002", 11L);
    concurrencyValues.put("in-tel-tv-non_ssai|SSAI::001", 1L);
    return ConcurrencyGroup.builder()
      .tsBucket(100L)
      .concurrencyValues(concurrencyValues)
      .build();
  }

}
