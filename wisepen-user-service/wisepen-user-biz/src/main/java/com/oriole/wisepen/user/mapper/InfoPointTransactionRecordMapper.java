package com.oriole.wisepen.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeSearchRequest;
import com.oriole.wisepen.user.domain.entity.InfoPointTransactionRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InfoPointTransactionRecordMapper extends BaseMapper<InfoPointTransactionRecordEntity> {

    @Select("""
            <script>
            SELECT COUNT(DISTINCT related_id)
            FROM sys_mkt_info_point_record
            WHERE related_id IS NOT NULL
              AND user_id = #{req.userId}
            <if test="req.changeType != null">
              AND change_type = #{req.changeType.code}
            </if>
            <if test="req.tradeStatus != null">
              AND trade_status = #{req.tradeStatus.code}
            </if>
            <if test="req.changeAmount != null">
              AND change_amount = #{req.changeAmount}
            </if>
            </script>
            """)
    long countDistinctRelatedIds(@Param("req") InfoPointTradeSearchRequest req);

    @Select("""
            <script>
            SELECT *
            FROM sys_mkt_info_point_record
            WHERE record_id IN (
                SELECT MAX(record_id)
                FROM sys_mkt_info_point_record
                WHERE related_id IS NOT NULL
                  AND user_id = #{req.userId}
                <if test="req.changeType != null">
                  AND change_type = #{req.changeType.code}
                </if>
                <if test="req.tradeStatus != null">
                  AND trade_status = #{req.tradeStatus.code}
                </if>
                <if test="req.changeAmount != null">
                  AND change_amount = #{req.changeAmount}
                </if>
                GROUP BY related_id
            )
            ORDER BY create_time DESC
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<InfoPointTransactionRecordEntity> selectRelatedIdMatches(
            @Param("req") InfoPointTradeSearchRequest req,
            @Param("offset") long offset,
            @Param("size") int size
    );
}
