package com.oriole.wisepen.common.core.constant;

public class SecurityConstants {
    /** Cookie的Authorization Token */
    public static final String AUTHORIZATION_TOKEN = "authorization";

    /** 网关透传的用户ID Header Key */
    public static final String HEADER_USER_ID = "X-User-Id";

    /** 网关透传的用户身份 Header Key */
    public static final String HEADER_IDENTITY_TYPE = "X-Identity-Type";

    /** 网关透传的用户账号状态 Header Key */
    public static final String HEADER_USER_STATUS = "X-User-Status";

    /** 网关透传的组ID Header Key */
    public static final String HEADER_GROUP_ROLE_MAP = "X-Group-Role-Map";

    /** 内部服务调用时的鉴权 Header (防止绕过网关直连) */
    public static final String HEADER_FROM_SOURCE = "X-From-Source";

    /** 正常账号状态，取值需与用户服务 Status.NORMAL 保持一致 */
    public static final Integer USER_STATUS_NORMAL = 1;

    /** 未完成身份认证账号状态，取值需与用户服务 Status.UNIDENTIFIED 保持一致 */
    public static final Integer USER_STATUS_UNIDENTIFIED = -1;
}
