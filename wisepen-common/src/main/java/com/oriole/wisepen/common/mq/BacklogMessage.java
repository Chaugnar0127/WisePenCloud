package com.oriole.wisepen.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacklogMessage {

    private String topic;
    private String kafkaKey;
    private String payloadType;
    private String payloadJson;
}
