package com.hotstar.adtech.blaze.allocation.planner;

import com.google.common.collect.Sets;
import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import com.hotstar.adtech.blaze.admodel.common.entity.PlatformEntity;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.exchanger.api.entity.LanguageMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.PlatformMapping;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QualificationTestData {
  public static List<BreakDetail> getBreakDetails() {
    return Arrays.asList(
      BreakDetail.builder()
        .breakDuration(Arrays.asList(20000, 50000, 80000))
        .breakTypeId(1)
        .breakType("Over")
        .build(),
      BreakDetail.builder()
        .breakDuration(Arrays.asList(20000, 40000, 60000))
        .breakTypeId(2)
        .breakType("PPL")
        .build(),
      BreakDetail.builder()
        .breakDuration(Arrays.asList(20000, 40000, 60000))
        .breakTypeId(3)
        .breakType("Unknown")
        .build(),
      BreakDetail.builder()
        .breakDuration(Arrays.asList(30000, 50000, 70000))
        .breakTypeId(4)
        .breakType("STO")
        .build()
    );
  }

  public static LanguageMapping getLanguageMapping() {
    List<LanguageEntity> languages = Arrays.asList(
      LanguageEntity.builder()
        .id(1)
        .tag("CLAGNGUAGE_ENGLISH")
        .name("English")
        .build(),
      LanguageEntity.builder()
        .id(2)
        .tag("CLAGNGUAGE_HINDI")
        .name("Hindi")
        .build()
    );
    return LanguageMapping.builder()
      .languageEntities(languages)
      .build();

  }

  public static PlatformMapping getPlatformMapping() {
    List<PlatformEntity> platforms = Arrays.asList(
      PlatformEntity.builder()
        .id(1)
        .tag("android")
        .name("Android")
        .build(),
      PlatformEntity.builder()
        .id(2)
        .tag("ios")
        .name("iOS")
        .build(),
      PlatformEntity.builder()
        .id(3)
        .tag("web")
        .name("Web")
        .build(),
      PlatformEntity.builder()
        .id(4)
        .tag("mweb")
        .name("MWeb")
        .build(),
      PlatformEntity.builder()
        .id(5)
        .tag("jiolyf")
        .name("JioLyf")
        .build(),
      PlatformEntity.builder()
        .id(6)
        .tag("androidtv")
        .name("AndroidTV")
        .build(),
      PlatformEntity.builder()
        .id(7)
        .tag("appletv")
        .name("AppleTV")
        .build(),
      PlatformEntity.builder()
        .id(8)
        .tag("firetv")
        .name("FireTV")
        .build()
    );
    return PlatformMapping.builder()
      .platformEntities(platforms)
      .build();
  }

  public static Map<String, Integer> getAttributeId2TargetingTagMap() {
    HashMap<String, Integer> attributeId2TargetingTagMap = new HashMap<>();
    attributeId2TargetingTagMap.put("S_APTG", 1);
    attributeId2TargetingTagMap.put("M_MUM", 2);
    attributeId2TargetingTagMap.put("M_NCR", 2);
    attributeId2TargetingTagMap.put("M_BEN", 2);
    return attributeId2TargetingTagMap;
  }

  public static List<Ad> getAds() {
    return Arrays.asList(
      Ad.builder()
        .durationMs(20000)
        .adSetId(1)
        .languageIds(Sets.newHashSet(1))
        .build(),
      Ad.builder()
        .durationMs(20000)
        .adSetId(1)
        .languageIds(Sets.newHashSet(3))
        .build(),
      Ad.builder()
        .durationMs(100000)
        .adSetId(1)
        .languageIds(Sets.newHashSet(1))
        .build()
    );
  }
}
