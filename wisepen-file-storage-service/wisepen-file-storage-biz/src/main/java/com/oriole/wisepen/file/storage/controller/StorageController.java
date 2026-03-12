package com.oriole.wisepen.file.storage.controller;

import cn.hutool.core.io.FileUtil;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.file.storage.api.domain.dto.StorageRecordDTO;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.exception.StorageErrorCode;
import com.oriole.wisepen.file.storage.service.IStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@RestController
@RequestMapping("/storage/")
@RequiredArgsConstructor
@CheckLogin
public class StorageController {

    private final IStorageService storageService;

    /**
     * 图床代理上传
     * @param file     图片文件
     * @param isPublic 是否为公开图片
     * @param bizPath  业务路径隔离标识
     */
    @PostMapping("/imageUpload")
    public R<StorageRecordDTO> uploadImageProxy(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic,
            @RequestParam(value = "bizPath", required = false) String bizPath) {

        String extension = FileUtil.extName(file.getOriginalFilename()).toLowerCase();
        if (!Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
            throw new ServiceException(StorageErrorCode.FILE_TYPE_UNSUPPORTED);
        }
        StorageSceneEnum scene = isPublic ? StorageSceneEnum.PUBLIC_IMAGE : StorageSceneEnum.PRIVATE_IMAGE;
        StorageRecordDTO record = storageService.uploadSmallFileProxy(file, scene, bizPath);
        return R.ok(record);
    }
}