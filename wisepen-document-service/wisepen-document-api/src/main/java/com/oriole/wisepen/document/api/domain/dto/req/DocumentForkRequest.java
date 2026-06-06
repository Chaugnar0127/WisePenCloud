package com.oriole.wisepen.document.api.domain.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentForkRequest {
    @NotBlank
    private String sourceResourceId;

    @NotBlank
    private String targetResourceId;

    @NotNull
    @Min(0)
    @Max(0)
    private Long version;

    @NotNull
    private Long buyerId;
}
