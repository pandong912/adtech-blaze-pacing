package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import com.hotstar.adtech.blaze.admodel.client.model.LanguageInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Language {
  private static final Language DEFAULT = Language.builder().id(-1).name("").build();

  private final Integer id;
  private final String name;
  private final String tag;

  public static Language getNullLanguage() {
    return DEFAULT;
  }

  public LanguageInfo toLanguageInfo() {
    return LanguageInfo.builder().id(id).name(name).tag(tag).build();
  }
}
