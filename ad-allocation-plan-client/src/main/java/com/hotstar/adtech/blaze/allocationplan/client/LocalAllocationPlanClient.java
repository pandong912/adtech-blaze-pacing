package com.hotstar.adtech.blaze.allocationplan.client;

import com.hotstar.adtech.blaze.admodel.common.enums.AlgorithmType;
import com.hotstar.adtech.blaze.admodel.common.enums.PlanType;
import com.hotstar.adtech.blaze.allocation.planner.common.response.diagnosis.AllocationDiagnosis;
import com.hotstar.adtech.blaze.allocation.planner.common.response.hwm.HwmAllocationPlan;
import com.hotstar.adtech.blaze.allocation.planner.common.response.shale.ShaleAllocationPlan;
import com.hotstar.adtech.blaze.allocationplan.client.common.GzipUtils;
import com.hotstar.adtech.blaze.allocationplan.client.common.PathUtils;
import com.hotstar.adtech.blaze.allocationplan.client.common.ProtostuffUtils;
import com.hotstar.adtech.blaze.allocationplan.client.model.LoadRequest;
import com.hotstar.adtech.blaze.allocationplan.client.model.UploadResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
@SuppressWarnings("unused")
public class LocalAllocationPlanClient implements AllocationPlanClient {
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


  public <T> T loadFromLocal(Class<T> clazz, String path, String fileName) {
    try {
      byte[] bytes = Files.readAllBytes(Paths.get(baseDir, path, fileName));
      return ProtostuffUtils.deserialize(bytes, clazz);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public UploadResult uploadShalePlan(String path, ShaleAllocationPlan allocationPlan) {
    byte[] file = ProtostuffUtils.serialize(allocationPlan);
    String md5 = DigestUtils.md5Hex(file);
    path = Paths.get(baseDir, path).toString();
    String fileName = doUpload(path, file, md5);
    return UploadResult.builder()
      .breakTypeId(-1L)
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
      .breakTypeId(allocationPlan.getBreakTypeId())
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

  private String doUpload(String path, byte[] file, String md5) {
    String id = UUID.randomUUID().toString();
    String fileName = id + md5;
    try {
      Path filePath = Paths.get(path, fileName);
      log.info("test file path: {}", filePath);
      Path parentDir = filePath.getParent();
      Files.createDirectories(parentDir);
      Files.write(filePath, file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return fileName;
  }

  public void uploadAllocationDiagnosis(AllocationDiagnosis allocationDiagnosis) {
    try {
      byte[] value = ProtostuffUtils.serialize(allocationDiagnosis);
      Path path = Paths.get(baseDir,
        PathUtils.joinToDiagnosisPath(allocationDiagnosis.getContentId(), allocationDiagnosis.getVersion()));
      Path parentDir = path.getParent();
      Files.createDirectories(parentDir);
      Files.write(path, GzipUtils.compress(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public AllocationDiagnosis loadAllocationDiagnosis(String path) {
    try {
      byte[] bytes = Files.readAllBytes(Paths.get(path));
      byte[] decompressed = GzipUtils.decompress(bytes);
      return ProtostuffUtils.deserialize(decompressed, AllocationDiagnosis.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
