package com.oriole.wisepen.resource.constant;

import java.util.List;

public interface ResourceConstants {
    String PERSONAL_GROUP_PREFIX = "p_";
    String MARKET_GROUP_PREFIX = "M_";

    // 系统级保留路径节点名称
    String ROOT_TAG_NAME = "/";
    String TRASH_TAG_NAME = ".Trash";
    String SHARED_TAG_NAME = ".Shared";
    String PAGE_TAG_NAME = ".Page";
    String NOTE_TAG_NAME = ".Note";

    List<String> PERSONAL_SYSTEM_PATH_TAG_NAMES = List.of(
            ROOT_TAG_NAME,
            TRASH_TAG_NAME,
            SHARED_TAG_NAME,
            PAGE_TAG_NAME,
            NOTE_TAG_NAME
    );

    String RESOURCE_TRASH_COLLECTION = "wisepen_resource_trash";
    String CONFIG_TRASH_COLLECTION = "wisepen_group_res_config_trash";
    String TAGS_TRASH_COLLECTION = "wisepen_tags_trash";
}
