package com.oriole.wisepen.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.wisepen.market.domain.entity.MarketOrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MarketOrderMapper extends BaseMapper<MarketOrderEntity> {
}
