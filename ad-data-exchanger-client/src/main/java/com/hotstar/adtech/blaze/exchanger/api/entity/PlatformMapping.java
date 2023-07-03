package com.hotstar.adtech.blaze.exchanger.api.entity;

import com.hotstar.adtech.blaze.admodel.common.entity.PlatformEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;

@Value
public class PlatformMapping {
  Map<String, PlatformEntity> nameToPlatform;
  Map<Integer, PlatformEntity> idToPlatform;

  @Builder
  public PlatformMapping(List<PlatformEntity> platformEntities) {
    this.nameToPlatform =
      platformEntities.stream().collect(Collectors.toMap(PlatformEntity::getName, Function.identity()));
    this.idToPlatform = platformEntities.stream().collect(Collectors.toMap(PlatformEntity::getId, Function.identity()));
  }

  public PlatformEntity getByName(String name) {
    return Optional.ofNullable(nameToPlatform.get(name))
      .orElse(PlatformEntity.getNullPlatform());
  }

  public PlatformEntity getById(Integer id) {
    return Optional.ofNullable(idToPlatform.get(id))
      .orElse(PlatformEntity.getNullPlatform());
  }
}
