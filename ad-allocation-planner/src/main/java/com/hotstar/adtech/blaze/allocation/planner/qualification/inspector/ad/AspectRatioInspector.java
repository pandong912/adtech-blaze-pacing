package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad;

import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.Inspector;
import com.hotstar.adtech.blaze.allocation.planner.source.admodel.Ad;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;


@RequiredArgsConstructor
public class AspectRatioInspector implements Inspector<Ad> {
  private final String languageName;
  private final List<String> verticalStreamBlackList = Arrays.asList("16:9", "1:1");
  private final List<String> normalStreamBlackList = Arrays.asList("9:14", "9:16", "1:1");

  @Override
  public boolean qualify(Ad ad) {
    if (checkIfVerticalStream(languageName)) {
      return !(!StringUtils.isEmpty(ad.getAspectRatio()) && verticalStreamBlackList.contains(ad.getAspectRatio()));
    }
    return !(!StringUtils.isEmpty(ad.getAspectRatio()) && normalStreamBlackList.contains(ad.getAspectRatio()));
  }

  public boolean checkIfVerticalStream(String languageName) {
    return languageName.toLowerCase().contains("vertical");
  }
}