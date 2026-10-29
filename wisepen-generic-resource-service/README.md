# 通用资源服务

本服务负责 `ARCHIVE`、`BINARY`、`UNKNOWN` 文件的上传、状态补偿、原文件下载和删除事件处理，不提供预览。资源主档、挂载和权限使用 Resource API，文件存储使用 Storage API。

## 同步 upstream/main 的适配说明

本次同步基于 upstream 提交 `ba6de69f`，重点对齐 Web App 资源类型与 Nacos 业务配置目录。

### 资源类型与存储场景

- 保留上游 `SINGLE_HTML_PAGE`（code 为 `41`），通用资源的 `ARCHIVE`、`BINARY` 调整为 `51`、`52`。
- 保留上游 `PRIVATE_WEB_APP_ASSET`（code 为 `8`），`PRIVATE_GENERIC_RESOURCE` 调整为 `9`。
- JSON 与 MyBatis 枚举值仍使用原有字符串；MongoDB 枚举名称和通用资源对象路径 `private/generic-resources` 不变。
- 使用旧分支数字编号的调用方需要同步更新，尤其不能再以存储场景 `8` 表示通用资源；推荐传递 `PRIVATE_GENERIC_RESOURCE` 字符串。
- 资源类型解析同时支持类型字符串、扩展名别名和前导点，例如 `SINGLE_HTML_PAGE`、`.HTML`、`.ZIP`、`bin`。HTML 属于 Web App，通用资源上传入口会拒绝。

### Nacos 配置迁移

通用资源配置位于 `DEFAULT_GROUP` 的 `wisepen-generic-resource-service-{profile}.yaml`。若已有自定义配置，将 `wisepen.generic-resource` 下的旧键迁移为以下新键；旧键不再读取。

| 旧键 | 新键 | 默认值 |
| --- | --- | --- |
| `base-timeout-ms` | `upload-timeout.base-timeout-ms` | `600000` |
| `assumed-speed-bps` | `upload-timeout.assumed-speed-bps` | `102400` |
| `max-timeout-ms` | `upload-timeout.max-timeout-ms` | `3600000` |
| `stale-check-delay-ms` | `stale-check.delay-ms` | `300000` |

```yaml
wisepen:
  generic-resource:
    upload-timeout:
      base-timeout-ms: 600000
      assumed-speed-bps: 102400
      max-timeout-ms: 3600000
    stale-check:
      delay-ms: 300000
```

System 的 `config-catalog/wisepen-generic-resource-service/config-catalog.json` 已注册上传超时策略与检查调度。超时策略按现有 `ConfigurationProperties` 机制更新；调度间隔由 `@Scheduled` 启动时解析，修改后需要重启通用资源服务。

配置结构参考 `DocumentProperties`、`DocumentGcTask` 和文档服务配置目录；只合并既有边界与配置模式，没有新增远程降级或兼容分支。存储服务将 `configId` 改为 `providerCode` 后，本服务仍通过默认存储提供商上传，无需新增配置或改动 Feign 调用。

## 验证

```shell
mvn -pl wisepen-generic-resource-service/wisepen-generic-resource-biz -am test
```

回归测试覆盖资源类型与存储场景的序列化、删除消息的枚举 Map key、编号唯一性、HTML 上传拒绝，以及新配置路径对上传超时判断的影响。涉及共享 API 的合并还应执行根目录 `mvn verify`。
