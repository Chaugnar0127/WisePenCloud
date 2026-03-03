package com.oriole.wisepen.user.api.domain.dto;

import com.oriole.wisepen.common.core.domain.enums.GroupType;
import lombok.Data;

import java.io.Serializable;

@Data
public class GroupQueryResponse implements Serializable {
	private Long id;
	private String name;
	private CreatorInfo creator;
	private String description;
	private GroupType type;
	private String coverUrl;
	private String inviteCode;
	private Integer memberCount;
}
