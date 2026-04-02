package com.oriole.wisepen.resource.domain.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TagInfoBase extends TagSpaceBase{
    private String tagName;
    private String tagDesc;
    private String tagIcon;
    private String tagColor;
}