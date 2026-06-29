package com.oriole.wisepen.document.api.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.onlyoffice.model.documenteditor.Config;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlyOfficeEditorConfigResponse implements Serializable {
    private String sessionId;
    private Config config;
}
