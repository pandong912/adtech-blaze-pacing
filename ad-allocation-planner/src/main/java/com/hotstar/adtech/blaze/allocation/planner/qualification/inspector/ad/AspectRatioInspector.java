package com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.ad;

import com.hotstar.adtech.blaze.admodel.common.enums.StreamView;
import com.hotstar.adtech.blaze.allocation.planner.common.admodel.Ad;
import com.hotstar.adtech.blaze.allocation.planner.qualification.inspector.Inspector;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;


@RequiredArgsConstructor
public class AspectRatioInspector implements Inspector<Ad> {
  private static final List<String> verticalStreamBlackList = Arrays.asList("16:9", "1:1");
  private static final List<String> normalStreamBlackList = Arrays.asList("9:14", "9:16", "1:1");

  private final StreamView streamView;

  public AspectRatioInspector(String languageName) {
    this.streamView = StreamView.fromLanguageName(languageName);
  }

  @Override
  public boolean qualify(Ad ad) {
    if (streamView == StreamView.VERTICAL) {
      return !(!StringUtils.isEmpty(ad.getAspectRatio()) && verticalStreamBlackList.contains(ad.getAspectRatio()));
    }
    return !(!StringUtils.isEmpty(ad.getAspectRatio()) && normalStreamBlackList.contains(ad.getAspectRatio()));
  }
}