package com.oriole.wisepen.file.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.wisepen.file.storage.domain.entity.StorageConfigEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StorageConfigMapper extends BaseMapper<StorageConfigEntity> {
}