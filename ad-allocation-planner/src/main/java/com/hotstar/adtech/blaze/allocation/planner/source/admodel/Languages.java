package com.hotstar.adtech.blaze.allocation.planner.source.admodel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

public class Languages {

  private final Map<Integer, Language> idToLanguageMap;
  private final Map<String, Language> tagToLanguageMap;
  private final Map<String, Language> nameToLanguageMap;

  public Languages(List<Language> languages) {
    if (CollectionUtils.isEmpty(languages)) {
      this.idToLanguageMap = Collections.emptyMap();
      this.tagToLanguageMap = Collections.emptyMap();
      this.nameToLanguageMap = Collections.emptyMap();
    } else {
      this.idToLanguageMap = languages.stream().collect(Collectors.toMap(Language::getId, Function.identity()));
      this.tagToLanguageMap = languages.stream().collect(Collectors.toMap(Language::getTag, Function.identity()));
      this.nameToLanguageMap = languages.stream()
        .collect(Collectors.toMap(language -> language.getName(), Function.identity()));
    }
  }

  public Language getById(Integer languageId) {
    return idToLanguageMap.get(languageId);
  }

  public Language getByTag(String tag) {
    return Optional.ofNullable(tag)
      .map(String::toUpperCase)
      .map(tagToLanguageMap::get).orElse(null);
  }

  public Language getByName(String name) {
    return nameToLanguageMap.get(name);
  }

}
