# Ruto

<p align="center">
  <!-- TODO: 您可以在 art/logo.png 路径下放置您的项目Logo -->
  <img src="art/logo.png" alt="Ruto Logo" width="200"/>
</p>

<p align="center">
  <strong>一个基于 AI 的 Android 自动化与多任务框架，开启无限可能。</strong>
</p>

<p align="center">
  <a href="https://github.com/iamr0s/Ruto-GLM/releases"><img src="https://img.shields.io/github/v/release/iamr0s/Ruto-GLM" alt="Latest Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="License"></a>
  <a href="https://github.com/iamr0s/Ruto-GLM/pulls"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg" alt="PRs Welcome"></a>
</p>

---

<div align="center">

**Ruto 正在参加 [AutoGLM 实战派开发者激励活动](https://mp.weixin.qq.com/s/wRp22dmRVF23ySEiATiWIQ)！**

*如果您喜欢这个项目，请为我们投上宝贵的一票，您的支持是我们前进的最大动力！*

</div>

---

## 🤔 Ruto 是什么？

Ruto (Run, Auto, by iamr0s) 是一个强大的 Android 自动化和多任务框架。它借助 [Shizuku](https://shizuku.rikka.app/) 激活的系统级能力，结合 AI 大模型，将您的设备变为一个真正的智能助理。

无论是进行智能自动化测试，还是在虚拟屏幕上实现应用多开，Ruto 都为您提供了强大而易用的工具。

## 🏆 Ruto 的独特优势：不止于自动化

与众多在手机主屏幕上执行任务的自动化工具不同，Ruto 提供了**系统级的虚拟化能力**，带来了革命性的体验：

*   **💅 优美流畅的 UI**：完全使用 Jetpack Compose 构建，Ruto 拥有精美、现代的界面和丝滑流畅的动画，确保为您带来愉悦的用户体验。
*   **🚀 真正的后台自动化**：您可以在 Ruto 创建的**虚拟屏幕**上运行任何自动化任务。这意味着 AI 可以在完全独立的后台环境中进行测试、数据抓取或执行任何耗时操作，而您的手机主屏幕可以**同时正常使用，互不干扰**。这对于自动化测试和长时间运行的任务来说是颠覆性的。
*   **📱 原生应用多实例运行**：借助虚拟屏幕，您可以**在隔离的环境中运行任何应用**，实现真正的“应用分身”或“应用多开”。每个实例都拥有独立的运行空间，为您带来前所未有的灵活性。
*   **🖥️ 桌面级多窗口管理**：我们开发了一个全新的、**类似桌面系统的多窗口管理界面**。您可以轻松地在多个虚拟屏幕之间切换、管理在其上运行的应用，让 Android 设备也能拥有桌面级的多任务处理能力。
*   **🤖 AI 深度集成**：Ruto 不仅仅是任务的执行者。它内置了 AI 对话界面，您可以与 AI 自由交流，然后无缝地让它在**主屏幕**或任何一个**虚拟屏幕**上为您完成任务，实现“说到做到”的智能体验。

## 📸 截图展示

<p align="center">
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-52-03-28_f59c7c123c76015c9a9703cea7064428.jpg" alt="AI 对话" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-52-10-22_f59c7c123c76015c9a9703cea7064428.jpg" alt="模型管理" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-52-30-24_f59c7c123c76015c9a9703cea7064428.jpg" alt="虚拟屏幕创建" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-53-08-43_f59c7c123c76015c9a9703cea7064428.jpg" alt="多任务预览" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-53-40-52_f59c7c123c76015c9a9703cea7064428.jpg" alt="在虚拟屏幕上运行" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-54-10-45_f59c7c123c76015c9a9703cea7064428.jpg" alt="自动化任务" width="200"/>
</p>

## ✨ 核心功能

*   **AI 自动化任务**: 基于 [Open-AutoGLM](https://github.com/zai-org/Open-AutoGLM) 模型，Ruto 可以理解您的指令，并在您的设备上自动执行 UI 操作，如点击、滑动等。
*   **虚拟屏幕与应用多开**: 创建独立的虚拟屏幕，让应用在其上运行。这不仅能实现应用的多实例运行（应用分身），还能在后台执行任务而不干扰您的前台操作。
*   **桌面级多窗口管理**: 我们开发了一个全新的、类似桌面系统的多窗口管理界面，让您在 Android 上也能体验高效的多任务处理。
*   **AI 对话集成**: 内置了与大语言模型对话的界面，您可以轻松添加自己的模型 API，与 AI 进行智能交流。
*   **灵活的任务执行**: 自动化任务既可以在主屏幕上运行，也可以在任何一个虚拟屏幕上执行，实现真正的后台自动化。

## 🚀 如何开始

### 1. 下载与安装

从我们的 [Releases 页面](https://github.com/iamr0s/Ruto-GLM/releases) 下载最新的 APK 文件并安装到您的设备上。

### 2. 激活 Shizuku

为了让 Ruto 正常工作，您需要先根据 [Shizuku 官方文档](https://shizuku.rikka.app/guide/setup/) 完成安装和启动，并在 Shizuku 应用中为 Ruto 授权。

### 3. 使用方法

#### 🤖 进行 AI 对话

1.  打开 Ruto 应用，进入“模型”管理界面。
2.  新增一个模型，并填入您的 AI 模型 API 信息。
3.  返回主界面，新增一个对话，选择您刚添加的模型，即可开始聊天。

#### ⚡ 自动化任务

Ruto 的自动化能力目前依赖 [Open-AutoGLM](https://github.com/zai-org/Open-AutoGLM) 模型。

1.  **选择运行环境**: 在任务设置中，选择您希望任务运行的屏幕。
2.  **后台运行 (可选)**:
    *   如果您希望任务在后台执行，请先新建一个虚拟屏幕。
    *   在任务设置中，将运行屏幕指定为您创建的虚拟屏幕。
3.  **开始任务**: 启动任务，AI 将接管并在指定的屏幕上开始执行操作。

## 🙌 贡献

我们欢迎任何形式的贡献！您可以通过以下方式参与项目：
*   **提交 Issue**: 报告 Bug 或提出功能建议。
*   **发起 Pull Request**: 修复 Bug 或实现新功能。
*   **改进文档**: 让项目介绍和使用说明更清晰。

项目开源在: [https://github.com/iamr0s/Ruto-GLM](https://github.com/iamr0s/Ruto-GLM)

## 📄 许可证

Ruto 使用 [Apache License 2.0](LICENSE) 许可证。
