# Xylonet Snake 开发日志

## 2025-10-03 开发会话记录

### 项目概述
- **项目名称**: Xylonet_Snake
- **类型**: 有攻击功能的贪吃蛇游戏
- **技术栈**: Java (Swing UI) + Python (AI 分析) + SQLite + PyTorch + GPT-4o
- **目标**: 30帧闯关游戏，AI 根据玩家行为定制关卡

### 游戏设计要求

#### 核心机制
1. **控制方式**:
   - WASD: 移动蛇
   - 方向键(↑↓←→): 射击（不能向后射击）
   - 蛇有转向延迟，不能 180° 掉头

2. **地图与规则**:
   - 地图大小: 64x64 固定
   - 边界墙血量: 9999（几乎不可破坏）
   - 内部障碍物: 可破坏
   - 撞墙/撞自己/撞障碍物 = 死亡
   - 每关限时 3 分钟

3. **武器系统**:
   - 无限子弹（显示为 ∞）
   - 射击冷却: 1 秒
   - 子弹碰到第一个障碍就消失

4. **食物与过关**:
   - 只有普通食物，吃一个 +1 节
   - 达到一定长度后出现小门（一直存在）
   - 进门后 AI 分析玩家行为，定制下一关

#### UI 设计
- **分辨率**: 1200x900
- **风格**: ASCII 黑底白字（不是黑客帝国绿色！）
- **布局**:
  ```
  ┌───────────────────┬─────────────┐
  │                   │  InfoPanel  │
  │   GamePanel       │  (右上)     │
  │   640x640         ├─────────────┤
  │   (64x64格子)     │  Console    │
  │                   │  (右下)     │
  └───────────────────┴─────────────┘
  ```

- **符号定义**:
  - 蛇头: `□` (空心方块) - 白色
  - 蛇身: `#` - 白色
  - 食物: `◉` - 金色
  - 子弹: `@` - 浅红色
  - 障碍: `■` - 灰色

- **信息面板** (右上，垂直排列):
  - 分数
  - 长度
  - 关卡
  - 时间 (倒计时)
  - MBTI 类型
  - 子弹数
  - 暂停按钮

- **Console** (右下):
  - 带历史记录功能（↑↓ 键翻看）
  - 支持命令输入
  - AI 每 30 秒提问一次（问玩家感受、意见）
  - 滚动显示历史消息

#### AI 分析系统
1. **数据收集** (SQLite):
   - 玩家路径选择（贴墙/中央）
   - 射击频率和准确率
   - 面对障碍物的反应时间
   - 是否激进（冒险吃食物）还是保守（绕远路）
   - 死亡时的状态（位置、长度、原因）

2. **MBTI 推断**:
   - 计算 4 个维度分数：aggression, caution, exploration, planning
   - 把数据和分析结果发送给 GPT-4o
   - GPT-4o 返回 MBTI 类型和个性分析

3. **关卡生成**:
   - 根据 MBTI 和玩家行为调整：
     - 障碍物数量和分布
     - 食物位置（"讨好"或"恶心"玩家）
     - 门的出现位置
     - 地图复杂度

#### 技术架构
- **Java**:
  - Swing UI (MainWindow, GamePanel, InfoPanel, ConsolePanel)
  - 游戏逻辑 (Snake, Food, Obstacle, Bullet, GameBoard, GameEngine)
  - 数据库操作 (GameDatabase - SQLite)
  - Python 进程管理 (PythonProcessManager)
  - Socket 客户端 (AIClient)

- **Python**:
  - Socket 服务器 (ai_service.py) - 动态端口 50705+
  - 行为分析器 (behavior_analyzer.py)
  - 数据库初始化 (init_database.py)

- **数据库** (SQLite - `data/snake_game.db`):
  - game_sessions - 游戏会话
  - player_actions - 玩家操作记录
  - shooting_events - 射击事件
  - food_collection - 食物收集
  - game_snapshots - 游戏状态快照
  - mbti_analysis - MBTI 分析历史
  - player_stats - 玩家统计

- **游戏引擎**:
  - 30 FPS
  - 游戏逻辑更新和渲染频率分离

#### 存档系统
- 支持多个存档（不同档案使用不同数据库）
- 记录历史最高分和通关记录
- 可暂停/继续游戏

#### 部署方案
- 打包成 .zip 文件
- 内含：Java .jar + Python 脚本 + 启动脚本 (.sh 和 .bat)
- 打包独立 Python runtime
- 首次运行时下载 PyTorch 模型

---

## 已完成的工作

### Phase 1: UI 框架搭建 ✅

#### 已创建的文件:
1. **MainWindow.java** - 主窗口 (1200x900，左右布局)
2. **GamePanel.java** - 游戏渲染面板 (64x64 网格)
   - 预定义颜色和符号常量
   - 提供 `drawCell()` 和 `drawSymbol()` 方法
3. **InfoPanel.java** - 信息面板 (右上，垂直布局)
   - 显示所有游戏数据
   - 可动态更新
4. **ConsolePanel.java** - 控制台面板 (右下)
   - ✅ 历史记录功能 (↑↓ 键)
   - ✅ 命令处理系统
   - ✅ 彩色消息输出
   - ✅ AI 提问接口
   - ✅ 滚动条和历史记录
5. **GameBoard.java** - 游戏板类（占位，待完善）

#### UI 测试状态:
- ✅ 窗口正常显示
- ✅ 布局正确（左侧游戏区，右侧信息+控制台）
- ✅ Console 历史记录功能正常
- ✅ 颜色方案已改为白色（不是绿色）

### Phase 0: Java-Python 通信框架 ✅ (之前完成)
- Socket IPC 实现
- Python 进程管理
- SQLite 数据收集
- 基础 MBTI 分析

---

## 待完成的工作

### Phase 2: 游戏主体实现 (下一步)
- [ ] 创建游戏实体类
  - [ ] Snake.java - 蛇的移动、转向、延迟
  - [ ] Food.java - 食物生成
  - [ ] Obstacle.java - 障碍物（可破坏，血量系统）
  - [ ] Bullet.java - 子弹飞行、碰撞
  - [ ] Door.java - 过关门

- [ ] 完善 GameBoard.java
  - [ ] 64x64 网格管理
  - [ ] 实体位置跟踪
  - [ ] 碰撞检测系统

- [ ] 实现键盘输入处理
  - [ ] WASD 移动（带延迟）
  - [ ] 方向键射击（冷却 1 秒）
  - [ ] 暂停键 (P 或 ESC)

- [ ] 创建 GameEngine.java
  - [ ] 30 FPS 游戏循环
  - [ ] 逻辑与渲染分离
  - [ ] 时间倒计时 (3 分钟)
  - [ ] 游戏状态管理 (开始/暂停/结束)

### Phase 3: 数据记录与分析
- [ ] 集成 GameDatabase 到游戏循环
- [ ] 每帧记录关键操作
- [ ] 每 30 秒触发 AI 提问
- [ ] 每关结束后分析 MBTI

### Phase 4: AI 关卡生成
- [ ] 实现关卡生成器
- [ ] 根据 MBTI 调整难度
- [ ] GPT-4o API 集成

### Phase 5: 打包部署
- [ ] 创建启动脚本
- [ ] 打包 Python runtime
- [ ] 测试 macOS 和 Windows 兼容性

---

## 技术问题修复记录

### 问题 1: Package 名错误 ✅ 已修复
**症状**: IntelliJ 报错但命令行能运行

**原因**: 用户错误地将 package 改成了：
```java
package main.java.com.xylonet.snake.ui;  // ❌ 错误
```

**正确写法**:
```java
package com.xylonet.snake.ui;  // ✅ 正确
```

**修复**: 已修改所有文件的 package 声明

### 问题 2: IntelliJ 项目结构配置 ✅ 已修复
**症状**: 能编译运行但 IDE 到处报错

**原因**: `Xylonet_Snake.iml` 配置错误
- ❌ 旧配置: `<sourceFolder url="file://$MODULE_DIR$/src" />`
- ✅ 新配置: `<sourceFolder url="file://$MODULE_DIR$/src/main/java" />`

**修复内容**:
```xml
<content url="file://$MODULE_DIR$">
  <sourceFolder url="file://$MODULE_DIR$/src/main/java" isTestSource="false" />
  <sourceFolder url="file://$MODULE_DIR$/python_ai" isTestSource="false" />
  <excludeFolder url="file://$MODULE_DIR$/out" />
  <excludeFolder url="file://$MODULE_DIR$/data" />
</content>
```

### 问题 3: Python 报错 (str, int, len 等 Unresolved) ✅ 已修复
**症状**: Python 文件报 63 个错误，所有内置函数都无法识别

**原因**: `.venv` 虚拟环境配置错误，链接指向了错误的 Python

**解决方案**:
1. ✅ 删除了 `.venv` 目录
2. ⏳ 需要在 IntelliJ 中配置系统 Python SDK
3. ⏳ 需要重启 IntelliJ 并 Invalidate Caches

**用户需要做的**:
1. 打开 `File → Project Structure → SDKs`
2. 确保 Python 3.13 的路径是：`/Library/Frameworks/Python.framework/Versions/3.13/bin/python3`
3. 在 `Modules → Dependencies` 中确认有 `Python 3.13 interpreter library`
4. 重启 IntelliJ: `File → Invalidate Caches → Invalidate and Restart`

---

## 项目文件结构

```
Xylonet_Snake/
├── src/main/java/com/xylonet/snake/
│   ├── Main.java                    # ✅ 通信测试主程序
│   ├── ui/
│   │   ├── MainWindow.java          # ✅ 主窗口
│   │   ├── GamePanel.java           # ✅ 游戏渲染面板
│   │   ├── InfoPanel.java           # ✅ 信息面板
│   │   └── ConsolePanel.java        # ✅ 控制台面板
│   ├── game/
│   │   ├── GameBoard.java           # ⏳ 占位类
│   │   ├── Snake.java               # ❌ 待创建
│   │   ├── Food.java                # ❌ 待创建
│   │   ├── Obstacle.java            # ❌ 待创建
│   │   ├── Bullet.java              # ❌ 待创建
│   │   ├── Door.java                # ❌ 待创建
│   │   └── GameEngine.java          # ❌ 待创建
│   ├── network/
│   │   ├── PythonProcessManager.java # ✅ Python 进程管理
│   │   └── AIClient.java            # ✅ Socket 客户端
│   └── data/
│       └── GameDatabase.java        # ✅ SQLite 操作
│
├── python_ai/
│   ├── ai_service.py                # ✅ Socket 服务器
│   └── scripts/
│       ├── init_database.py         # ✅ 数据库初始化
│       └── behavior_analyzer.py     # ✅ MBTI 分析器
│
├── lib/
│   ├── gson-2.10.1.jar              # ✅ JSON 处理
│   └── sqlite-jdbc-3.44.1.0.jar     # ✅ SQLite JDBC
│
├── data/
│   └── snake_game.db                # ✅ 运行时生成
│
├── out/                             # 编译输出
├── .idea/                           # IntelliJ 配置
├── Xylonet_Snake.iml                # ✅ 已修复
└── CLAUDE.md                        # ✅ 项目概述文档
```

---

## 命令速查

### 编译运行
```bash
# 清理并编译所有 Java 文件
cd "/Users/lainos/Dropbox/Ptolemaeus Studio/Turner Sienter/Java projects/IntelliJ/Xylonet_Snake"
rm -rf out
javac -d out -cp "lib/*" src/main/java/com/xylonet/snake/**/*.java

# 运行 UI 测试
java -cp "out:lib/*" com.xylonet.snake.ui.MainWindow

# 运行通信测试
java -cp "out:lib/*" com.xylonet.snake.Main
```

### Python 相关
```bash
# 初始化数据库
python3 python_ai/scripts/init_database.py

# 手动启动 AI 服务（调试用）
python3 python_ai/ai_service.py
```

---

## 下次开发重点

1. **游戏实体类实现** (优先级最高)
   - 从 Snake.java 开始
   - 实现基础移动逻辑
   - 测试渲染是否正常

2. **键盘输入处理**
   - WASD 移动
   - 方向键射击

3. **游戏循环**
   - 30 FPS 定时器
   - 简单的碰撞检测

4. **逐步集成功能**
   - 先让基础游戏跑起来
   - 再加入 AI 分析
   - 最后完善关卡生成

---

## 重要提醒

- ⚠️ **不要再改 package 名！** 应该是 `com.xylonet.snake.*`，不包含 `main.java.`
- ⚠️ **IntelliJ 重启后** 记得检查 Python SDK 配置
- ⚠️ **所有颜色都用白色主题**，不要用绿色
- ⚠️ **食物是 ◉，子弹是 @**

---

## 联系人信息
- 开发者: lainos
- 项目路径: `/Users/lainos/Dropbox/Ptolemaeus Studio/Turner Sienter/Java projects/IntelliJ/Xylonet_Snake`
- Java SDK: Oracle OpenJDK 21.0.8
- Python: 3.13.5
