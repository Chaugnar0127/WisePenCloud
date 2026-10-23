package com.oriole.wisepen.resource.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PersonalFolderEnsureReqDTO {

    @NotBlank
    private String targetUserId;

    @NotEmpty
    private List<@NotBlank String> folderPath;

    private boolean createIfAbsent;
}
