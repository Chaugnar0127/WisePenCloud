package com.oriole.wisepen.user.api.constant;

public interface MessageTemplatePlaceholders {
    String USER_TEMPLATE = "{{USER:%s}}";
    String GROUP_TEMPLATE = "{{GROUP:%s}}";

    static String user(Object userId) {
        return String.format(USER_TEMPLATE, userId);
    }

    static String group(Object groupId) {
        return String.format(GROUP_TEMPLATE, groupId);
    }
}
