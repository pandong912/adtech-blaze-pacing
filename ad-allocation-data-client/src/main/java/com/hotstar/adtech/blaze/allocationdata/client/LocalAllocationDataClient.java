package com.hotstar.adtech.blaze.allocationdata.client;

import com.hotstar.adtech.blaze.admodel.common.exception.BusinessException;
import com.hotstar.adtech.blaze.allocationdata.client.model.GeneralPlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.model.ShalePlanContext;
import com.hotstar.adtech.blaze.allocationdata.client.util.GzipUtils;
import com.hotstar.adtech.blaze.allocationdata.client.util.ProtostuffUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unused")
public class LocalAllocationDataClient implements AllocationDataClient {

  private static final String ALLOCATION_PLAN_DATA = "allocation-plan-data";
  private static final String SHALE_PLAN_DATA = "shale-plan-data";
  private static final String HWM_PLAN_DATA = "hwm-plan-data";
  private final String baseDir;

  public LocalAllocationDataClient(String baseDir) {
    this.baseDir = baseDir;
  }


  public <T> T loadFromLocal(Class<T> clazz, String path, String fileName) {
    Path key = Paths.get(baseDir, path, fileName);
    try {
      byte[] bytes = Files.readAllBytes(key);
      byte[] decompress = GzipUtils.decompress(bytes);
      return ProtostuffUtils.deserialize(decompress, clazz);
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_LOAD_FAILED, e, key);
    }
  }


  private String doUpload(String path, byte[] file, String fileName) {
    Path filePath = Paths.get(path, fileName);
    try {
      log.info("local file path: {}", filePath);
      Path parentDir = filePath.getParent();
      Files.createDirectories(parentDir);
      byte[] compress = GzipUtils.compress(file);
      Files.write(filePath, compress);
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_UPLOAD_FAILED, e, filePath);
    }
    return fileName;
  }


  @Override
  public void uploadShaleData(String contentId, String version, ShalePlanContext shalePlanContext) {
    byte[] file = ProtostuffUtils.serialize(shalePlanContext);
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId).toString();
    String fileName = doUpload(path, file, SHALE_PLAN_DATA);
  }

  @Override
  public void uploadHwmData(String contentId, String version, GeneralPlanContext generalPlanContext) {
    byte[] file = ProtostuffUtils.serialize(generalPlanContext);
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId).toString();
    String fileName = doUpload(path, file, HWM_PLAN_DATA);
  }

  @Override
  public ShalePlanContext loadShaleData(String contentId, String version) {
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId).toString();
    return loadFromLocal(ShalePlanContext.class, path, SHALE_PLAN_DATA);
  }

  @Override
  public GeneralPlanContext loadHwmData(String contentId, String version) {
    String path = Paths.get(ALLOCATION_PLAN_DATA, contentId).toString();
    return loadFromLocal(GeneralPlanContext.class, path, HWM_PLAN_DATA);
  }
}
