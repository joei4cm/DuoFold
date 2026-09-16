# DuoFold

面向 **小米 18 Fold / HyperOS 4** 的独立折叠屏 Android 桌面。

**v0.6.0** · 包名 `com.duofold.launcher` · [MIT](LICENSE)

[English README](README.md)

DuoFold 是原创 Home 应用，关注折叠连续性：外屏 ↔ 内屏布局、柔和材质、外屏 StandBy、跟手编辑、小组件，以及可选的 HyperOS Assist 手势。**与** Apple、小米或其他桌面产品**无关联**。

## 功能

| 方向 | 内容 |
|---|---|
| 桌面 | 4×6 网格、横向分页、毛玻璃竖向 Dock |
| 折叠 | 展开时左侧 Leading 画布；可选铰链叶片过渡 |
| StandBy | 外屏横置多页（时钟 / 画布 / Glance） |
| 编辑 | 长按编辑；网格、Leading、Dock 之间跟手拖拽 |
| 小组件 | 原生 AppWidget 宿主 + 首方 Glance 卡片 |
| Assist | 可选顶部下拉 shade + 软导航（仅无障碍全局动作） |
| LockSurface | 可选解锁后玻璃时钟（**不**替换系统锁屏） |

布局与偏好仅保存在本机。无账号、无统计、无激活服务器。

## 环境要求

- **compile / target：** Android 17（API 37）
- **minSdk：** 31（Android 12+）— 向下兼容较旧折叠屏，同时面向 Android 17
- 本地构建需 JDK 17
- Android SDK Platform 37（及 Build-Tools 37）

## 构建

调试包：

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

签名正式包（密钥库必须在仓库外）：

```sh
./scripts/generate-release-keystore.sh   # 仅首次
./scripts/release-signed.sh
# → app/build/outputs/apk/release/app-release.apk
```

详见 [docs/signing.md](docs/signing.md)。

## 安装

1. 安装正式版 APK。
2. 打开 DuoFold → **设为默认桌面**（或系统默认应用）。
3. 可选：设置中开启下拉 shade / 软导航，并在无障碍中启用 **DuoFold Assist**。

## 文档

- [架构](docs/architecture.md)
- [愿景 / 路线](docs/vision-iphone-duo.md)
- [用户指南](docs/user-guide.md)
- [QA 矩阵](docs/qa-matrix.md)
- [设计笔记](docs/design-notes.md)
- [隐私](PRIVACY.md)
- [第三方声明](THIRD_PARTY_NOTICES.md)

## CI

`main` 上的 GitHub Actions：

1. 跑单元测试
2. 使用仓库 Secrets 打出**签名正式 APK**（见 [docs/signing.md](docs/signing.md)）
3. 上传产物 **DuoFold-release**

Pull Request 只跑单元测试（不需要签名 Secrets）。

## 许可证

[MIT](LICENSE) — DuoFold 贡献者。
