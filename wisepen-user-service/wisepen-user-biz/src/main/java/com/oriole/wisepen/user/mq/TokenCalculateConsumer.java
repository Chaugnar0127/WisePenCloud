package com.oriole.wisepen.user.mq;


import com.oriole.wisepen.user.domain.entity.TokenCalculateMessage;
import com.oriole.wisepen.user.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.parser.Token;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oriole.wisepen.user.api.constant.MqTopicConstants.TOPIC_TOKEN_CALC;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCalculateConsumer {
//	private final GroupMapper groupMapper;
	private final GroupMemberService groupMemberService;
//	private final UserWalletsMapper userWalletsMapper;

	@KafkaListener(topics = TOPIC_TOKEN_CALC, groupId = "wisepen-resource-token-calc-group")
	public void onTokenCalculate(TokenCalculateMessage message) {
		try {
			log.debug("接收到 Token 使用事件, ResourceId: {}", message.getTraceId());
			// 执行真正的溯源和权限计算
			groupMemberService.calculateToken(message);
		} catch (Exception e) {
			// 这里建议捕获异常，防止某一个资源的脏数据导致消费者无限重试和阻塞
			// 在成熟的架构中，可以将失败的 message 发送到 Dead Letter Queue (死信队列)
			log.error("Token 计算失败, ResourceId: {}", message.getTraceId(), e);
		}
	}
}
