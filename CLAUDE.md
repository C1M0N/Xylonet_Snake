# CLAUDE.md - Xylonet Snake 项目完整文档

**重要提醒**:
- 使用中文回答用户问题
- 本文档是 Claude Code 的完整上下文记忆，包含项目概述、开发历史和当前状态

---

## 项目概述

Xylonet_Snake 是一个带有 AI 行为分析的贪吃蛇游戏。项目使用 **Java (Swing UI)** 实现游戏逻辑，**Python (Socket 服务)** 进行实时行为分析。Java 和 Python 通过 Socket IPC 通信，使用 SQLite 作为共享数据存储。

### 核心特性
- 30 FPS 闯关游戏，支持移动和射击
- ASCII 黑底白字风格界面（不是黑客帝国绿色！）
- AI 根据玩家行为进行 MBTI 人格分析
- 基于 MBTI 类型定制关卡难度

---

## 架构设计

### Java-Python 通信流程

1. **Java Main** → `PythonProcessManager.startPythonService()` 启动 Python 子进程
2. **Python** `ai_service.py` 绑定动态端口 (50705+)，将端口写入 `data/ai_port.txt`
3. **Java** `AIClient` 读取端口文件并通过 Socket 连接
4. **运行时**: 游戏将玩家操作写入 SQLite；Python 读取 SQLite 进行 MBTI 分析
5. **关闭**: Java 自动停止 Python 进程

### 关键组件

**Java** (`src/main/java/com/xylonet/snake/`)
- `ui/` - Swing UI 组件
  - `MainWindow.java` - 主窗口 (1200x900，左右布局)
  - `GamePanel.java` - 游戏渲染面板 (64x64 网格)
  - `InfoPanel.java` - 信息面板 (右上)
  - `ConsolePanel.java` - 控制台面板 (右下，支持命令历史)
  - `ProgressBarPanel.java` - 进度条面板 (顶部独立区域)

- `game/` - 游戏逻辑
  - `GameEngine.java` - 30 FPS 游戏循环，状态管理
  - `GameBoard.java` - 64x64 网格管理，实体位置跟踪
  - `Snake.java` - 蛇的移动、转向、延迟
  - `Food.java` - 食物生成
  - `Obstacle.java` - 障碍物（可破坏，血量系统）
  - `Bullet.java` - 子弹飞行、碰撞
  - `Door.java` - 过关门

- `network/` - 网络通信
  - `PythonProcessManager.java` - 跨平台 Python 进程生命周期管理
  - `AIClient.java` - Socket 客户端，异步消息传递 (PING, GAME_STATE, REQUEST_ANALYSIS)

- `data/` - 数据持久化
  - `GameDatabase.java` - SQLite 操作 (sessions, actions, snapshots, MBTI results)

**Python** (`python_ai/`)
- `ai_service.py` - Socket 服务器，处理来自 Java 的消息
- `scripts/behavior_analyzer.py` - 计算 4 个维度分数 (aggression, caution, exploration, planning) 推断 MBTI
- `scripts/init_database.py` - 创建 7 个 SQLite 表

### 数据库模式

SQLite 数据库位于 `data/snake_game.db`，包含：
- `game_sessions` - 会话元数据 (开始/结束时间, 分数, 结果)
- `player_actions` - 每次移动、方向改变
- `shooting_events` - 射击使用
- `food_collection` - 食物拾取事件
- `game_snapshots` - 周期性游戏状态快照
- `mbti_analysis` - MBTI 分析结果和置信度分数
- `player_stats` - 聚合统计数据

---

## 游戏设计

### 核心机制

1. **控制方式**:
   - WASD: 移动蛇
   - 方向键(↑↓←→): 射击（不能向后射击）
   - Shift: 冲刺（速度加速到 33ms）
   - 空格: 开始/暂停游戏
   - T: 打开控制台
   - ESC: 从控制台返回游戏
   - P: 暂停
   - R: 重新开始
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
   - 每关需要比初始长度多固定数量才能出现门：
     - 第1关：初始长度3 → 需要长度13（+10）
     - 第2关：初始长度3 → 需要长度18（+15）
     - 第3关：初始长度3 → 需要长度23（+20）
     - 增长公式：每关 +5 递增
   - 进门后进入下一关

5. **速度系统**:
   - 速度公式: y = -10ln(x-1) + 70
   - x 是速度等级（关卡数），y 是毫秒延迟
   - 特殊情况：等级 1 固定为 100ms
   - 命令：`/snake speed [等级]` 或 `/snake speed info`
   - 冲刺速度：按住 Shift 时固定为 33ms（30 FPS）

6. **障碍物密度**:
   - 第 n 关的障碍物密度 = n%
   - 计算公式：(GRID_SIZE - 2)² × level / 100
   - 生成顺序：先放蛇，再放障碍物（避免重叠）

### UI 设计

- **分辨率**: 1200x900
- **风格**: ASCII 黑底白字
- **布局**:
  ```
  ┌────────────────────┬═══════════════┐
  │  进度条 (Food to door: X)           │
  ├════════════════════╬═══════════════┤
  │                    ║  InfoPanel    │
  │   GamePanel        ║  (右上)       │
  │   640x640          ╠═══════════════╣
  │   (64x64格子)      ║  Console      │
  │                    ║  (右下)       │
  └────────────────────╩═══════════════┘
  ```

- **符号定义**:
  - 蛇头: `□` (空心方块) - 浅灰色 (200, 200, 200)
  - 蛇身: `#` - 白色
  - 食物: `◉` - 金色 (255, 215, 0)
  - 子弹: `@` - 浅红色 (255, 100, 100)
  - 障碍: `■` - 灰色 (128, 128, 128)
  - 边界: 深红色 (200, 0, 0)
  - 门: `◇` - 亮蓝色 (0, 200, 255)

- **信息面板** (右上):
  - 分数
  - 长度
  - 关卡
  - 时间 (倒计时)
  - MBTI 类型
  - 子弹数 (∞)

- **进度条** (顶部):
  - 显示距离门激活还需多少食物
  - 门出现后进度条消失
  - 用双线 (═) 分隔

- **Console** (右下):
  - 带历史记录功能（↑↓ 键翻看）
  - 所有命令必须以 `/` 开头
  - 支持命令：
    - `/help` - 显示帮助
    - `/snake speed [等级]` - 设置速度等级
    - `/snake speed info` - 显示当前速度信息
    - `/snake speed` - 重置为默认速度
    - `/clear` - 清空控制台
    - `/status` - 显示游戏状态
    - `/history` - 查看命令历史
  - 显示系统级别信息：
    - ✅ 关卡变化（=== Level X ===）
    - ✅ 关卡完成（关卡 X 完成！按 SPACE 继续）
    - ✅ 游戏开始/暂停/继续/重置
    - ✅ 游戏结束（撞墙或时间到）
    - ✅ 报错信息
  - 不显示实时游戏信息：
    - ❌ 分数实时更新
    - ❌ 长度实时更新
    - ❌ 吃到食物的提示
  - AI 每 30 秒提问一次（问玩家感受、意见）
  - 行距调整：30% 额外行距 (StyleConstants.setLineSpacing)
  - T 键聚焦，ESC 返回游戏

- **游戏状态覆盖层**:
  - "GAME OVER" (红色) - 蛇死亡时
  - "TIME'S UP" (红色) - 超时时
  - "LEVEL X COMPLETE!" (绿色) - 过关时
  - 半透明黑色背景，大号字体居中显示
  - 显示提示文字（按 R 重新开始或 SPACE 继续）

### AI 分析系统

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

---

## 开发命令

### 初始设置

```bash
# 1. 初始化数据库（只需运行一次）
python3 python_ai/scripts/init_database.py

# 2. 下载 Java 依赖到 lib/（如果尚未存在）
curl -o lib/gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
curl -o lib/sqlite-jdbc-3.44.1.0.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar

# 3. 将 JAR 添加到 IntelliJ: File → Project Structure → Modules → Dependencies → + → JARs or directories → select lib/
```

### 运行项目

**在 IntelliJ 中**:
- 运行 `MainWindow.java` 启动游戏
- 运行 `Main.java` → 选择选项 `5` 进行完整集成测试

**手动启动 Python 服务**（用于调试）:
```bash
python3 python_ai/ai_service.py
# 输出: [AI服务] 启动成功，监听端口: 50705
```

### 编译运行

```bash
# 清理并编译所有 Java 文件
cd "/Users/lainos/Dropbox/Ptolemaeus Studio/Turner Sienter/Java projects/IntelliJ/Xylonet_Snake"
rm -rf out
javac -d out -cp "lib/*" src/main/java/com/xylonet/snake/**/*.java

# 运行游戏
java -cp "out:lib/*" com.xylonet.snake.ui.MainWindow

# 运行通信测试
java -cp "out:lib/*" com.xylonet.snake.Main
```

### 测试各个组件

**测试 Python Socket 服务器**:
```bash
python3 python_ai/ai_service.py
# 在另一个终端：
nc localhost 50705
# 输入: {"type":"PING"}
# 响应: {"type":"PONG"}
```

**测试数据库初始化**:
```bash
python3 python_ai/scripts/init_database.py
sqlite3 data/snake_game.db ".tables"
# 应显示: food_collection, game_sessions, game_snapshots, mbti_analysis, player_actions, player_stats, shooting_events
```

**测试行为分析器**（需要现有数据）:
```bash
# 首先运行 Main.java 选项 2 生成测试数据
python3 -c "from python_ai.scripts.behavior_analyzer import analyze_from_database; print(analyze_from_database())"
```

---

## 项目状态

### 已完成的阶段 ✅

#### Phase 0/1: Java-Python 通信框架
- Socket IPC 动态端口分配
- Python 进程生命周期管理
- SQLite 数据收集
- 基础 MBTI 启发式分析

#### Phase 1: UI 框架搭建
**已创建的文件**:
1. **MainWindow.java** - 主窗口 (1200x900，左右布局)
   - 使用 BorderLayout 组织面板
   - 添加双线分隔符（║ 和 ═）
   - 键盘事件处理（WASD, 方向键, Shift, T, ESC, P, R, 空格）

2. **GamePanel.java** - 游戏渲染面板 (64x64 网格)
   - 预定义颜色和符号常量
   - 提供 `drawCell()` 和 `drawSymbol()` 方法
   - 绘制游戏元素（蛇、食物、障碍、子弹、门）
   - 游戏状态覆盖层

3. **InfoPanel.java** - 信息面板 (右上，垂直布局)
   - 显示所有游戏数据
   - 可动态更新

4. **ConsolePanel.java** - 控制台面板 (右下)
   - 历史记录功能 (↑↓ 键)
   - 命令处理系统（所有命令必须以 `/` 开头）
   - 彩色消息输出
   - AI 提问接口
   - 滚动条和历史记录
   - 行距调整（30% 额外行距）
   - T 键聚焦，ESC 返回游戏

5. **ProgressBarPanel.java** - 进度条面板
   - 显示距离门激活还需多少食物
   - 门出现后消失
   - 位于顶部独立区域

#### Phase 2: 游戏主体实现
**已创建的文件**:
1. **Snake.java** - 蛇的移动、转向、延迟
2. **Food.java** - 食物生成
3. **Obstacle.java** - 障碍物（可破坏，血量系统）
4. **Bullet.java** - 子弹飞行、碰撞
5. **Door.java** - 过关门
6. **GameBoard.java** - 64x64 网格管理，实体位置跟踪，碰撞检测
7. **GameEngine.java** - 30 FPS 游戏循环，状态管理

**游戏引擎功能**:
- 30 FPS 定时器
- 游戏逻辑更新和渲染频率分离
- 速度公式系统: y = -10ln(x-1) + 70
- 冲刺功能: 按住 Shift 时速度为 33ms
- 时间倒计时 (3 分钟)
- 游戏状态管理 (READY, RUNNING, PAUSED, GAME_OVER, LEVEL_COMPLETE)

#### Phase A: 游戏优化
1. ✅ 修复 `/help` 命令无响应问题
2. ✅ 实现速度公式系统 y = -10ln(x-1) + 70
3. ✅ 添加 `/snake speed info` 命令
4. ✅ 修改过关要求为增量模式（每关 +10, +15, +20...）
5. ✅ 修改障碍物密度公式（第 n 关 = n%）
6. ✅ 确认生成顺序正确（先放蛇，再放障碍物）
7. ✅ 添加进度条到顶部独立区域
8. ✅ 添加游戏状态覆盖层（Game Over/Level Complete）
9. ✅ Console 只显示系统级别信息，不显示实时游戏信息
10. ✅ 添加 Shift 冲刺功能

### 待完成的工作 🔄

#### Phase 3: 数据记录与分析（下一步）
- [ ] 集成 GameDatabase 到游戏循环
- [ ] 每帧记录关键操作
- [ ] 每 30 秒触发 AI 提问
- [ ] 每关结束后分析 MBTI

#### Phase 4: AI 关卡生成
- [ ] 实现关卡生成器
- [ ] 根据 MBTI 调整难度
- [ ] GPT-4o API 集成

#### Phase 5: 打包部署
- [ ] 创建启动脚本
- [ ] 打包 Python runtime
- [ ] 测试 macOS 和 Windows 兼容性

---

## 依赖项

**Java** (JDK 8+):
- `gson-2.10.1.jar` - JSON 序列化
- `sqlite-jdbc-3.44.1.0.jar` - SQLite JDBC 驱动

**Python** (3.8+):
- 仅标准库 (socket, json, sqlite3)
- 可选: PyTorch, NumPy（用于未来的 ML 功能）

---

## 关键设计决策

- **Socket vs HTTP**: 选择 Socket 以获得实时游戏循环中的更低延迟
- **动态端口分配**: 避免端口冲突（尝试 50705-50714）
- **双数据通道**: Socket 用于实时消息；SQLite 用于持久化分析
- **进程管理**: Java 自动启动/停止 Python 子进程
- **异步通信**: `CompletableFuture` 防止游戏循环阻塞
- **错误处理**: Python 崩溃导致 Java 退出（可在 `PythonProcessManager` 中配置）
- **UI 风格**: ASCII 黑底白字，使用 Unicode 边框字符（║, ═）进行分隔
- **速度控制**: 基于对数公式的速度等级系统，支持冲刺模式

---

## 重要说明

- Python 服务将端口号写入 `data/ai_port.txt` - Java 读取此文件以连接
- 数据库必须在首次运行前通过 `init_database.py` 初始化
- Java 依赖 (`lib/*.jar`) 必须手动添加到 IntelliJ 项目
- 跨平台兼容性: 自动检测 `python3` (macOS/Linux) vs `python` (Windows)
- 消息格式: 带有 `type` 字段的 JSON (PING, PONG, GAME_STATE, REQUEST_ANALYSIS, ANALYSIS_RESULT)
- **不要修改 package 名！** 应该是 `com.xylonet.snake.*`，不包含 `main.java.`
- **所有颜色都用白色主题**，不要用绿色
- **食物是 ◉，子弹是 @**

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
│   │   ├── ConsolePanel.java        # ✅ 控制台面板
│   │   └── ProgressBarPanel.java    # ✅ 进度条面板
│   ├── game/
│   │   ├── GameBoard.java           # ✅ 游戏板
│   │   ├── GameEngine.java          # ✅ 游戏引擎
│   │   ├── Snake.java               # ✅ 蛇
│   │   ├── Food.java                # ✅ 食物
│   │   ├── Obstacle.java            # ✅ 障碍物
│   │   ├── Bullet.java              # ✅ 子弹
│   │   └── Door.java                # ✅ 过关门
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
├── docs/                            # 项目文档
│   ├── QUICKSTART.md
│   ├── INTELLIJ_COMPLETE_SETUP.md
│   ├── JAVA_DEPENDENCIES.md
│   ├── JAVA_PYTHON_INTEGRATION.md
│   ├── PROJECT_STRUCTURE.txt
│   └── PYTHON_SETUP_INTELLIJ.md
│
├── out/                             # 编译输出
├── .idea/                           # IntelliJ 配置
├── Xylonet_Snake.iml                # ✅ 已修复
├── CLAUDE.md                        # ✅ 本文档
└── README.md                        # 项目说明
```

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

## 下次开发重点

根据当前进度，下一步应该实现 **Phase 3: 数据记录与分析**：

1. **集成 GameDatabase 到游戏循环**
   - 在游戏开始时创建新会话
   - 每帧记录关键操作（移动、射击、吃食物、死亡等）
   - 记录到 SQLite 数据库

2. **每 30 秒触发 AI 提问**
   - 在 Console 中显示 AI 的问题
   - 记录玩家的回答

3. **每关结束后分析 MBTI**
   - 调用 Python AI 服务
   - 获取 MBTI 类型和个性分析
   - 显示在 InfoPanel

这样可以为后续的 AI 关卡生成（Phase 4）提供数据支持。

---

## 联系人信息
- 开发者: lainos
- 项目路径: `/Users/lainos/Dropbox/Ptolemaeus Studio/Turner Sienter/Java projects/IntelliJ/Xylonet_Snake`
- Java SDK: Oracle OpenJDK 21.0.8
- Python: 3.13.5
