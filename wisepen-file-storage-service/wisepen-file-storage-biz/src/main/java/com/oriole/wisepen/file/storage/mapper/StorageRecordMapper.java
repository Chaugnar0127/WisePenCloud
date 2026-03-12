package com.oriole.wisepen.file.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.wisepen.file.storage.domain.entity.StorageRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StorageRecordMapper extends BaseMapper<StorageRecordEntity> {
}