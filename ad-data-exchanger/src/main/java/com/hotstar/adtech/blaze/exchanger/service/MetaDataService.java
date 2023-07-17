package com.hotstar.adtech.blaze.exchanger.service;

import com.hotstar.adtech.blaze.admodel.common.entity.LanguageEntity;
import com.hotstar.adtech.blaze.admodel.common.entity.PlatformEntity;
import com.hotstar.adtech.blaze.admodel.repository.LanguageRepository;
import com.hotstar.adtech.blaze.admodel.repository.PlatformRepository;
import com.hotstar.adtech.blaze.admodel.repository.model.Language;
import com.hotstar.adtech.blaze.admodel.repository.model.Platform;
import com.hotstar.adtech.blaze.exchanger.api.entity.LanguageMapping;
import com.hotstar.adtech.blaze.exchanger.api.entity.PlatformMapping;
import com.hotstar.adtech.blaze.exchanger.config.CacheConfig;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaDataService {
  private final LanguageRepository languageRepository;
  private final PlatformRepository platformRepository;

  @Cacheable(cacheNames = CacheConfig.LANGUAGE,
    cacheManager = CacheConfig.DATABASE_CACHE_MANAGER,
    sync = true)
  public LanguageMapping getLanguageMapping() {
    List<LanguageEntity> languageEntities =
      languageRepository.findAll().stream().map(this::buildLanguage).collect(Collectors.toList());
    return LanguageMapping.builder()
      .languageEntities(languageEntities)
      .build();
  }

  @Cacheable(cacheNames = CacheConfig.PLATFORM,
    cacheManager = CacheConfig.DATABASE_CACHE_MANAGER,
    sync = true)
  public PlatformMapping getPlatformMapping() {
    List<PlatformEntity> platformEntities =
      platformRepository.findAll().stream().map(this::buildPlatform).collect(Collectors.toList());
    return PlatformMapping.builder()
      .platformEntities(platformEntities)
      .build();
  }

  private PlatformEntity buildPlatform(Platform platform) {
    return PlatformEntity.builder()
      .id(platform.getId())
      .tag(platform.getTag())
      .name(platform.getName())
      .build();
  }

  private LanguageEntity buildLanguage(Language language) {
    return LanguageEntity.builder()
      .id(language.getId())
      .tag(language.getTag())
      .name(language.getName())
      .abbreviation(language.getAbbreviation())
      .build();
  }

}

