package com.oriole.wisepen.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资源微服务业务错误消息枚举
 * 与 UserErrorMessage 保持风格一致，供 ResourceError 引用
 */
@Getter
@AllArgsConstructor
public enum ResourceErrorMessage {

    // Tag节点相关
    TAG_NODE_NOT_FOUND("标签节点不存在"),
    PARENT_TAG_NODE_NOT_FOUND("父标签节点不存在"),
    TAG_NODE_NAME_CONFLICT("同级目录下已存在同名标签节点"),
    CANNOT_SET_TAG_NODE_VISIBILITY("不能设置个人标签节点的可见范围"),

    // Tag路径节点相关
    CANNOT_USE_RESERVED_TAG_PATH_NODE_NAME("不能使用系统保留名称(/ 或 .Trash)作为路径节点名称"),
    CANNOT_MODIFY_SYSTEM_TAG_PATH_NODE("不能修改系统路径节点"),
    CANNOT_MOVE_SYSTEM_TAG_PATH_NODE("不能移动系统路径节点"),
    CANNOT_DELETE_SYSTEM_TAG_PATH_NODE("不能删除系统路径节点"),
    CANNOT_DELETE_TAG_PATH_NODE_DIRECTLY("不能直接删除路径节点，请先移入回收站"),
    CANNOT_OPERATE_TRASHED_TAG_PATH_NODE("不能操作回收站内的路径节点"),

    // Tag树相关
    CANNOT_MOVE_TAG_NODE_ACROSS_GROUP("不能跨小组移动标签节点"),
    CANNOT_MOVE_TAG_NODE_ACROSS_TAG_TYPE("不能跨节点类型(目录/标签)移动或挂载标签节点"),
    CANNOT_MOVE_TAG_NODE_TO_SELF("不能将标签节点移动到自身之下"),
    CANNOT_MOVE_TAG_NODE_TO_DESCENDANT("不能将标签节点移动到其子孙节点之下"),

    // 资源相关
    RESOURCE_NOT_FOUND("资源不存在"),
    RESOURCE_PERMISSION_DENIED("无权访问或操作该资源"),
    RESOURCE_TYPE_UNSUPPORTED_FOR_SELL("该资源类型不支持上架"),

    // 资源标签相关
    CANNOT_BIND_RESOURCE_TO_MULTIPLE_PATH_NODES("不能为资源绑定多个路径节点"),
    CANNOT_PLACE_RESOURCE_PATH_TAG_AFTER_TAGS("不能将资源路径标签放在普通标签之后"),
    CANNOT_BIND_MULTIPLE_RESOURCE_TAGS_IN_FOLDER_MODE("该资源已绑定标签，文件夹模式下不能重复绑定"),

    // 资源市场相关
    SELL_INFO_NOT_FOUND("售卖信息不存在"),
    SELL_INFO_NOT_PURCHASABLE("售卖信息当前不可购买"),
    SELL_INFO_ALREADY_LISTED("该资源已存在同类在售信息"),
    RESOURCE_RESELL_NOT_ALLOWED("当前资源不允许二次出售"),
    SUBSCRIPTION_FORK_NOT_ALLOWED("当前资源不允许订阅 fork"),
    RESOURCE_VERSION_UNSUPPORTED("该资源版本不支持此操作"),
    RESOURCE_MARKET_OPERATION_UNSUPPORTED("资源市场操作暂不支持"),
    RESOURCE_MARKET_TRADE_SETTLE_FAILED("资源市场交易结算失败");

    private final String msg;
}
