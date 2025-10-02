# SmsForwarder 项目 Wiki 文档

## 项目概述

SmsForwarder（短信转发器）是一款 Android 应用程序，可以监控手机的短信、来电和应用通知，并根据用户定义的规则将这些信息转发到其他平台，如钉钉、企业微信、飞书、邮箱、Telegram 等。

## 主要功能

1. **短信转发**：监听并转发接收到的短信
2. **来电转发**：监听并转发来电信息
3. **通知转发**：监听并转发应用通知
4. **多种转发渠道**：
   - 钉钉群机器人
   - 企业微信群机器人
   - 飞书群机器人
   - 邮箱
   - Telegram 机器人
   - Webhook
   - Server 酱
   - PushPlus
   - 手机短信等
5. **远程控制**：支持主动控制服务端与客户端，实现远程发短信、查短信、查通话等功能
6. **自动任务**：支持快捷指令，实现自动化操作

## 技术架构

### 核心组件

- **Kotlin**：主要开发语言
- **Android Room**：本地数据库存储
- **AndServer**：内置 HTTP 服务器
- **WorkManager**：后台任务处理
- **XXPermissions**：权限管理
- **XUI**：UI 组件库

### 项目结构

```
app/src/main/
├── java/                    # Java/Kotlin 源代码
│   └── com/idormy/sms/forwarder/
│       ├── activity/        # Activity 组件
│       ├── adapter/         # 适配器
│       ├── core/            # 核心组件
│       ├── database/        # 数据库相关
│       ├── entity/          # 数据模型
│       ├── fragment/        # Fragment 组件
│       ├── receiver/        # 广播接收器
│       ├── server/          # 服务器相关
│       ├── service/         # 服务组件
│       ├── utils/           # 工具类
│       ├── widget/          # 自定义控件
│       ├── workers/         # 后台工作器
│       └── App.kt           # 应用入口
├── res/                     # 资源文件
└── assets/                  # 静态资源
```

## 编译与打包

### 环境要求

- Windows 11
- Android Studio 或 IntelliJ IDEA
- JDK 8 或更高版本
- Android SDK

### 构建步骤

1. 克隆项目代码
2. 使用 Android Studio 打开项目
3. 同步 Gradle 依赖
4. 构建 APK

### 命令行构建

```bash
# Windows 环境下执行
gradlew.bat assembleRelease
```

生成的 APK 文件位于：`app/build/outputs/apk/release/`

## 配置说明

### 签名配置

在 `keystore/keystore.properties` 文件中配置签名信息：

```properties
keyAlias=your_key_alias
keyPassword=your_key_password
storeFile=your_keystore_file_path
storePassword=your_store_password
```

### 多渠道打包

支持按 CPU 架构分别打包：
- armeabi-v7a
- arm64-v8a
- x86
- x86_64

## 使用指南

### 基本设置

1. 安装应用后，授予必要的权限（短信、电话、通知等）
2. 在"转发规则"中添加转发规则
3. 配置转发目标（如钉钉机器人、邮箱等）
4. 启用相应的监听功能

### 转发规则配置

转发规则包含以下要素：
- **触发条件**：什么事件触发转发（短信、来电、通知）
- **过滤条件**：根据关键词、号码等过滤
- **转发目标**：转发到哪个平台
- **消息模板**：自定义转发内容格式

### 远程控制

通过内置的 HTTP 服务器，可以实现远程控制功能：
- 发送短信
- 查询短信
- 查询通话记录
- 查询联系人
- 查询电池信息

## 常见问题

1. **无法接收短信**：检查是否授予了短信权限，是否设置了默认短信应用
2. **转发失败**：检查网络连接，验证转发目标配置是否正确
3. **后台运行**：确保应用的后台运行权限已开启，添加到电池优化白名单

## 开发指南

### 添加新的转发渠道

1. 在 `entity/sender` 包中创建新的发送者实体类
2. 在 `fragment/senders` 包中创建对应的配置界面
3. 实现具体的发送逻辑

### 扩展监听功能

1. 创建新的 `BroadcastReceiver` 监听特定事件
2. 在 `AndroidManifest.xml` 中注册广播接收器
3. 在设置界面添加对应的开关选项

## 贡献代码

欢迎提交 Issue 和 Pull Request 来改进 SmsForwarder。

## 许可证

本项目采用 BSD 许可证，详情请参见 [LICENSE](LICENSE) 文件。

## 功能设计与执行计划：飞书“未读加急电话”

### 目标

- 当通过“飞书企业应用”发送的消息在一段时间内未读时，自动按指数退避最多 N 次（N≤5，可配置）调用飞书“加急短信/电话”接口，直到目标用户已读或达到上限即停止。

### 适用范围与判定口径

- 仅支持 `receive_id_type = user_id`。
- “已读”判定：`read_users` 返回的用户列表中包含该 `user_id`。

### 参考接口（飞书开放平台）

- 创建消息（返回 `message_id`）：[创建消息](https://open.feishu.cn/document/server-docs/im-v1/message/create?appId=cli_a8507cedc239100d)
- 查询消息已读用户：[查询消息阅读用户](https://open.feishu.cn/document/server-docs/im-v1/message/read_users)
- 加急短信/电话：[urgent_sms](https://open.feishu.cn/document/server-docs/im-v1/buzz-messages/urgent_sms)

### 设置与默认值

- 新增设置字段（`FeishuAppSetting`）：
  - `enableUrgent: Boolean = false`
  - `urgentMaxAttempts: Int = 3`（范围 1–5）
  - `urgentInitialDelaySeconds: Int = 60`
- UI 待补充：设置页新增“未读加急”分区（开关、最大尝试次数、首次等待秒数）。

### 时序与指数退避

1. 发送消息成功，得到 `message_id`。
2. 若开启加急：调度后台 Worker，第一次检查在 `urgentInitialDelaySeconds` 后执行。
3. 每次检查：
   - 调用 `read_users` 判断是否已读；已读则结束。
   - 未读则调用 `urgent_sms` 尝试加急。
   - 成功或失败（失败不计次）后，按指数退避计算下一次检查时间：`initial * 2^k`。
4. 尝试次数达上限或已读即停止。

### 可靠性与风控

- 鉴权：沿用 `tenant_access_token` 缓存与刷新逻辑，`Authorization: Bearer <token>`。
- 限流/网络异常：不计入尝试次数，延后下一次检查（可加入 ±10% 抖动）。
- 幂等：以 `message_id` 为唯一键调度唯一工作；调用 `urgent_sms` 可携带 `uuid`（可选）。

### 代码改动清单（本次提交）

- 数据模型：扩展 `FeishuAppSetting` 新增 3 个字段（默认值保证兼容）。
- 网络封装：新增 `FeishuAppApi` 提供 `getMessageReadUsers` 与 `sendUrgentSms` 两个方法。
- 后台任务：新增 `FeishuUrgentWorker`（基于 WorkManager），使用 InputData 传递 `appId/userId/messageId`、尝试与退避参数；任务结束条件：已读或达到上限。
- 发送挂钩：在 `FeishuAppUtils.sendTextMsg` 成功回调中解析 `message_id` 并按设置调度 Worker。

### 后续工作（后续提交）

- 设置界面：新增“未读加急”分区与校验。
- 观测与日志：在发送日志中标记关键节点（启动监控、加急尝试、已读、终止原因）。
