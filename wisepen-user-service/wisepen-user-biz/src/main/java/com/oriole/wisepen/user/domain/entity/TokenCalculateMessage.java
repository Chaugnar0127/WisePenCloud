package com.oriole.wisepen.user.domain.entity;

import com.oriole.wisepen.common.core.domain.enums.ModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenCalculateMessage implements Serializable{
	private Long userId;
	private Long groupId;
	private Integer usageTokens;
	private Long traceId;
	private ModelType modelType;
	private LocalDateTime requestTime;
}