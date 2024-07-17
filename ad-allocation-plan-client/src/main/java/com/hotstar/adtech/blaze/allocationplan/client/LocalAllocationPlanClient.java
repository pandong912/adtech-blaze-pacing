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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
  public Map<Long, ShaleAllocationPlan> loadShaleAllocationPlans(PlanType planType, List<LoadRequest> loadRequests) {
    return loadShaleFromLocal().stream()
      .collect(Collectors.toMap(plan -> (long) plan.getTags().hashCode(), Function.identity()));
  }

  @Override
  public Map<Long, HwmAllocationPlan> loadHwmAllocationPlans(PlanType planType, List<LoadRequest> loadRequests) {
    return loadHwmFromLocal(planType).stream()
      .collect(Collectors.toMap(plan -> (long) plan.getTags().hashCode(), Function.identity()));
  }

  @Override
  public SupplyInfo loadSupplyIdMap(String path) {
    return loadSupplyInfo();
  }

  public SupplyInfo loadSupplyInfo() {
    Path key = Paths.get(baseDir, SUPPLY_ID_MAP_FILE_NAME);
    try {
      byte[] bytes = Files.readAllBytes(key);
      return ProtostuffUtils.deserialize(bytes, SupplyInfo.class);
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_LOAD_FAILED, e, key);
    }
  }


  public List<ShaleAllocationPlan> loadShaleFromLocal() {
    Path dir = Paths.get(baseDir);
    List<ShaleAllocationPlan> shaleAllocationPlans = new ArrayList<>();
    try (DirectoryStream<Path> pathStream = Files.newDirectoryStream(dir, "shale*")) {
      for (Path entry : pathStream) {
        byte[] bytes = Files.readAllBytes(entry);
        ShaleAllocationPlan deserialize = ProtostuffUtils.deserialize(bytes, ShaleAllocationPlan.class);
        shaleAllocationPlans.add(deserialize);
      }
      return shaleAllocationPlans;
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_LOAD_FAILED, e, dir);
    }
  }

  public List<HwmAllocationPlan> loadHwmFromLocal(PlanType planType) {
    Path dir = Paths.get(baseDir);
    List<HwmAllocationPlan> hwmAllocationPlans = new ArrayList<>();
    String prefix = "hwm" + "|" + planType;
    try (DirectoryStream<Path> pathStream = Files.newDirectoryStream(dir, prefix + "*")) {
      for (Path entry : pathStream) {
        byte[] bytes = Files.readAllBytes(entry);
        HwmAllocationPlan deserialize = ProtostuffUtils.deserialize(bytes, HwmAllocationPlan.class);
        hwmAllocationPlans.add(deserialize);
      }
      return hwmAllocationPlans;
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_LOAD_FAILED, e, dir);
    }
  }

  @Override
  public UploadResult uploadShalePlan(String path, ShaleAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    path = Paths.get(baseDir, path).toString();
    String fileName = allocationPlan.getTags() + "|" + md5;
    doUpload(path, file, fileName);
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
  public UploadResult uploadHwmPlan(String path, HwmAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    path = Paths.get(baseDir, path).toString();
    String fileName = allocationPlan.getTags() + "|" + md5;
    doUpload(path, file, fileName);
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
  public void uploadSupplyIdMap(String path, Map<String, Integer> supplyIdMap) {
    SupplyInfo supplyInfo = SupplyInfo.builder()
      .supplyIdMap(supplyIdMap)
      .build();
    byte[] file = ProtostuffUtils.serialize(supplyInfo);
    path = Paths.get(baseDir, path).toString();
    doUpload(path, file, SUPPLY_ID_MAP_FILE_NAME);
  }

  private void doUpload(String path, byte[] file, String fileName) {
    Path filePath = Paths.get(path, fileName);
    try {
      log.info("test file path: {}", filePath);
      Path parentDir = filePath.getParent();
      Files.createDirectories(parentDir);
      Files.write(filePath, file);
    } catch (IOException e) {
      throw new BusinessException(ErrorCodes.ALLOCATION_DATA_UPLOAD_FAILED, e, filePath);
    }
  }
}
