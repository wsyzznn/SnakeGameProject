# SnakeGameProject
# 基于 DDS 的多人实时贪吃蛇系统实施指南

本项目是一个基于 **Data Distribution Service (DDS)** 中间件开发的分布式实时对战系统[cite: 3, 5]。文档主要说明如何配置环境并部署实施该系统。

---

## 🛠 环境配置要求

### 1. 核心中间件：ZR-DDS
系统依赖 DDS 实现数据分发与状态同步[cite: 3]。
*   **网络要求**：所有物理机或虚拟机必须处于同一局域网段。
*   **协议支持**：确保网络环境支持 **UDP 组播**（Multicast），这是 DDS 发现机制的基础。
*   **配置文件**：确保 `DDS_DOMAIN_ID` 在所有终端（Android & Java Backend）中保持一致。

### 2. 开发与运行环境
*   **后端 (Server)**: JDK 17+, Maven 3.x, MySQL 8.0[cite: 4]。
*   **前端 (Android)**: Android Studio Dolphin+, SDK 31+[cite: 4]。
*   **中间件工具**: ZR-DDS 动态库及相应的 IDL 编译器。

---

## 🚀 系统实施步骤

### 第一步：数据模型定义 (IDL)
在使用系统前，需通过 IDL 文件定义通信拓扑。
1.  编写或修改 `SnakeData.idl`，定义玩家坐标、食物位置及游戏状态结构[cite: 3]。
2.  使用 DDS 编译器生成 Java 语言的辅助类（包括 TypeSupport, DataReader, DataWriter）。

### 第二步：后端与数据库部署
1.  **数据库初始化**：
    *   执行项目中的 SQL 脚本，创建 `user`（存储账号[cite: 6]）和 `game_logs` 等表。
2.  **后端逻辑配置**：
    *   修改 `config.properties`，配置数据库连接池。
    *   配置 **QoS (Quality of Service)** 策略：针对位置信息建议使用 `RELIABILITY_BEST_EFFORT`（追求实时性），针对登录消息建议使用 `RELIABILITY_RELIABLE`[cite: 3]。
3.  **启动服务**：运行后端主程序，开启 DDS 监听。

### 第三步：Android 客户端实施
1.  **导入动态库**：将 DDS 的 `.so` 文件放入 Android 项目的 `jniLibs` 目录。
2.  **权限配置**：在 `AndroidManifest.xml` 中开启网络权限及组播权限：
    *   `android.permission.INTERNET`
    *   `android.permission.CHANGE_WIFI_MULTICAST_STATE`
3.  **编译打包**：连接物理设备进行调试（建议不要使用模拟器，以免组播数据包被屏蔽）。

---

## 🔍 关键配置点拨

### 1. 分布式同步逻辑
系统采用 **发布/订阅模型**[cite: 3]：
*   **客户端**：发布 `SnakeDirection`（方向指令），订阅 `GameState`（全局画面同步）。
*   **后端**：订阅指令进行逻辑计算（碰撞、得分），发布最新的地图状态。

### 2. 性能调试
若在实施中发现延迟，请检查以下配置：
*   **检查 Domain ID**：确认是否受到同网段其他 DDS 程序的干扰。
*   **调整心跳频率**：在 QoS 配置文件中修改 `heartbeat_period`，优化高频坐标同步性能。

---

## 📈 系统测试结论
根据实训测试，在 3-5 人局域网对战环境下[cite: 5]：
*   **平均延迟**：< 50ms。
*   **并发稳定性**：支持多房间并行，状态同步无明显错位。
