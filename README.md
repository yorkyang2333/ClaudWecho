<div align="center">
  <!-- 🎨 [Logo Placeholder] -->
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="120" alt="ClaudWecho Logo Placeholder">
  <br>
  <sub>*(专属 App 图标待征集中...)*</sub>
  
  <h1>ClaudWecho</h1>

  <p>
    <img src="https://img.shields.io/badge/Platform-Wear%20OS-brightgreen.svg" alt="Platform">
    <img src="https://img.shields.io/badge/Language-Kotlin-orange.svg" alt="Kotlin">
    <img src="https://img.shields.io/badge/Architecture-MVVM-blue.svg" alt="MVVM">
    <img src="https://img.shields.io/badge/License-GPL%203.0-blue.svg" alt="License">
    <img src="https://img.shields.io/badge/PRs-Welcome-brightgreen.svg" alt="PRs Welcome">
    <img src="https://img.shields.io/github/last-commit/yorkyang2333/ClaudWecho" alt="Last Commit">
    <img src="https://img.shields.io/github/repo-size/yorkyang2333/ClaudWecho" alt="Repo Size">
    <img src="https://img.shields.io/github/stars/yorkyang2333/ClaudWecho?style=social" alt="Stars">
  </p>
</div>

**ClaudWecho** (读作: /klɔːd ˈwɛkoʊ/) 是一款专为 Android 智能手表（Wear OS / Android Wear）打造的“某知名音乐软件”第三方客户端。致力于为腕上设备提供轻量、流畅、沉浸的音乐播放体验。

项目名字源自 **Claud(e) + We(ar) + Echo** 的巧妙拼写组合，蕴含着它的诞生灵感：
* **Claud(e)**：由 AI 伙伴 Claude 启发并协力构建的极客产物；
  * ~~其实根本用不起 用的Gemini~~
* **We(ar)**：专注于腕上设备的轻量化与高适配性；
* **Echo**：让旋律在方寸之间的表盘上长久回响。

## 🌟 核心特性 (Features)

* **纯粹的腕上体验**：基于 Wear OS 最佳实践设计的用户交互，操作逻辑清晰流畅。
* **原生性能体验**：采用标准 Android View 架构 (Activity + Fragment + XML)，结合 ViewBinding 实现高性能渲染，摆脱卡顿。
* **音乐无缝播放**：深度集成 MediaSession 和前台服务，支持后台播放，随时随地享受音乐。
* **现代技术栈**：基于 Kotlin 编写，使用 Koin 进行依赖注入，项目结构清晰，易于扩展与维护。

## 🛠️ 技术架构 (Tech Stack)

* **Language**: Kotlin
* **Architecture**: MVVM
* **UI**: Android View System (XML, RecyclerView, WearableRecyclerView, ViewBinding)
* **Dependency Injection**: Koin
* **Media**: ExoPlayer (Media3), MediaSession
* **Networking/Data**: Retrofit, Kotlin Coroutines, Flow

## 🚀 快速开始 (Getting Started)

### 环境要求
* Android Studio (推荐最新版)
* JDK 17
* Wear OS 模拟器或实体手表 (如 OPPO Watch 系列、Galaxy Watch 等)

### 编译与运行
1. 克隆项目到本地：
   ```bash
   git clone https://github.com/yorkyang2333/ClaudWecho.git
   ```
2. 使用 Android Studio 打开项目。
3. 连接你的 Wear OS 设备或启动 Wear OS 模拟器。
4. 点击 `Run` 按钮或在终端执行：
   ```bash
   ./gradlew installDebug
   ```

## ⚠️ 目前的不足与已知问题 (Known Issues & Limitations)
* **性能瓶颈**：在部分安卓手表（例如作者本人的 OPPO Watch X3）上，目前存在较为明显的性能问题和异常卡顿，可能与架构实现或系统适配有关。非常需要有性能调优或 Wear OS 开发经验的大佬提供支持与帮助。
* **功能待完善**：目前仍有一些进阶功能（如“听歌识曲”等）尚未开发完成。

## 🤝 贡献与反馈 (Contributing)
如果你有好的想法、发现了 Bug，或是想为 ClaudWecho 贡献代码，非常欢迎提交 Issue 或 Pull Request。

**🎨 图标征集中 (Icon Wanted!)**：项目目前还没有专属的 App 图标（目前使用默认的占位图）。如果你有设计特长，愿意为 ClaudWecho 设计一枚好看的专属图标，欢迎随时联系或提交 PR！

## 📝 声明 (Disclaimer)
本项目为个人学习与研究所作的第三方客户端，不对任何因使用本软件造成的风险负责。请支持正版软件与正版数字音乐，同时也由衷希望官方能早日把自家的手表客户端做得更好用。

## 📄 开源协议与反倒卖声明 (License & Anti-Resale)
本项目基于 **[GPL-3.0 License](LICENSE)** 开源。

> **⚠️ 严正声明**：本软件**完全免费**且**开源**。**严禁任何人将本项目及打包后的 APK 用于任何形式的商业售卖（包括但不限于闲鱼、淘宝等平台）或打包盈利**。如果您是付费购买的本软件，说明您已被骗，请立即要求退款并向平台举报卖家！
