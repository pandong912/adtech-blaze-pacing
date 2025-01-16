package com.hotstar.adtech.blaze.ingester;

import com.hotstar.adtech.blaze.ingester.service.ConcurrencyService;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StreamConverterBuildingTest {
  @Test
  public void whenDisableLdThenNoAnyChangesOnStreamMappingMap() {
    ConcurrencyService concurrencyService = new ConcurrencyService(null, null, null, new LdconfigTestClass());
    Map<String, String> streamMappingConverter = Map.of("in-eng-phone-ssai", "P1",
      "in-eng-web-ssai", "P1",
      "in-eng-tv-ssai", "P2",
      "in-eng-tv-non_ssai", "P3",
      "in-eng-phone-non_ssai", "P4"
    );
    Map<String, String> newMap = concurrencyService.buildConverterForStreamConcurrency(streamMappingConverter);
    Assertions.assertEquals(streamMappingConverter, newMap);
  }

  @Test
  public void whenEnableLdButSpotStreamAlreadyDefinedThenNoAnyChangesOnStreamMappingMap() {
    LdconfigTestClass ldconfigTestClass = new LdconfigTestClass();
    ldconfigTestClass.setEnable(true);
    ConcurrencyService concurrencyService = new ConcurrencyService(null, null, null, ldconfigTestClass);
    Map<String, String> streamMappingConverter = Map.of("in-eng-phone-ssai", "P1",
      "in-eng-web-ssai", "P1",
      "in-eng-tv-ssai", "P2",
      "in-eng-tv-non_ssai", "P3",
      "in-eng-phone-non_ssai", "P4",
      "in-eng-web-non_ssai", "P4"
    );
    Map<String, String> newMap = concurrencyService.buildConverterForStreamConcurrency(streamMappingConverter);
    Assertions.assertEquals(streamMappingConverter, newMap);
  }

  @Test
  public void whenEnableLdThenStreamMappingIncludeSpotStream() {
    LdconfigTestClass ldconfigTestClass = new LdconfigTestClass();
    ldconfigTestClass.setEnable(true);
    ConcurrencyService concurrencyService = new ConcurrencyService(null, null, null, ldconfigTestClass);
    Map<String, String> streamMappingConverter = Map.of("in-eng-phone-ssai", "P1",
      "in-eng-web-ssai", "P1",
      "in-eng-tv-ssai", "P2"
    );
    Map<String, String> newMap = concurrencyService.buildConverterForStreamConcurrency(streamMappingConverter);
    Map<String, String> expectedMap = Map.of("in-eng-phone-ssai", "P1",
      "in-eng-web-ssai", "P1",
      "in-eng-tv-ssai", "P2",
      "in-eng-tv-non_ssai", "P2",
      "in-eng-phone-non_ssai", "P1",
      "in-eng-web-non_ssai", "P1"
    );
    Assertions.assertEquals(expectedMap, newMap);
  }

  @Test
  public void whenEnableLdButSomeSpotStreamAlreadyDefinedThenStreamMappingIncludeSpotStream() {
    LdconfigTestClass ldconfigTestClass = new LdconfigTestClass();
    ldconfigTestClass.setEnable(true);
    ConcurrencyService concurrencyService = new ConcurrencyService(null, null, null, ldconfigTestClass);
    Map<String, String> streamMappingConverter = Map.of("in-eng-phone-ssai", "P1",
      "in-eng-web-ssai", "P1",
      "in-eng-tv-ssai", "P2",
      "in-eng-web-non_ssai", "P1"
    );
    Map<String, String> newMap = concurrencyService.buildConverterForStreamConcurrency(streamMappingConverter);
    Map<String, String> expectedMap = Map.of("in-eng-phone-ssai", "P1",
      "in-eng-web-ssai", "P1",
      "in-eng-tv-ssai", "P2",
      "in-eng-tv-non_ssai", "P2",
      "in-eng-phone-non_ssai", "P1",
      "in-eng-web-non_ssai", "P1"
    );
    Assertions.assertEquals(expectedMap, newMap);
  }


}
