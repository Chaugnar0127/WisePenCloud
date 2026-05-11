package com.oriole.wisepen.market.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.wisepen.market.domain.entity.UserInfoPointEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfoPointMapper extends BaseMapper<UserInfoPointEntity> {
}
