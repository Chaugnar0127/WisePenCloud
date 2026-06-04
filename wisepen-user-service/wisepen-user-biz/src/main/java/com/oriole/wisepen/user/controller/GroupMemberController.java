package com.oriole.wisepen.user.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.user.api.domain.dto.req.GroupMemberKickRequest;
import com.oriole.wisepen.user.api.domain.dto.req.GroupMemberRoleUpdateRequest;
import com.oriole.wisepen.user.api.domain.dto.req.GroupMemberQuitRequest;
import com.oriole.wisepen.user.api.domain.dto.req.GroupMemberTokenLimitUpdateRequest;
import com.oriole.wisepen.user.api.domain.dto.res.GroupMemberDetailResponse;
import com.oriole.wisepen.user.api.domain.dto.res.GroupMemberTokenDetailResponse;
import com.oriole.wisepen.user.service.IGroupMemberService;
import com.oriole.wisepen.user.service.IWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小组成员管理", description = "小组成员退出、移除、角色、配额与列表查询")
@RestController
@RequestMapping("/group/member")
@RequiredArgsConstructor
@Validated
@CheckLogin
public class GroupMemberController {

	private final IGroupMemberService groupMemberService;
	private final IWalletService walletService;

	@Operation(summary = "退出小组")
	@PostMapping("/quit")
	public R<Void> quitGroup(@RequestBody @Valid GroupMemberQuitRequest req) {
		SecurityContextHolder.assertInGroup(req.getGroupId()); // 用户退群必须先在群中
		groupMemberService.quitGroup(req, SecurityContextHolder.getUserId(), SecurityContextHolder.getGroupRole(req.getGroupId()));
		return R.ok();
	}

	@Operation(summary = "移除小组成员")
	@PostMapping("/kick")
	public R<Void> kickGroupMembers(@RequestBody @Valid GroupMemberKickRequest req) {
		SecurityContextHolder.assertGroupRole(req.getGroupId(), GroupRoleType.OWNER, GroupRoleType.ADMIN);
		groupMemberService.kickGroupMembers(req, SecurityContextHolder.getUserId(), SecurityContextHolder.getGroupRole(req.getGroupId()));
		return R.ok();
	}

	@Operation(summary = "修改成员角色")
	@PostMapping("/changeRole")
	public R<Void> changeRole(@RequestBody @Valid GroupMemberRoleUpdateRequest req) {
		SecurityContextHolder.assertGroupRole(req.getGroupId(), GroupRoleType.OWNER); //必须是所有者才能修改成员权限
		groupMemberService.updateGroupMemberRole(req, SecurityContextHolder.getUserId());
		return R.ok();
	}

	@Operation(summary = "分页查询小组成员")
	@GetMapping("/list")
	public R<PageR<GroupMemberDetailResponse>> listGroupMembers(
			@RequestParam("groupId") Long groupId,
			@RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
			@RequestParam(value = "size", defaultValue = "20") @Min(1) int size
	) {
		SecurityContextHolder.assertInGroup(groupId);
		return R.ok(groupMemberService.getGroupMemberList(groupId, page, size));
	}

	@Operation(summary = "获取我的小组成员信息")
	@GetMapping("/getMyGroupMemberInfo")
	public R<GroupMemberDetailResponse> getMyGroupMemberInfo(@RequestParam("groupId") Long groupId) {
		return R.ok(groupMemberService.getGroupMemberInfoByUserId(groupId, SecurityContextHolder.getUserId()));
	}

	@Operation(summary = "获取我的小组角色")
	@GetMapping("/getMyRole")
	public R<GroupRoleType> getMyRole(@RequestParam("groupId") Long groupId) {
		return R.ok(SecurityContextHolder.getGroupRole(groupId));
	}

	@Operation(summary = "修改成员信息点额度")
	@PostMapping("/changeTokenLimit")
	public R<Void> changeTokenLimit(@RequestBody @Valid GroupMemberTokenLimitUpdateRequest req) {
		SecurityContextHolder.assertGroupRole(req.getGroupId(), GroupRoleType.OWNER);
		walletService.updateGroupMemberTokenLimit(req);
		return R.ok();
	}

	@Operation(summary = "查询我的小组信息点额度")
	@GetMapping("/getAllMyGroupTokenInfo")
	public R<PageR<GroupMemberTokenDetailResponse>> getAllMyGroupTokenInfo(
			@RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
			@RequestParam(value = "size", defaultValue = "20") @Min(1) Integer size
	){
		Long userId = SecurityContextHolder.getUserId();
		return R.ok(walletService.getAllGroupTokenInfoByUserId(userId, page, size));
	}
}
