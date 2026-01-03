# Ruto

<p align="center">
  <!-- TODO: You can place your project logo under the art/logo.png path -->
  <img src="art/logo.png" alt="Ruto Logo" width="200"/>
</p>

<p align="center">
  <strong>An AI-powered Android Automation and Multitasking Framework, unlocking infinite possibilities.</strong>
</p>

<p align="center">
  <a href="https://github.com/iamr0s/Ruto-GLM/releases"><img src="https://img.shields.io/github/v/release/iamr0s/Ruto-GLM" alt="Latest Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="License"></a>
  <a href="https://github.com/iamr0s/Ruto-GLM/pulls"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg" alt="PRs Welcome"></a>
</p>

---

<div align="center">

**Ruto is participating in the [AutoGLM Real-World Developer Incentive Program](https://mp.weixin.qq.com/s/wRp22dmRVF23ySEiATiWIQ)!**

*If you like this project, please give us your valuable vote. Your support is our greatest motivation! *

</div>

---

## 🤔 What is Ruto?

Ruto (Run, Auto, by iamr0s) is a powerful Android automation and multitasking framework. It leverages system-level capabilities activated by [Shizuku](https://shizuku.rikka.app/) and integrates with large AI models to transform your device into a true smart assistant.

Whether you're performing intelligent automated testing or running multiple instances of an app on a virtual screen, Ruto provides you with powerful and easy-to-use tools.

## 🏆 Ruto's Unique Advantage: More Than Just Automation

Unlike many automation tools that execute tasks on the main screen, Ruto provides **system-level virtualization capabilities**, delivering a revolutionary experience:

*   **💅 Elegant & Fluid UI**: Built entirely with Jetpack Compose, Ruto features a beautiful, modern interface with silky-smooth animations, ensuring a delightful user experience.
*   **🚀 True Background Automation**: You can run any automation task on a **virtual screen** created by Ruto. This means the AI can perform testing, data scraping, or any time-consuming operations in a completely separate background environment, while you can **continue to use your phone's main screen normally without any interruption**. This is a game-changer for automated testing and long-running tasks.
*   **📱 Native App Multi-Instance**: With virtual screens, you can **run any app in an isolated environment**, achieving true "app cloning" or multi-instance operation. Each instance has its own separate running space, providing unprecedented flexibility.
*   **🖥️ Desktop-Grade Multi-Window Management**: We have developed a brand-new, **desktop-like multi-window management interface**. You can easily switch between multiple virtual screens and manage the apps running on them, giving your Android device desktop-level multitasking capabilities.
*   **🤖 Deep AI Integration**: Ruto is more than just a task executor. It features a built-in AI chat interface where you can freely communicate with the AI and then seamlessly have it complete tasks for you on either the **main screen** or any **virtual screen**, delivering a true "say-and-do" smart experience.

## 📸 Screenshots

<p align="center">
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-52-03-28_f59c7c123c76015c9a9703cea7064428.jpg" alt="AI Chat" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-52-10-22_f59c7c123c76015c9a9703cea7064428.jpg" alt="Model Management" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-52-30-24_f59c7c123c76015c9a9703cea7064428.jpg" alt="Virtual Screen Creation" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-53-08-43_f59c7c123c76015c9a9703cea7064428.jpg" alt="Multi-task Preview" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-53-40-52_f59c7c123c76015c9a9703cea7064428.jpg" alt="Running on Virtual Screen" width="200"/>
  <img src="https://raw.githubusercontent.com/iamr0s/Ruto-GLM/main/screenshot/Screenshot_2026-01-04-01-54-10-45_f59c7c123c76015c9a9703cea7064428.jpg" alt="Automation Task" width="200"/>
</p>

## ✨ Core Features

*   **AI Automation Tasks**: Powered by the [Open-AutoGLM](https://github.com/zai-org/Open-AutoGLM) model, Ruto can understand your instructions and automatically perform UI operations such as clicks, swipes, and more on your device.
*   **Virtual Screens & App Cloning**: Create independent virtual screens to run applications. This enables multi-instance operation of apps (app cloning) and allows tasks to run in the background without interfering with your foreground activities.
*   **Desktop-like Multi-Window Management**: We have developed a brand-new, desktop-style multi-window management interface, allowing you to experience efficient multitasking on Android.
*   **AI Chat Integration**: An integrated interface for conversing with large language models. You can easily add your own model API to start intelligent conversations.
*   **Flexible Task Execution**: Automation tasks can be run on the main screen or on any virtual screen, enabling true background automation.

## 🚀 Getting Started

### 1. Download and Install

Download the latest APK file from our [Releases Page](https://github.com/iamr0s/Ruto-GLM/releases) and install it on your device.

### 2. Activate Shizuku

For Ruto to work correctly, you first need to install and start Shizuku according to the [official Shizuku documentation](https://shizuku.rikka.app/guide/setup/) and grant permission to Ruto within the Shizuku app.

### 3. How to Use

#### 🤖 Engaging in AI Conversation

1.  Open the Ruto app and go to the "Models" management screen.
2.  Add a new model and fill in your AI model's API information.
3.  Return to the main screen, add a new conversation, select the model you just added, and start chatting.

#### ⚡ Automating Tasks

Ruto's automation capabilities currently rely on the [Open-AutoGLM](https://github.com/zai-org/Open-AutoGLM) model.

1.  **Select Execution Environment**: In the task settings, choose the screen where you want the task to run.
2.  **Running in the Background (Optional)**:
    *   If you want the task to run in the background, first create a new virtual screen.
    *   In the task settings, assign the execution screen to the virtual screen you created.
3.  **Start the Task**: Launch the task, and the AI will take over and start performing operations on the specified screen.

## 🙌 Contributing

We welcome all forms of contributions! You can get involved in the project by:
*   **Submitting Issues**: Report bugs or suggest new features.
*   **Creating Pull Requests**: Fix bugs or implement new functionalities.
*   **Improving Documentation**: Make the project introduction and usage instructions clearer.

Our project is open-sourced at: [https://github.com/iamr0s/Ruto-GLM](https://github.com/iamr0s/Ruto-GLM)

## 📄 License

Ruto is licensed under the [Apache License 2.0](LICENSE).
