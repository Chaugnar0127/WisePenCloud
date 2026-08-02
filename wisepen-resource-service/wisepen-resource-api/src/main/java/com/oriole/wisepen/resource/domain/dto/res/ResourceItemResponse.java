package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.enums.ResourceAction;
import com.oriole.wisepen.user.api.domain.base.GroupDisplayBase;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResourceItemResponse extends ResourceBaseInfoResponse {

    private List<ResourceTagBindResponse> tagBinds;
    private List<ResourceAction> currentActions;
    private ResourceUserInteractionRecordResponse myInteractionRecord;

    private List<GroupGrantedActionsResponse> overrideGrantedActions;
    private List<SpecifiedUserGrantedActionsResponse> specifiedUsersGrantedActions;

    private Map<String, MarketSaleInfoResponse> marketSaleInfos = new HashMap<>();

    @Builder
    @Data
    public static class GroupGrantedActionsResponse {
        private String groupId;
        private GroupDisplayBase groupInfo;
        private List<ResourceAction> grantedActions;
    }

    @Builder
    @Data
    public static class SpecifiedUserGrantedActionsResponse {
        private String userId;
        private UserDisplayBase userInfo;
        private List<ResourceAction> grantedActions;
    }
}
