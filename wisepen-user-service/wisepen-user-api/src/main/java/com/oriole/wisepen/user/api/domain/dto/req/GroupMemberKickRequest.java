package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.constant.GroupValidationMsg;
import com.oriole.wisepen.user.api.domain.base.GroupIdentityBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupMemberKickRequest extends GroupIdentityBase {
	@NotBlank(message = GroupValidationMsg.TARGET_USER_IDS_NOT_EMPTY)
	private List<String> targetUserIds;
}