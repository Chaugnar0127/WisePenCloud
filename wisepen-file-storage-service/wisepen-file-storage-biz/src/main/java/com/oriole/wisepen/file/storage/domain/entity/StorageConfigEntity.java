package com.oriole.wisepen.file.storage.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oriole.wisepen.file.storage.api.enums.StorageProviderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存储提供商动态配置表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_storage_config")
public class StorageConfigEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 配置名称 (如：阿里云华东1主库、校内 MinIO 备份库) */
    private String name;

    /** 提供商类型 (ALIYUN_OSS, MINIO, TENCENT_COS) */
    private StorageProviderEnum provider;

    /** 云厂商 Region */
    private String region;

    /** 云厂商 Endpoint 或内网连接地址 */
    private String endpoint;

    /** 访问密钥 ID */
    private String accessKeyId;

    /** 访问密钥 Secret */
    private String accessKeySecret;

    /** 存储桶名称 */
    private String bucketName;

    /** 该存储源绑定的访问域名 */
    private String domain;

    /** 角色资源描述符 ARN (用于签发 STS Token) */
    @TableField("role_arn")
    private String roleArn;

    /** 是否为首要存储源 (整个系统只能有一个为 true，图床强制用它) */
    @TableField("is_primary")
    private Boolean isPrimary;

    /** 是否启用该配置 */
    private Boolean enabled;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}