package com.hotstar.adtech.blaze.allocation.planner;

import com.google.common.collect.Sets;
import com.hotstar.adtech.blaze.allocation.planner.common.model.BreakDetail;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Language;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Languages;
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
        .build()
    );
  }

  public static Languages getLanguages() {
    List<Language> languages = Arrays.asList(
      Language.builder()
        .id(1)
        .tag("CLAGNGUAGE_ENGLISH")
        .name("English")
        .build(),
      Language.builder()
        .id(2)
        .tag("CLAGNGUAGE_HINDI")
        .name("Hindi")
        .build()
    );
    return new Languages(languages);
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
