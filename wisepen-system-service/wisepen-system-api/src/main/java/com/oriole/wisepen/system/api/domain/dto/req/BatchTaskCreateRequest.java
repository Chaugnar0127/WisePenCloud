package com.oriole.wisepen.system.api.domain.dto.req;

import com.oriole.wisepen.system.api.enums.BatchTaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class BatchTaskCreateRequest {

    @NotNull
    private BatchTaskType type;

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotEmpty
    private Map<String, Object> meta;
}
