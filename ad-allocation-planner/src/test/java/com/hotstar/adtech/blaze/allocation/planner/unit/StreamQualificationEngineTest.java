package com.hotstar.adtech.blaze.allocation.planner.unit;

import com.hotstar.adtech.blaze.admodel.common.enums.RuleType;
import com.hotstar.adtech.blaze.admodel.common.enums.StreamType;
import com.hotstar.adtech.blaze.admodel.common.enums.Tenant;
import com.hotstar.adtech.blaze.allocation.planner.QualificationTestData;
import com.hotstar.adtech.blaze.allocation.planner.common.model.ContentStream;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Language;
import com.hotstar.adtech.blaze.allocation.planner.common.model.Platform;
import com.hotstar.adtech.blaze.allocation.planner.common.model.PlayoutStream;
import com.hotstar.adtech.blaze.allocation.planner.qualification.StreamAdSetQualificationEngine;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.BitSetQualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.service.worker.qualification.QualificationResult;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.AdSet;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRule;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.StreamTargetingRuleClause;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamQualificationEngineTest {

  private final Map<String, Language> languageMapping = QualificationTestData.getLanguageMapping();
  private final Map<String, Platform> platformMapping = QualificationTestData.getPlatformMapping();

  @Test
  public void whenIncludeRuleIsAllMatchThenSuccess() {
    ContentStream stream =
      ContentStream.builder()
        .concurrencyIdInStream(0)
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(languageMapping.get("English"))
          .platforms(Arrays.asList(
            platformMapping.get("Android"),
            platformMapping.get("iOS"))
          )
          .tenant(Tenant.India)
          .build())
        .build();
    QualificationResult bitSet = new BitSetQualificationResult(10, 2);
    StreamAdSetQualificationEngine streamQualificationEngine =
      new StreamAdSetQualificationEngine(stream.getPlayoutStream(), stream.getConcurrencyIdInStream(), bitSet);


    AdSet adSet = AdSet.builder()
      .demandId(0)
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Include)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(1)
            .languageId(1)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(2)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    streamQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertTrue(bitSet.get(0, 0));

    AdSet adSet2 = AdSet.builder()
      .demandId(1)
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Include)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(1)
            .languageId(1)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(8)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    streamQualificationEngine.qualify(Collections.singletonList(adSet2));
    Assertions.assertFalse(bitSet.get(0, 1));
  }

  @Test
  public void whenExcludeRuleNoneMatchThenSuccess() {
    ContentStream stream =
      ContentStream.builder()
        .concurrencyIdInStream(0)
        .playoutStream(PlayoutStream.builder()
          .streamType(StreamType.SSAI_Spot)
          .language(languageMapping.get("English"))
          .platforms(Arrays.asList(
            platformMapping.get("Android"),
            platformMapping.get("iOS")
          ))
          .tenant(Tenant.India)
          .build())
        .build();
    QualificationResult bitSet = new BitSetQualificationResult(10, 2);
    StreamAdSetQualificationEngine streamQualificationEngine =
      new StreamAdSetQualificationEngine(stream.getPlayoutStream(), stream.getConcurrencyIdInStream(), bitSet);


    AdSet adSet = AdSet.builder()
      .demandId(0)
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Exclude)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(6)
            .languageId(1)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(8)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    streamQualificationEngine.qualify(Collections.singletonList(adSet));
    Assertions.assertTrue(bitSet.get(0, 0));

    AdSet adSet2 = AdSet.builder()
      .demandId(1)
      .id(1)
      .spotAds(QualificationTestData.getAds())
      .streamTargetingRule(StreamTargetingRule.builder()
        .tenant(Tenant.India)
        .ruleType(RuleType.Exclude)
        .streamTargetingRuleClauses(Arrays.asList(
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(6)
            .languageId(2)
            .build(),
          StreamTargetingRuleClause.builder()
            .tenant(Tenant.India)
            .platformId(8)
            .languageId(1)
            .languageId(1)
            .build()
        ))
        .build())
      .build();
    streamQualificationEngine.qualify(Collections.singletonList(adSet2));
    Assertions.assertTrue(bitSet.get(0, 1));
  }

}
