package com.oriole.wisepen.system.service.executor.resource.fork;

import com.oriole.wisepen.resource.enums.ResourceType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ResourceForkTaskMeta {
    private String sourceResourceId;
    private ResourceType sourceResourceType;
    private Integer sourceVersion;
    private String targetResourceName;
    private List<Long> targetUserIds;
    private List<String> targetFolderPath;
    private Boolean createFolderIfAbsent;
}
