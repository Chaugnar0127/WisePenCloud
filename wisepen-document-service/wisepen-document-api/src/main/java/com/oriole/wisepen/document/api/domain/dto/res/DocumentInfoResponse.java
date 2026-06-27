package com.oriole.wisepen.document.api.domain.dto.res;

import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfoResponse {
    private ResourceItemResponse resourceInfo;
    private DocumentVersionInfoResponse documentVersionInfo;
    private List<Long> authors;
}
