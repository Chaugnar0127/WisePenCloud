package com.oriole.wisepen.user.api.domain.dto;

import com.oriole.wisepen.common.core.domain.enums.GroupType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class GetGroupInfoResponse implements Serializable {
	Long id;
	String name;
	CreatorInfo creator;
	String description;
	GroupType type;
	String coverUrl;
	String inviteCode;
	Integer memberCount;
	LocalDateTime createTime;
}
