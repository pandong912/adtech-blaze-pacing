package com.hotstar.adtech.blaze.exchanger.api.entity;

import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;

@Value
public class LanguageMapping {
  Map<String, LanguageEntity> nameToPlatform;
  Map<Integer, LanguageEntity> idToPlatform;

  @Builder
  public LanguageMapping(List<LanguageEntity> languageEntities) {
    this.nameToPlatform =
      languageEntities.stream().collect(Collectors.toMap(LanguageEntity::getName, Function.identity()));
    this.idToPlatform = languageEntities.stream().collect(Collectors.toMap(LanguageEntity::getId, Function.identity()));
  }

  public LanguageEntity getByName(String name) {
    return Optional.ofNullable(nameToPlatform.get(name))
      .orElse(LanguageEntity.getNullLanguage());
  }

  public LanguageEntity getById(Integer id) {
    return Optional.ofNullable(idToPlatform.get(id))
      .orElse(LanguageEntity.getNullLanguage());
  }
}
