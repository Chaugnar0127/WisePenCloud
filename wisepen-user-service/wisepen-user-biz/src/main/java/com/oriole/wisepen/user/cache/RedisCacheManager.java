package com.oriole.wisepen.user.cache;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisCacheManager {

	private final RedisTemplate<String, Object> redisTemplate;
	private final StringRedisTemplate stringRedisTemplate;

	private static final String REDIS_SESSION_PREFIX = "wisepen:user:auth:session:";
	private static final String REDIS_SESSION_TO_USER_PREFIX = "wisepen:user:auth:user2session:";
	private static final String REDIS_GROUP_CHAT_BLOCK_PREFIX = "wisepen:chat:block:group:";
	private static final String REDIS_GROUP_MEMBER_CHAT_BLOCK_PREFIX = "wisepen:chat:block:member:";
	private static final long SESSION_TIMEOUT_DAYS = 7;


	public String setSession(String userId, IdentityType identityType, Map<String, Integer> groupRoleMap) {
		// 构建 Session 上下文数据
		Map<String, Object> sessionData = new HashMap<>();
		sessionData.put("userId", userId);
		sessionData.put("identityType", identityType.getCode());
		sessionData.put("groupRoleMap", groupRoleMap);

		String sessionId = stringRedisTemplate.opsForValue().get(REDIS_SESSION_TO_USER_PREFIX + userId);
		if (StrUtil.isBlank(sessionId)) {
			sessionId = IdUtil.fastSimpleUUID();
		}
		// sessionId不存在则新增；sessionId存在则刷新一下时间
		redisTemplate.opsForValue().set(REDIS_SESSION_PREFIX + sessionId, sessionData,
				SESSION_TIMEOUT_DAYS, TimeUnit.DAYS); // 存储Session
		stringRedisTemplate.opsForValue().set(REDIS_SESSION_TO_USER_PREFIX + userId, sessionId,
				SESSION_TIMEOUT_DAYS, TimeUnit.DAYS); // 存储sessionId(建立与用户名的关联以便检索)
		return sessionId;
	}

	public void deleteSession(String sessionId, String userId) {
		stringRedisTemplate.delete(REDIS_SESSION_PREFIX + sessionId);
		stringRedisTemplate.delete(REDIS_SESSION_TO_USER_PREFIX + userId);
	}

	public void updateGroupRoleMapInSession(String userId, String groupId, GroupRoleType groupRoleType) {
		String sessionId = stringRedisTemplate.opsForValue().get(REDIS_SESSION_TO_USER_PREFIX + userId);
		if (StrUtil.isBlank(sessionId)) return; // 用户未登录则直接返回

		@SuppressWarnings("unchecked")
		Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(REDIS_SESSION_PREFIX + sessionId);
		if (sessionData == null) return;

		@SuppressWarnings("unchecked")
		Map<String, Integer> groupRoleMap = (Map<String, Integer>) sessionData.get("groupRoleMap");
		if (groupRoleMap == null) groupRoleMap = new HashMap<>();

		if (groupRoleType.equals(GroupRoleType.NOT_MEMBER)) {
			groupRoleMap.remove(groupId);
		} else {
			groupRoleMap.put(groupId, groupRoleType.getCode());
		}
		sessionData.put("groupRoleMap", groupRoleMap);

		redisTemplate.opsForValue().set(REDIS_SESSION_PREFIX + sessionId, sessionData,
				SESSION_TIMEOUT_DAYS, TimeUnit.DAYS);
	}

	// 封印/解封 群组Chat
	public void blockGroupChat(String groupId) {
		stringRedisTemplate.opsForValue().set(REDIS_GROUP_CHAT_BLOCK_PREFIX + groupId, "1");
	}
	public void unblockGroupChat(String groupId) {
		stringRedisTemplate.delete(REDIS_GROUP_CHAT_BLOCK_PREFIX + groupId);
	}

	// 封印/解封 组成员Chat
	public void blockGroupMemberChat(String groupId, String userId) {
		stringRedisTemplate.opsForValue().set(REDIS_GROUP_MEMBER_CHAT_BLOCK_PREFIX + groupId + ":" + userId, "1");
	}
	public void unblockGroupMemberChat(String groupId, String userId) {
		stringRedisTemplate.delete(REDIS_GROUP_MEMBER_CHAT_BLOCK_PREFIX + groupId + ":" + userId);
	}
}
