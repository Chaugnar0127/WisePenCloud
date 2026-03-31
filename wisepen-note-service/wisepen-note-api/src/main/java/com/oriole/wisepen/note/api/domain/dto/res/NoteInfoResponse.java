package com.oriole.wisepen.note.api.domain.dto.res;

import com.oriole.wisepen.note.api.domain.base.NoteInfoBase;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NoteInfoResponse {
    ResourceItemResponse resourceInfo;
    NoteInfoBase noteInfo;
}
