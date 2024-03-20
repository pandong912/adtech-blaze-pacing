package com.hotstar.adtech.blaze.allocationplan.client;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.admodel.common.exception.BusinessException;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.SupplyInfo;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import com.hotstar.adtech.blaze.allocationplan.client.util.ProtostuffUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
@SuppressWarnings("unused")
public class LocalAllocationPlanClient implements AllocationPlanClient {

  private static final String SUPPLY_ID_MAP_FILE_NAME = "streamCohortToSupplyId";
  private final String baseDir;

  public LocalAllocationPlanClient(String baseDir) {
    this.baseDir = baseDir;
  }


  @Override
  public List<ShaleAllocationPlan> loadShaleAllocationPlans(List<LoadRequest> loadRequests) {
    return loadRequests.parallelStream()
      .map(loadRequest -> loadFromLocal(ShaleAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName()))
      .collect(Collectors.toList());
  }

  @Override
  public List<HwmAllocationPlan> loadHwmAllocationPlans(List<LoadRequest> loadRequests) {
    return loadRequests.parallelStream()
      .map(loadRequest -> loadFromLocal(HwmAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName()))
      .collect(Collectors.toList());
  }

  @Override
  public ShaleAllocationPlan loadShaleAllocationPlan(LoadRequest loadRequest) {
    return loadFromLocal(ShaleAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName());
  }

  @Override
  public HwmAllocationPlan loadHwmAllocationPlan(LoadRequest loadRequest) {
    return loadFromLocal(HwmAllocationPlan.class, loadRequest.getPath(), loadRequest.getFileName());
  }

  @Override
  public SupplyInfo loadSupplyIdMap(String path) {
    return loadFromLocal(SupplyInfo.class, path, SUPPLY_ID_MAP_FILE_NAME);
  }


  public <T> T loadFromLocal(Class<T> clazz, String path, String fileName) {
    Path key = Paths.get(baseDir, path, fileName);
    try {
      byte[] bytes = Files.readAllBytes(key);
      return ProtostuffUtils.deserialize(bytes, clazz);
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_LOAD_FAILED, e, key);
    }
  }

  @Override
  public UploadResult uploadShalePlan(String path, ShaleAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    path = Paths.get(baseDir, path).toString();
    String fileName = doUpload(path, file, md5);
    return UploadResult.builder()
      .breakTypeIds(allocationPlan.getBreakTypeIds())
      .planType(PlanType.SSAI)
      .algorithmType(AlgorithmType.SHALE)
      .duration(allocationPlan.getDuration())
      .nextBreakIndex(allocationPlan.getNextBreakIndex())
      .totalBreakNumber(allocationPlan.getTotalBreakNumber())
      .fileName(fileName)
      .md5(md5)
      .build();
  }

  @Override
  public List<UploadResult> batchUploadShalePlan(String path, List<ShaleAllocationPlan> allocationPlans) {
    return allocationPlans.stream()
      .map(allocationPlan -> uploadShalePlan(path, allocationPlan))
      .collect(Collectors.toList());
  }

  @Override
  public UploadResult uploadHwmPlan(String path, HwmAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    path = Paths.get(baseDir, path).toString();
    String fileName = doUpload(path, file, md5);
    return UploadResult.builder()
      .breakTypeIds(allocationPlan.getBreakTypeIds())
      .algorithmType(AlgorithmType.HWM)
      .planType(allocationPlan.getPlanType())
      .duration(allocationPlan.getDuration())
      .nextBreakIndex(allocationPlan.getNextBreakIndex())
      .totalBreakNumber(allocationPlan.getTotalBreakNumber())
      .fileName(fileName)
      .md5(md5)
      .build();
  }

  @Override
  public List<UploadResult> batchUploadHwmPlan(String path, List<HwmAllocationPlan> allocationPlans) {
    return allocationPlans.stream()
      .map(allocationPlan -> uploadHwmPlan(path, allocationPlan))
      .collect(Collectors.toList());
  }

  @Override
  public void uploadSupplyIdMap(String path, Map<String, Integer> supplyIdMap) {
    SupplyInfo supplyInfo = SupplyInfo.builder()
      .supplyIdMap(supplyIdMap)
      .build();
    byte[] file = ProtostuffUtils.serialize(supplyInfo);
    path = Paths.get(baseDir, path).toString();
    doUpload(path, file, SUPPLY_ID_MAP_FILE_NAME);
  }

  private String doUpload(String path, byte[] file, String fileName) {
    Path filePath = Paths.get(path, fileName);
    try {
      log.info("test file path: {}", filePath);
      Path parentDir = filePath.getParent();
      Files.createDirectories(parentDir);
      Files.write(filePath, file);
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_UPLOAD_FAILED, e, filePath);
    }
    return fileName;
  }
}
