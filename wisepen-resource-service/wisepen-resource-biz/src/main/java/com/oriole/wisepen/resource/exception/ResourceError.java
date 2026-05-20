package com.oriole.wisepen.resource.exception;
import com.oriole.wisepen.common.core.domain.IResult;
import com.oriole.wisepen.common.core.domain.ResultKey;
import com.oriole.wisepen.common.core.domain.enums.BusinessDomain;
import com.oriole.wisepen.common.core.exception.ErrorReason;
import com.oriole.wisepen.resource.constant.ResourceSubject;
import com.oriole.wisepen.resource.enums.ResourceErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资源微服务(5)专属业务错误
 */
@Getter
@AllArgsConstructor
public enum ResourceError implements IResult {

    // Tag节点相关异常
    TAG_NODE_NOT_FOUND(5111, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_NODE, ErrorReason.NOT_FOUND), ResourceErrorMessage.TAG_NODE_NOT_FOUND.getMsg()),
    PARENT_TAG_NODE_NOT_FOUND(5112, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_NODE, ErrorReason.NOT_FOUND), ResourceErrorMessage.PARENT_TAG_NODE_NOT_FOUND.getMsg()),
    TAG_NODE_NAME_CONFLICT(5121, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_NODE, ErrorReason.CONFLICT), ResourceErrorMessage.TAG_NODE_NAME_CONFLICT.getMsg()),
    CANNOT_SET_TAG_NODE_VISIBILITY(5131, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_NODE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_SET_TAG_NODE_VISIBILITY.getMsg()),

    // Tag路径节点相关异常
    CANNOT_USE_RESERVED_TAG_PATH_NODE_NAME(5211, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_PATH_NODE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_USE_RESERVED_TAG_PATH_NODE_NAME.getMsg()),
    CANNOT_MODIFY_SYSTEM_TAG_PATH_NODE(5212, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_PATH_NODE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_MODIFY_SYSTEM_TAG_PATH_NODE.getMsg()),
    CANNOT_MOVE_SYSTEM_TAG_PATH_NODE(5213, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_PATH_NODE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_MOVE_SYSTEM_TAG_PATH_NODE.getMsg()),
    CANNOT_DELETE_SYSTEM_TAG_PATH_NODE(5214, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_PATH_NODE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_DELETE_SYSTEM_TAG_PATH_NODE.getMsg()),
    CANNOT_DELETE_TAG_PATH_NODE_DIRECTLY(5215, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_PATH_NODE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_DELETE_TAG_PATH_NODE_DIRECTLY.getMsg()),
    CANNOT_OPERATE_TRASHED_TAG_PATH_NODE(5216, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_PATH_NODE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_OPERATE_TRASHED_TAG_PATH_NODE.getMsg()),

    // Tag树相关异常
    CANNOT_MOVE_TAG_NODE_ACROSS_GROUP(5311, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_TREE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_MOVE_TAG_NODE_ACROSS_GROUP.getMsg()),
    CANNOT_MOVE_TAG_NODE_ACROSS_TAG_TYPE(5312, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_TREE, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_MOVE_TAG_NODE_ACROSS_TAG_TYPE.getMsg()),
    CANNOT_MOVE_TAG_NODE_TO_SELF(5321, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_TREE, ErrorReason.UNSUPPORTED), ResourceErrorMessage.CANNOT_MOVE_TAG_NODE_TO_SELF.getMsg()),
    CANNOT_MOVE_TAG_NODE_TO_DESCENDANT(5322, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.TAG_TREE, ErrorReason.UNSUPPORTED), ResourceErrorMessage.CANNOT_MOVE_TAG_NODE_TO_DESCENDANT.getMsg()),

    // 资源相关异常
    RESOURCE_NOT_FOUND(5411, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE, ErrorReason.NOT_FOUND), ResourceErrorMessage.RESOURCE_NOT_FOUND.getMsg()),
    RESOURCE_PERMISSION_DENIED(5421, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE, ErrorReason.PERMISSION_DENIED), ResourceErrorMessage.RESOURCE_PERMISSION_DENIED.getMsg()),
    RESOURCE_TYPE_UNSUPPORTED_FOR_SELL(5431, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.UNSUPPORTED), ResourceErrorMessage.RESOURCE_TYPE_UNSUPPORTED_FOR_SELL.getMsg()),

    // 资源标签相关异常
    CANNOT_BIND_RESOURCE_TO_MULTIPLE_PATH_NODES(5511, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_TAG, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_BIND_RESOURCE_TO_MULTIPLE_PATH_NODES.getMsg()),
    CANNOT_PLACE_RESOURCE_PATH_TAG_AFTER_TAGS(5512, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_TAG, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.CANNOT_PLACE_RESOURCE_PATH_TAG_AFTER_TAGS.getMsg()),
    CANNOT_BIND_MULTIPLE_RESOURCE_TAGS_IN_FOLDER_MODE(5521, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_TAG, ErrorReason.ALREADY_EXISTS), ResourceErrorMessage.CANNOT_BIND_MULTIPLE_RESOURCE_TAGS_IN_FOLDER_MODE.getMsg()),

    // 资源市场相关异常
    SELL_INFO_NOT_FOUND(5611, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.NOT_FOUND), ResourceErrorMessage.SELL_INFO_NOT_FOUND.getMsg()),
    SELL_INFO_NOT_PURCHASABLE(5612, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.STATE_INVALID), ResourceErrorMessage.SELL_INFO_NOT_PURCHASABLE.getMsg()),
    SELL_INFO_ALREADY_LISTED(5621, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.CONFLICT), ResourceErrorMessage.SELL_INFO_ALREADY_LISTED.getMsg()),
    RESOURCE_RESELL_NOT_ALLOWED(5631, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.RESOURCE_RESELL_NOT_ALLOWED.getMsg()),
    SUBSCRIPTION_FORK_NOT_ALLOWED(5632, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.NOT_ALLOWED), ResourceErrorMessage.SUBSCRIPTION_FORK_NOT_ALLOWED.getMsg()),
    RESOURCE_VERSION_UNSUPPORTED(5641, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.UNSUPPORTED), ResourceErrorMessage.RESOURCE_VERSION_UNSUPPORTED.getMsg()),
    RESOURCE_MARKET_OPERATION_UNSUPPORTED(5642, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.UNSUPPORTED), ResourceErrorMessage.RESOURCE_MARKET_OPERATION_UNSUPPORTED.getMsg()),
    RESOURCE_MARKET_TRADE_SETTLE_FAILED(5651, new ResultKey(BusinessDomain.RESOURCE, ResourceSubject.RESOURCE_MARKET, ErrorReason.EXTERNAL_FAILED), ResourceErrorMessage.RESOURCE_MARKET_TRADE_SETTLE_FAILED.getMsg());

    private final Integer code;
    private final ResultKey key;
    private final String msg;
}
