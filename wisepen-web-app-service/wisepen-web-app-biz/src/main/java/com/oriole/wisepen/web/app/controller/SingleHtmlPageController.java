package com.oriole.wisepen.web.app.controller;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.common.security.annotation.CheckRole;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionReqDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.domain.dto.ResourceInfoGetReqDTO;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import com.oriole.wisepen.resource.enums.ResourceAction;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import com.oriole.wisepen.web.app.domain.dto.res.SingleHtmlPageInfoResponse;
import com.oriole.wisepen.web.app.domain.dto.res.SingleHtmlPageVersionInfoResponse;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageEntity;
import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageVersionEntity;
import com.oriole.wisepen.web.app.exception.WebAppError;
import com.oriole.wisepen.web.app.service.ISingleHtmlPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Single HTML Page", description = "Single HTML Page 信息、版本和内容访问")
@RestController
@RequestMapping("/singleHtmlPage")
@RequiredArgsConstructor
@Validated
@CheckRole
public class SingleHtmlPageController {

    private static final String CONTENT_SECURITY_POLICY = "sandbox allow-scripts; default-src 'none'; "
            + "script-src 'unsafe-inline'; style-src 'unsafe-inline'; img-src data: blob:; "
            + "connect-src 'none'; form-action 'none'; base-uri 'none'; frame-ancestors 'self'";

    private final ISingleHtmlPageService singleHtmlPageService;
    private final RemoteResourceService remoteResourceService;

    @Operation(
            summary = "获取 Single HTML Page 信息",
            description = """
                    - 用途：获取页面资源详情、当前版本和指定版本摘要。
                    - 请求：resourceId 指定页面资源；targetVersion 可选，未传时读取当前版本。
                    - 约束：当前用户必须已登录并拥有资源 VIEW 动作；指定版本必须存在。
                    - 处理：通过资源服务校验目标版本访问权限，组合资源详情、页面当前版本及目标版本摘要；不返回 HTML 正文。
                    - 失败：资源不存在 -> ResourceError.RESOURCE_NOT_FOUND；没有 VIEW 动作 -> ResourceError.RESOURCE_PERMISSION_DENIED；页面不存在 -> WebAppError.SINGLE_HTML_PAGE_NOT_FOUND；版本不存在 -> WebAppError.SINGLE_HTML_PAGE_VERSION_NOT_FOUND。
                    - 响应：返回资源详情、当前版本号和目标版本摘要。
                    """
    )
    @PostMapping("/getSingleHtmlPageInfo")
    public R<SingleHtmlPageInfoResponse> getSingleHtmlPageInfo(
            @RequestParam String resourceId,
            @RequestParam(value = "targetVersion", required = false) Integer targetVersion) {
        ResourceItemResponse resourceInfo = remoteResourceService.getResourceInfo(new ResourceInfoGetReqDTO(
                resourceId, SecurityContextHolder.getUserId(), SecurityContextHolder.getGroupRoleMap(), targetVersion
        )).getData();
        if (resourceInfo == null || resourceInfo.getResourceType() != ResourceType.SINGLE_HTML_PAGE) {
            throw new ServiceException(WebAppError.SINGLE_HTML_PAGE_NOT_FOUND);
        }
        SingleHtmlPageEntity page = singleHtmlPageService.getSingleHtmlPageInfo(resourceId);
        SingleHtmlPageVersionEntity version = singleHtmlPageService.getSingleHtmlPageVersion(resourceId, targetVersion);
        return R.ok(SingleHtmlPageInfoResponse.builder()
                .resourceInfo(resourceInfo)
                .currentVersion(page.getCurrentVersion())
                .versionInfo(BeanUtil.copyProperties(version, SingleHtmlPageVersionInfoResponse.class))
                .build());
    }

    @Operation(
            summary = "获取 Single HTML Page 内容",
            description = """
                    - 用途：读取页面当前版本或指定历史版本的完整 HTML，供受控页面容器加载。
                    - 请求：resourceId 指定页面资源；targetVersion 可选，未传时读取当前版本。
                    - 约束：当前用户必须已登录并拥有目标版本 VIEW 动作；指定版本必须存在。
                    - 处理：通过资源服务校验 VIEW 权限，从对象存储读取并校验内容摘要后输出 HTML 正文，同时附加 sandbox CSP、禁止缓存和禁止 MIME 嗅探等安全响应头；不修改页面或版本。
                    - 失败：没有 VIEW 动作 -> WebAppError.SINGLE_HTML_PAGE_PERMISSION_DENIED；页面不存在 -> WebAppError.SINGLE_HTML_PAGE_NOT_FOUND；版本不存在 -> WebAppError.SINGLE_HTML_PAGE_VERSION_NOT_FOUND；对象存储内容读取或摘要校验失败 -> WebAppError.SINGLE_HTML_PAGE_CONTENT_READ_FAILED。
                    - 响应：返回 text/html;charset=UTF-8 的页面正文。
                    """
    )
    @GetMapping(value = "/getSingleHtmlPageContent", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSingleHtmlPageContent(
            @RequestParam String resourceId,
            @RequestParam(value = "targetVersion", required = false) Integer targetVersion) {
        assertReadable(resourceId, targetVersion);
        String htmlContent = singleHtmlPageService.getSingleHtmlPageContent(resourceId, targetVersion);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header("Content-Security-Policy", CONTENT_SECURITY_POLICY)
                .header("X-Content-Type-Options", "nosniff")
                .header("Referrer-Policy", "no-referrer")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(htmlContent);
    }

    @Operation(
            summary = "分页查询 Single HTML Page 版本",
            description = """
                    - 用途：查询页面的完整历史版本轨迹。
                    - 请求：resourceId 指定页面资源；page 和 size 控制分页。
                    - 约束：当前用户必须已登录并拥有资源 VIEW 动作；page 从 1 开始，size 最大为 100。
                    - 处理：通过资源服务校验 VIEW 权限后按版本号倒序分页读取版本摘要；不返回 HTML 正文。
                    - 失败：没有 VIEW 动作 -> WebAppError.SINGLE_HTML_PAGE_PERMISSION_DENIED；页面不存在 -> WebAppError.SINGLE_HTML_PAGE_NOT_FOUND。
                    - 响应：返回版本摘要分页列表和总数。
                    """
    )
    @GetMapping("/listSingleHtmlPageVersions")
    public R<PageR<SingleHtmlPageVersionInfoResponse>> listSingleHtmlPageVersions(
            @RequestParam String resourceId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        assertReadable(resourceId, null);
        return R.ok(singleHtmlPageService.listVersions(resourceId, page, size));
    }

    private void assertReadable(String resourceId, Integer targetVersion) {
        ResourceCheckPermissionResDTO permission = remoteResourceService.checkResPermission(
                ResourceCheckPermissionReqDTO.builder()
                        .resourceId(resourceId)
                        .userId(SecurityContextHolder.getUserId())
                        .groupRoles(SecurityContextHolder.getGroupRoleMap())
                        .targetVersion(targetVersion)
                        .build()
        ).getData();
        if (permission == null || permission.getAllowedActions() == null
                || !permission.getAllowedActions().contains(ResourceAction.VIEW)) {
            throw new ServiceException(WebAppError.SINGLE_HTML_PAGE_PERMISSION_DENIED);
        }
    }
}
