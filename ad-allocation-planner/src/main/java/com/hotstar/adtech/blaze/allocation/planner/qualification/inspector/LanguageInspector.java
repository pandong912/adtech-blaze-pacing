package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector;

import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LanguageInspector implements Inspector<Ad> {

  private final Integer languageId;

  @Override
  public boolean qualify(Ad ad) {
    return ad.getLanguageIds().contains(languageId);
  }
}
