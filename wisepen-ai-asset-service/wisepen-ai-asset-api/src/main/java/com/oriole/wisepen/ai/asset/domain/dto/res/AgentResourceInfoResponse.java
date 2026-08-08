package com.oriole.wisepen.ai.asset.domain.dto.res;

import com.oriole.wisepen.ai.asset.domain.base.AIResourceInfoBase;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResourceInfoResponse {
    ResourceItemResponse resourceInfo;
    AIResourceInfoBase agentInfo;
    private LocalDateTime updateTime;
}
