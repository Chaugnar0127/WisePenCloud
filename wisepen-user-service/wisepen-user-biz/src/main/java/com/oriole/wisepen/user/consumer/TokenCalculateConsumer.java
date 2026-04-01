package com.oriole.wisepen.user.consumer;


import com.oriole.wisepen.user.domain.entity.TokenCalculateMessage;
import com.oriole.wisepen.user.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.user.api.constant.MqTopicConstants.TOPIC_TOKEN_CALC;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCalculateConsumer {
	private final GroupMemberService groupMemberService;

	@KafkaListener(topics = TOPIC_TOKEN_CALC, groupId = "wisepen-user-token-calc-group")
	public void onTokenCalculate(TokenCalculateMessage message) {
		log.debug("接收到 Token 使用事件, ResourceId: {}", message.getTraceId());
		groupMemberService.calculateToken(message);
	}
}
