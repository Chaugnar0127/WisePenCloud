package com.oriole.wisepen.file.storage.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.file.storage.api.domain.dto.StorageRecordDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitReqDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.StsTokenDTO;
import com.oriole.wisepen.file.storage.api.domain.dto.UploadInitRespDTO;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.service.IStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "内部文件存储", description = "供其他微服务初始化上传、获取下载地址与管理物理文件")
@RestController
@RequestMapping("/internal/storage/")
@RequiredArgsConstructor
public class InternalStorageController {

    private final IStorageService storageService;

    /**
     * 初始化上传（颁发 PUT URL 或触发秒传）
     */
    @Operation(summary = "内部初始化文件上传")
    @PostMapping("/initUpload")
    public R<UploadInitRespDTO> initUpload(@Validated @RequestBody UploadInitReqDTO req) {
        return R.ok(storageService.initUpload(req));
    }

    /**
     * 获取单文件的防盗链下载 URL
     */
    @Operation(summary = "内部获取文件下载地址")
    @GetMapping("/getDownloadUrl")
    public R<String> getDownloadUrl(
            @RequestParam("objectKey") String objectKey,
            @RequestParam(value = "duration", defaultValue = "900") Long duration) {
        return R.ok(storageService.getDownloadUrl(objectKey, duration));
    }

    /**
     * 颁发 STS 临时凭证（用于前端批量访问特定目录下的图片）
     */
    @Operation(summary = "内部获取临时访问凭证")
    @GetMapping("/getStsToken")
    public R<StsTokenDTO> getStsToken(
            @RequestParam("scene") StorageSceneEnum scene,
            @RequestParam(value = "bizTag", required = false) String bizTag,
            @RequestParam(value = "configId", required = false) Long configId,
            @RequestParam(value = "durationSeconds", defaultValue = "3600") Long durationSeconds) {
        return R.ok(storageService.getStsToken(scene, bizTag, configId, durationSeconds));
    }

    /**
     * 物理删除文件
     */
    @Operation(summary = "内部物理删除文件")
    @DeleteMapping("/deleteFiles")
    public R<Void> deleteFiles(@RequestBody List<String> objectKeys) {
        storageService.deleteFiles(objectKeys);
        return R.ok();
    }

    /**
     * 获取文件物理记录明细 (主动查单/补偿)
     */
    @Operation(summary = "内部获取文件记录")
    @GetMapping("/getFileRecord")
    public R<StorageRecordDTO> getFileRecord(@RequestParam("objectKey") String objectKey) {
        return R.ok(storageService.getFileRecord(objectKey));
    }
}
