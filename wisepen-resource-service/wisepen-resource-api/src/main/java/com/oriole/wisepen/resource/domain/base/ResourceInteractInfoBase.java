package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 资源互动信息基类，承载阅读量等可持续扩展的互动统计字段。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceInteractInfoBase {
    private Long readCount;   // 资源有效阅读量，默认 0
}
