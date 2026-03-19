package com.oriole.wisepen.resource.service.impl;

import com.oriole.wisepen.resource.domain.dto.req.GroupResConfigUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.res.GroupResConfigResponse;
import com.oriole.wisepen.resource.domain.entity.GroupResConfigEntity;
import com.oriole.wisepen.resource.enums.FileOrganizationLogic;
import com.oriole.wisepen.resource.repository.GroupResConfigRepository;
import com.oriole.wisepen.resource.service.IGroupResService;
import com.oriole.wisepen.resource.service.ITagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupResServiceImpl implements IGroupResService {

    public static final String CONFIG_TRASH_COLLECTION = "wisepen_group_res_config_trash";

    private final GroupResConfigRepository configRepository;
    private final MongoTemplate mongoTemplate;
    private final ITagService tagService;

    @Override
    public GroupResConfigResponse getConfig(String groupId) {
        FileOrganizationLogic logic = configRepository.findByGroupId(groupId)
                .map(GroupResConfigEntity::getFileOrgLogic)
                .orElse(FileOrganizationLogic.FOLDER);
        return new GroupResConfigResponse(groupId, logic);
    }

    @Override
    public void upsertConfig(GroupResConfigUpdateRequest req) {
        GroupResConfigEntity entity = configRepository.findByGroupId(req.getGroupId())
                .orElseGet(() -> {
                    GroupResConfigEntity newEntity = new GroupResConfigEntity();
                    newEntity.setGroupId(req.getGroupId());
                    return newEntity;
                });
        entity.setFileOrgLogic(req.getFileOrgLogic());
        entity.setUpdateTime(new Date());
        configRepository.save(entity);
    }

    @Override
    public FileOrganizationLogic getFileOrgLogic(String groupId) {
        return configRepository.findByGroupId(groupId)
                .map(GroupResConfigEntity::getFileOrgLogic)
                .orElse(FileOrganizationLogic.FOLDER);
    }

    @Override
    public void softDissolveGroup(String groupId) {
        // Tag 树软删除（resource关联会字段解决）
        tagService.softRemoveAllTagByGroupId(groupId);

        // 配置软删除 将 dissolvedAt 记录后移入 trash（兜底确保 dissolvedAt 存在）
        GroupResConfigEntity config = configRepository.findByGroupId(groupId)
                .orElseGet(() -> {
                    GroupResConfigEntity newEntity = new GroupResConfigEntity();
                    newEntity.setGroupId(groupId);
                    return newEntity;
                });
        config.setDissolvedAt(new Date());
        config.setUpdateTime(new Date());
        mongoTemplate.save(config, CONFIG_TRASH_COLLECTION);
        configRepository.deleteByGroupId(groupId);
        log.info("小组 {} 解散：资源配置已移入 trash，供定时任务 30 天后清理", groupId);
    }

    @Override
    public void hardDissolveGroup(String groupId) {
        // Tag 树硬删除 （同步删除Tag绑定的资源）
        tagService.hardRemoveAllTagByGroupId(groupId);

        // 清理 wisepen_group_res_config_trash 中该组的记录
        mongoTemplate.remove(
                Query.query(Criteria.where("groupId").is(groupId)),
                CONFIG_TRASH_COLLECTION
        );

        log.info("小组 {} 硬删除完成", groupId);
    }
}
