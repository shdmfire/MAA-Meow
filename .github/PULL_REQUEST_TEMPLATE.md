# Pull Request

提交前请阅读 [PR 规范](../docs/PULL_REQUEST_GUIDELINES.md) / [PR Guidelines](../docs/PULL_REQUEST_GUIDELINES_EN.md)。

## 关联 Issue

<!-- Closes #123 / Fixes #123 / Related #123；没有 Issue 时请说明需求来源。 -->

## 变更摘要

<!-- 用 2～5 条 bullet 说明改了什么。 -->

## 修改文件清单

<!-- 按 module / source set 列出关键修改文件；生成文件可合并说明。 -->

## Module 依赖变化

<!-- 说明新增、删除或调整的 project dependency；无变化请写“无”。 -->

## hostOnly 影响

<!-- 说明是否影响无 MAA Core 的 hostOnly 构建、运行或测试；无影响请写“无”。 -->

## 测试证据

<!-- 请粘贴 ./gradlew modularizationFastCheck 等命令结果，并写清设备、系统版本、权限方案或操作路径。不要只写“已测试”。 -->

## 截图 / 日志 / 说明

<!-- 涉及 UI、权限、后台服务、任务执行或 Bug 修复时，请提供截图、Logcat、Actions 链接或复现步骤。 -->

## Checklist

- [ ] 我已阅读并遵守 [PR 规范](../docs/PULL_REQUEST_GUIDELINES.md) / [PR Guidelines](../docs/PULL_REQUEST_GUIDELINES_EN.md)
- [ ] 我已列出修改文件及 module 依赖变化
- [ ] 我已说明 hostOnly 影响
- [ ] 我已提供可复现的测试证据
