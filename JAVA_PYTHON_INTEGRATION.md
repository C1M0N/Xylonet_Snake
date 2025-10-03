# Java-Python 集成架构说明

## 📋 目录

1. [架构概览](#架构概览)
2. [核心组件](#核心组件)
3. [通信流程](#通信流程)
4. [数据流](#数据流)
5. [快速开始](#快速开始)
6. [API文档](#api文档)

---

## 🏗️ 架构概览

本项目采用 **Socket IPC** 方式实现 Java 游戏主体和 Python AI 服务的实时双向通信。

```
┌─────────────────────────────────────────────────────────────┐
│                    Xylonet Snake 游戏                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐         Socket (JSON)         ┌─────────┐│
│  │   Java 游戏  │ ◄────────────────────────────► │ Python  ││
│  │   主程序     │                                │ AI服务  ││
│  │  (Swing UI)  │                                │(PyTorch)││
│  └──────────────┘                                └─────────┘│
│         │                                              │     │
│         ▼                                              ▼     │
│  ┌──────────────┐                              ┌─────────┐  │
│  │   SQLite DB  │ ◄────────────────────────────│ 行为    │  │
│  │  游戏数据    │      离线读取分析             │ 分析器  │  │
│  └──────────────┘                              └─────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 关键设计决策

- **端口动态分配**: Python从50705开始尝试绑定，避免端口冲突
- **双重数据通道**:
  - Socket用于实时通信（心跳、游戏状态、AI请求）
  - SQLite用于持久化数据和离线分析
- **进程自动管理**: Java启动时自动拉起Python进程，退出时自动清理

---

## 🔧 核心组件

### Java端

#### 1. `PythonProcessManager` - Python进程管理器
- **位置**: `src/main/java/com/xylonet/snake/network/PythonProcessManager.java`
- **职责**:
  - 启动Python AI服务进程
  - 监控进程状态
  - 读取动态分配的端口号
  - 关闭Python进程

**关键方法**:
```java
// 启动Python服务
boolean startPythonService()

// 获取AI服务端口
Integer getAiServicePort()

// 停止Python服务
void stopPythonService()
```

#### 2. `AIClient` - AI服务Socket客户端
- **位置**: `src/main/java/com/xylonet/snake/network/AIClient.java`
- **职责**:
  - 连接到Python Socket服务
  - 发送游戏状态
  - 请求MBTI分析
  - 心跳检测

**关键方法**:
```java
// 连接到AI服务
boolean connect(String host, int port)

// 发送心跳
boolean sendPing()

// 发送游戏状态（异步）
void sendGameState(Map<String, Object> gameState)

// 请求分析（返回Future）
CompletableFuture<AnalysisResult> requestAnalysis(int dataPoints)
```

#### 3. `GameDatabase` - 游戏数据库管理
- **位置**: `src/main/java/com/xylonet/snake/data/GameDatabase.java`
- **职责**:
  - 连接SQLite数据库
  - 记录游戏会话
  - 记录玩家操作、射击、食物收集等
  - 记录游戏状态快照

**关键方法**:
```java
// 开始新游戏会话
String startNewSession()

// 记录玩家操作
void recordAction(String actionType, String direction, int snakeLength, int posX, int posY)

// 记录射击事件
void recordShooting(int targetX, int targetY, boolean hit, int reactionTimeMs)

// 记录食物收集
void recordFoodCollection(String foodType, int foodX, int foodY, int distanceTraveled, int timeToCollectMs)

// 记录游戏状态快照
void recordSnapshot(int headX, int headY, int length, String direction, ...)

// 结束游戏会话
void endSession(int finalScore, int snakeLength, boolean victory, String deathReason)
```

### Python端

#### 1. `ai_service.py` - Socket服务主程序
- **位置**: `python_ai/ai_service.py`
- **职责**:
  - 动态分配端口并启动Socket服务器
  - 将端口号写入文件供Java读取
  - 处理来自Java的各种请求
  - 调用行为分析器

**消息类型**:
- `PING` - 心跳检测 → 返回 `PONG`
- `GAME_STATE` - 游戏状态更新 → 返回 `GAME_STATE_ACK`
- `REQUEST_ANALYSIS` - 请求MBTI分析 → 返回 `ANALYSIS_RESULT`

#### 2. `behavior_analyzer.py` - 玩家行为分析器
- **位置**: `python_ai/scripts/behavior_analyzer.py`
- **职责**:
  - 从SQLite读取玩家行为数据
  - 计算攻击性、谨慎度、探索性、计划性分数
  - 基于启发式算法推测MBTI类型
  - 计算分析置信度

**分析维度**:
```python
- aggression_score   # 攻击性 (射击频率、命中率)
- caution_score      # 谨慎度 (存活时间、准确率)
- exploration_score  # 探索性 (移动多样性、区域覆盖)
- planning_score     # 计划性 (路径效率、反应稳定性)
```

#### 3. `init_database.py` - 数据库初始化脚本
- **位置**: `python_ai/scripts/init_database.py`
- **职责**: 创建SQLite数据库表结构

**数据库表**:
- `game_sessions` - 游戏会话表
- `player_actions` - 玩家操作记录
- `shooting_events` - 射击事件记录
- `food_collection` - 食物收集记录
- `game_snapshots` - 游戏状态快照
- `mbti_analysis` - MBTI分析结果历史
- `player_stats` - 玩家特征统计

---

## 🔄 通信流程

### 启动流程

```
1. Java主程序启动
   ↓
2. PythonProcessManager.startPythonService()
   ↓
3. Python进程启动，绑定端口50705（或50706, 50707...）
   ↓
4. Python将端口号写入 data/ai_port.txt
   ↓
5. Java读取端口号
   ↓
6. AIClient.connect(localhost, port)
   ↓
7. Socket连接建立 ✓
   ↓
8. 发送心跳检测确认连接
```

### 实时通信流程

```
游戏循环 (30fps):
  ├─ 每帧更新游戏状态
  │
  ├─ 记录玩家操作到SQLite (同步)
  │
  ├─ (可选) 发送游戏状态到Python (异步)
  │    └─> AIClient.sendGameState() - 不阻塞游戏
  │
  └─ 每20秒触发:
       └─> AIClient.requestAnalysis() - 异步请求
            └─> Python读取SQLite分析数据
                 └─> 返回MBTI结果
                      └─> Java显示分析结果
```

### 消息格式示例

**Java → Python (请求分析)**:
```json
{
  "type": "REQUEST_ANALYSIS",
  "timestamp": 1735920000000,
  "data_points": 247
}
```

**Python → Java (分析结果)**:
```json
{
  "type": "ANALYSIS_RESULT",
  "timestamp": 1735920001234,
  "mbti": "ENTP",
  "confidence": 0.72,
  "sample_size": 247,
  "traits": {
    "aggression": 0.68,
    "caution": 0.45,
    "exploration": 0.83,
    "planning": 0.52
  }
}
```

---

## 💾 数据流

### 数据收集（Java → SQLite）

游戏中每次玩家操作都会被记录：

```java
// 玩家移动
database.recordAction("MOVE", "UP", snakeLength, headX, headY);

// 玩家射击
database.recordShooting(targetX, targetY, hit, reactionTime);

// 吃到食物
database.recordFoodCollection(foodType, foodX, foodY, distance, timeMs);

// 定期快照（每N帧）
database.recordSnapshot(headX, headY, length, direction, obstacles, ...);
```

### 数据分析（Python ← SQLite）

Python分析器读取数据并计算：

```python
analyzer = BehaviorAnalyzer("data/snake_game.db")
result = analyzer.analyze_player_behavior()

# result包含:
# - mbti: "ENTP"
# - confidence: 0.72
# - traits: {aggression, caution, exploration, planning}
```

---

## 🚀 快速开始

### 前置要求

1. **Java**: JDK 8+
2. **Python**: Python 3.8+
3. **Java依赖库**:
   - Gson (JSON处理)
   - SQLite JDBC Driver

详见 `JAVA_DEPENDENCIES.md`

### 步骤1: 下载依赖

#### 方式A: Maven (推荐)
创建 `pom.xml` (如需要可以提供完整配置)

#### 方式B: 手动下载JAR
```bash
mkdir lib
cd lib
# 下载 gson-2.10.1.jar
# 下载 sqlite-jdbc-3.44.1.0.jar
```

然后在IntelliJ中添加到Libraries。

### 步骤2: 初始化数据库

```bash
python3 python_ai/scripts/init_database.py
```

### 步骤3: 运行测试

在IntelliJ中运行 `Main.java`，或命令行：

```bash
# 编译
javac -cp "lib/*:src" -d out src/main/java/com/xylonet/snake/*.java src/main/java/com/xylonet/snake/*/*.java

# 运行
java -cp "lib/*:out" com.xylonet.snake.Main
```

### 步骤4: 测试通信

Main程序会提供交互菜单：

```
1. 测试心跳检测
2. 模拟游戏会话并记录数据
3. 请求MBTI分析
4. 发送游戏状态
5. 运行完整测试流程
0. 退出
```

选择 **5** 运行完整测试流程。

---

## 📚 API文档

### Java调用示例

#### 初始化

```java
// 1. 启动Python服务
PythonProcessManager pythonManager = new PythonProcessManager();
pythonManager.startPythonService();

// 2. 连接AI客户端
AIClient aiClient = new AIClient();
aiClient.connect("localhost", pythonManager.getAiServicePort());

// 3. 连接数据库
GameDatabase database = new GameDatabase();
database.connect();
```

#### 游戏循环中使用

```java
// 游戏开始
String sessionId = database.startNewSession();

// 游戏循环 (30fps)
while (gameRunning) {
    // 更新游戏逻辑...

    // 记录玩家操作
    if (playerMoved) {
        database.recordAction("MOVE", direction, snakeLength, headX, headY);
    }

    // 异步发送状态（不阻塞）
    if (frameCount % 30 == 0) {  // 每秒发送一次
        Map<String, Object> state = new HashMap<>();
        state.put("snake_length", snakeLength);
        state.put("score", score);
        aiClient.sendGameState(state);
    }

    // 每20秒请求分析
    if (frameCount % (30 * 20) == 0) {
        aiClient.requestAnalysis(database.getTotalActionCount())
            .thenAccept(result -> {
                System.out.println("您的MBTI: " + result.mbti);
                // 在UI中显示结果
            });
    }

    // 30fps
    Thread.sleep(33);
}

// 游戏结束
database.endSession(score, snakeLength, victory, deathReason);
```

#### 清理资源

```java
aiClient.disconnect();
database.close();
pythonManager.stopPythonService();
```

### Python扩展示例

如果你想在Python端添加PyTorch模型：

```python
# 在 ai_service.py 中添加
import torch

class SnakeAIService:
    def __init__(self, ...):
        # 加载PyTorch模型
        self.model = torch.load('python_ai/models/player_predictor.pth')
        self.model.eval()

    def analyze_game_state(self, data):
        # 使用PyTorch预测
        state_tensor = self.prepare_state(data)
        with torch.no_grad():
            prediction = self.model(state_tensor)

        return {
            'type': 'GAME_STATE_ACK',
            'prediction': prediction.tolist()
        }
```

---

## 🔍 调试技巧

### 查看Python输出

Java会自动打印Python的stdout和stderr：

```
[PYTHON-OUT] [AI服务] 启动成功，监听端口: 50705
[PYTHON-ERR] [AI服务] 分析失败: ...
```

### 检查端口文件

```bash
cat data/ai_port.txt
# 应该输出: 50705 (或其他端口)
```

### 查看数据库

```bash
sqlite3 data/snake_game.db
sqlite> SELECT COUNT(*) FROM player_actions;
sqlite> SELECT * FROM game_sessions;
```

### 常见问题

**Q: Python进程启动失败**
- 检查Python命令是否正确（`python` vs `python3`）
- 确保 `ai_service.py` 有执行权限

**Q: Socket连接超时**
- 检查防火墙设置
- 确认端口文件生成成功
- 查看Python输出是否有错误

**Q: 数据库锁定**
- SQLite同时只支持一个写入者
- Java和Python不要同时写入同一表

---

## 📦 后续扩展

### 添加PyTorch模型

1. 在 `python_ai/models/` 下放置模型文件
2. 在 `ai_service.py` 中加载模型
3. 在 `analyze_game_state()` 中使用模型预测

### 添加LLM调用

```python
# 在 ai_service.py 中
from openai import OpenAI

def generate_commentary(self, game_state):
    client = OpenAI()
    response = client.chat.completions.create(
        model="gpt-4",
        messages=[{"role": "user", "content": f"解说这局游戏: {game_state}"}]
    )
    return response.choices[0].message.content
```

### 打包成可执行文件

见后续的打包指南文档。

---

## 🎯 总结

核心优势：
- ✅ **解耦**: Java和Python独立开发和测试
- ✅ **实时**: Socket通信延迟低（<10ms）
- ✅ **可靠**: Python崩溃不影响游戏主体（可配置）
- ✅ **灵活**: 易于添加新的AI功能
- ✅ **可扩展**: 支持PyTorch、LLM等复杂模型

关键文件：
- `src/main/java/com/xylonet/snake/network/PythonProcessManager.java`
- `src/main/java/com/xylonet/snake/network/AIClient.java`
- `python_ai/ai_service.py`
- `python_ai/scripts/behavior_analyzer.py`

现在你已经有了一个完整的Java-Python通信框架，可以开始实现游戏主体了！
