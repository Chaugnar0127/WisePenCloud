package com.oriole.wisepen.document.api.domain.dto.res;

import com.oriole.wisepen.document.api.domain.base.DocumentInfoBase;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfoResponse {
    ResourceItemResponse resourceInfo;
    DocumentInfoBase documentInfo;
}
