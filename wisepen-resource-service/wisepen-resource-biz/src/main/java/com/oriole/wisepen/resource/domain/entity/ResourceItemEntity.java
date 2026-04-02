package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.domain.ComputedGroupAcl;
import com.oriole.wisepen.resource.domain.GroupTagBind;
import com.oriole.wisepen.resource.domain.base.ResourceItemInfoBase;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "wisepen_resource_items")
public class ResourceItemEntity extends ResourceItemInfoBase {
    @Id
    private String resourceId; // 资源全局唯一ID

    private List<GroupTagBind> groupBinds = new ArrayList<>();

    // 预计算后的运行时权限
    private Map<String, ComputedGroupAcl> computedGroupAcls;

    /** 资源级强覆盖：若非空，将无视 computedGroupAcls 的值 */
    private Integer overrideGrantedActionsMask;

    /** 资源级绝对用户特权：若用户命中此 Map，直接返回该值，无视其他所有规则 */
    private Map<String, Integer> specifiedUsersGrantedActionsMask;

    private Date createTime;
    private Date updateTime;
}