package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.GroupMemberBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
public class GroupMemberGetTokenResponse {
	Integer tokenUsed;
	Integer tokenBalance;
}
