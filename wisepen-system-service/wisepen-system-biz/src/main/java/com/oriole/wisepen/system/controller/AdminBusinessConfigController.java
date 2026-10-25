package com.oriole.wisepen.system.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckRole;
import com.oriole.wisepen.system.api.domain.dto.req.BusinessConfigApplyRequest;
import com.oriole.wisepen.system.api.domain.dto.res.BusinessConfigApplyResponse;
import com.oriole.wisepen.system.api.domain.dto.res.BusinessConfigResponse;
import com.oriole.wisepen.system.api.domain.dto.res.BusinessConfigSummaryResponse;
import com.oriole.wisepen.system.service.BusinessConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员 - 业务配置", description = "管理员查询并应用各微服务的 Nacos 业务配置")
@RestController
@RequestMapping("/system/admin/businessConfig")
@RequiredArgsConstructor
@CheckRole(IdentityType.ADMIN)
@Validated
public class AdminBusinessConfigController {

    private final BusinessConfigService businessConfigService;

    @Operation(
            summary = "查询业务配置目录",
            description = """
                    - 用途：查询当前 System 版本声明的全部可管理业务配置。
                    - 请求：无。
                    - 约束：当前身份必须是平台管理员。
                    - 处理：读取版本化静态目录并返回配置摘要；不读取 Nacos 配置内容。
                    - 失败：无业务失败项。
                    - 响应：返回配置编码、名称、描述和所属微服务列表。
                    """
    )
    @GetMapping("/listConfigs")
    public R<List<BusinessConfigSummaryResponse>> listConfigs() {
        return R.ok(businessConfigService.listConfigs());
    }

    @Operation(
            summary = "查询业务配置",
            description = """
                    - 用途：读取一个受管 Nacos YAML 节点及其 JSON Schema，供管理端生成表单和编辑。
                    - 请求：code 是静态目录中的业务配置编码。
                    - 约束：当前身份必须是平台管理员；配置编码、Nacos Data ID 和目标节点必须存在。
                    - 处理：读取完整 Nacos YAML，提取目录指定节点，并返回完整文档 MD5 revision；不修改配置。
                    - 失败：配置编码、Data ID 或目标节点不存在 -> SysError.BUSINESS_CONFIG_NOT_FOUND；Nacos YAML 无法解析 -> SysError.BUSINESS_CONFIG_CONTENT_INVALID；Nacos 查询失败 -> SysError.BUSINESS_CONFIG_NACOS_FAILED。
                    - 响应：返回配置元数据、Schema、当前 JSON 值和完整 Nacos 文档 revision。
                    """
    )
    @GetMapping("/getConfig")
    public R<BusinessConfigResponse> getConfig(@RequestParam @NotBlank String code) {
        return R.ok(businessConfigService.getConfig(code));
    }

    @Operation(
            summary = "应用业务配置",
            description = """
                    - 用途：将管理端编辑完成的 JSON 节点应用到对应微服务的 Nacos YAML。
                    - 请求：code 是业务配置编码；revision 是查询时取得的完整文档 MD5；value 是符合目录 JSON Schema 的新节点值。
                    - 约束：当前身份必须是平台管理员；value 必须通过 Schema 校验；revision 必须与 Nacos 当前完整文档一致；目标节点必须已存在。
                    - 处理：重新读取完整 YAML，替换指定节点并规范化整份 YAML，通过 Nacos CAS 发布；只保证发布到 Nacos 成功，不确认业务实例已刷新。
                    - 失败：配置编码、Data ID 或目标节点不存在 -> SysError.BUSINESS_CONFIG_NOT_FOUND；Schema 或 YAML 无效 -> SysError.BUSINESS_CONFIG_CONTENT_INVALID；文档版本变化或 CAS 失败 -> SysError.BUSINESS_CONFIG_REVISION_CONFLICT；Nacos 查询或发布失败 -> SysError.BUSINESS_CONFIG_NACOS_FAILED。
                    - 响应：返回发布成功标记和规范化后完整文档的新 revision。
                    """
    )
    @Log(title = "应用业务配置", businessType = BusinessType.UPDATE,
            isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/applyConfig")
    public R<BusinessConfigApplyResponse> applyConfig(
            @Valid @RequestBody BusinessConfigApplyRequest request) {
        return R.ok(businessConfigService.applyConfig(request));
    }
}
