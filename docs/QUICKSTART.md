# 快速开始指南

## 🎯 5分钟运行Java-Python通信测试

### 步骤1: 安装Python依赖（可选，初期不需要PyTorch）

```bash
# 如果只测试通信，不需要安装PyTorch
# 如果需要PyTorch:
cd python_ai
pip3 install -r requirements.txt
```

### 步骤2: 初始化数据库

```bash
python3 python_ai/scripts/init_database.py
```

你应该看到:
```
数据库初始化完成: /path/to/data/snake_game.db
```

### 步骤3: 在IntelliJ中配置项目

#### 3.1 添加Java依赖库

**选项A: 手动下载JAR（快速测试）**

1. 创建lib目录：
```bash
mkdir lib
```

2. 下载Gson：
```bash
cd lib
curl -O https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
```

3. 下载SQLite JDBC：
```bash
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar
```

4. 在IntelliJ中添加库：
   - File → Project Structure → Modules
   - Dependencies标签 → + → JARs or directories
   - 选择 `lib/` 目录下的两个JAR文件
   - Apply → OK

**选项B: 使用Maven（推荐生产环境）**

创建 `pom.xml` 在项目根目录：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.xylonet</groupId>
    <artifactId>snake</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- Gson -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>

        <!-- SQLite JDBC -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.44.1.0</version>
        </dependency>
    </dependencies>
</project>
```

然后在IntelliJ中右键 `pom.xml` → Maven → Reload Project

### 步骤4: 运行测试程序

1. 在IntelliJ中打开 `src/main/java/com/xylonet/snake/Main.java`

2. 右键 → Run 'Main.main()'

3. 你应该看到：

```
=== Xylonet Snake - Java-Python 通信测试 ===

1. 初始化数据库...
数据库初始化完成: /path/to/data/snake_game.db

2. 启动Python AI服务...
[Java] 正在启动Python AI服务...
[PYTHON-OUT] [AI服务] 启动成功，监听端口: 50705
[Java] Python AI服务启动成功，端口: 50705

3. 连接到AI服务...
[AIClient] 连接到 localhost:50705
[AIClient] 连接成功

4. 连接数据库...
[DB] 数据库连接成功: /path/to/data/snake_game.db

✓ 所有组件初始化成功！

========================================
通信测试菜单:
1. 测试心跳检测
2. 模拟游戏会话并记录数据
3. 请求MBTI分析
4. 发送游戏状态
5. 运行完整测试流程
0. 退出
========================================

请选择操作 (0-5):
```

### 步骤5: 测试通信

输入 **5** 然后回车，运行完整测试流程。

你会看到：

```
=== 运行完整测试流程 ===

--- 测试心跳检测 ---
心跳结果: ✓ 成功

等待1秒...

--- 模拟游戏会话 ---
开始新会话: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
模拟玩家操作...
✓ 会话结束，已记录 8 次操作

等待1秒...

--- 发送游戏状态 ---
✓ 游戏状态已发送

等待1秒...

--- 请求MBTI分析 ---
当前总操作数: 8

=== 分析结果 ===
MBTI: ENTP (置信度: 0.45, 样本量: 8)
特质 - 攻击性: 0.50, 谨慎度: 0.48, 探索性: 0.62, 计划性: 0.50

✓ 完整测试流程结束
```

**注意**: 第一次运行由于数据量少，置信度会很低。运行几次"模拟游戏会话"后，分析会更准确。

### 步骤6: 多次测试以提高分析准确度

再次选择 **2** 几次来模拟更多游戏数据：

```
请选择操作 (0-5): 2
--- 模拟游戏会话 ---
开始新会话: yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy
...

请选择操作 (0-5): 2
--- 模拟游戏会话 ---
开始新会话: zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz
...
```

然后再次选择 **3** 请求分析，你会看到置信度提高：

```
请选择操作 (0-5): 3
--- 请求MBTI分析 ---
当前总操作数: 56

=== 分析结果 ===
MBTI: INTP (置信度: 0.68, 样本量: 56)
特质 - 攻击性: 0.52, 谨慎度: 0.63, 探索性: 0.71, 计划性: 0.58
```

### 步骤7: 查看数据库（可选）

```bash
sqlite3 data/snake_game.db

sqlite> SELECT COUNT(*) FROM player_actions;
56

sqlite> SELECT COUNT(*) FROM game_sessions;
7

sqlite> SELECT session_id, final_score, victory FROM game_sessions LIMIT 3;
xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx|150|0
yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy|150|0
zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz|150|0

sqlite> .exit
```

---

## ✅ 成功标志

如果你看到以下输出，说明一切正常：

1. ✅ Python AI服务启动成功
2. ✅ Socket连接成功
3. ✅ 心跳检测通过
4. ✅ 游戏数据成功写入SQLite
5. ✅ Python成功读取数据并分析
6. ✅ MBTI分析结果返回Java

---

## 🐛 常见问题排查

### Python命令找不到

**Mac/Linux**:
```bash
which python3
# 应该显示: /usr/bin/python3 或 /usr/local/bin/python3
```

如果没有，安装Python:
```bash
# macOS
brew install python3

# Ubuntu/Debian
sudo apt install python3
```

**Windows**:
- 确保Python已安装并添加到PATH
- 可能需要修改 `PythonProcessManager.java` 中的命令从 `python3` 改为 `python`

### 端口被占用

如果看到类似 "无法在 50705-50715 范围内找到可用端口"：

1. 检查是否有其他进程占用端口：
```bash
# macOS/Linux
lsof -i :50705

# Windows
netstat -ano | findstr :50705
```

2. 修改起始端口（在 `ai_service.py` 和 `PythonProcessManager` 中）

### SQLite数据库锁定

如果看到 "database is locked"：

1. 关闭所有打开数据库的进程
2. 删除锁文件：
```bash
rm data/snake_game.db-shm data/snake_game.db-wal
```

### Gson或SQLite JDBC找不到

确认JAR文件已正确添加到项目：
- IntelliJ: File → Project Structure → Modules → Dependencies
- 应该能看到 `gson-2.10.1.jar` 和 `sqlite-jdbc-3.44.1.0.jar`

---

## 🎮 下一步

现在你已经验证了Java-Python通信正常工作，可以开始：

1. **实现游戏主体**: 创建贪吃蛇游戏逻辑
2. **集成通信**: 在游戏循环中调用 `AIClient` 和 `GameDatabase`
3. **优化AI分析**: 改进MBTI分析算法
4. **添加PyTorch模型**: 实现更复杂的AI功能
5. **添加LLM**: 集成大语言模型做游戏解说

详细架构说明请查看 `JAVA_PYTHON_INTEGRATION.md`

---

## 📞 需要帮助？

- 架构文档: `JAVA_PYTHON_INTEGRATION.md`
- Java依赖: `JAVA_DEPENDENCIES.md`
- 项目说明: `CLAUDE.md`

祝你开发顺利！🚀
