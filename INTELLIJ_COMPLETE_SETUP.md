# IntelliJ 完整配置指南（Java + Python）

## 当前状态

✅ Java 21 已安装: `/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`
✅ Python 3.13.5 已安装: `/Library/Frameworks/Python.framework/Versions/3.13/bin/python3`
✅ 配置文件已创建

## 问题解决

我已经为你创建了基础配置文件。现在需要在IntelliJ中手动确认配置。

---

## 步骤1: 重启IntelliJ（重要！）

配置文件更新后必须重启IntelliJ：

1. 关闭IntelliJ IDEA
2. 重新打开项目

---

## 步骤2: 配置Java SDK

### 2.1 检查SDK是否已自动识别

重启后，IntelliJ应该会自动检测到Java 21。检查方法：

1. 打开 `File` → `Project Structure` (快捷键: `⌘;`)
2. 左侧选择 `Project`
3. 查看 `SDK` 字段：
   - ✅ 如果显示 "21" 或 "Oracle OpenJDK version 21"，说明已配置好
   - ❌ 如果显示 "No SDK" 或红色错误，继续下一步

### 2.2 手动添加Java SDK（如果需要）

如果SDK未自动识别：

1. 在 `Project Structure` → `SDKs` 页面
2. 点击 `+` → `Add JDK...`
3. 浏览到: `/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`
4. 点击 `Open`
5. SDK名称会自动设置为 "21"
6. 返回 `Project` 页面，`SDK` 下拉菜单选择 "21"
7. `Language level` 选择 "21 - ..."
8. 点击 `Apply`

---

## 步骤3: 配置Python SDK

### 3.1 确认Python插件已安装

1. `IntelliJ IDEA` → `Settings` → `Plugins`
2. 搜索 "Python"
3. 确认 **"Python"** 插件已安装并启用
4. 如果刚安装，重启IntelliJ

### 3.2 添加Python SDK

1. `File` → `Project Structure` → `SDKs`
2. 点击 `+` → `Add Python SDK...`
3. 选择 `System Interpreter`
4. 路径填写或浏览到:
   ```
   /Library/Frameworks/Python.framework/Versions/3.13/bin/python3
   ```
5. 点击 `OK`
6. SDK名称会显示为 "Python 3.13"

---

## 步骤4: 配置模块

### 4.1 检查Java源码文件夹

1. `File` → `Project Structure` → `Modules`
2. 展开 `Xylonet_Snake` 模块
3. 在 `Sources` 标签页中，确认：
   - ✅ `src/main/java` 标记为 **Sources** (蓝色图标)
   - ✅ `python_ai` 标记为 **Sources** (蓝色图标)
   - ✅ `out` 标记为 **Excluded** (红色图标)
   - ✅ `data` 标记为 **Excluded** (红色图标)

如果不正确，右键相应文件夹手动标记：
- `src/main/java` → `Mark as: Sources`
- `python_ai` → `Mark as: Sources`
- `out` → `Mark as: Excluded`

### 4.2 配置模块依赖（Java库）

1. 仍在 `Modules` 页面
2. 切换到 `Dependencies` 标签页
3. 点击 `+` → `JARs or directories...`
4. 选择 `lib/` 目录（如果你已下载JAR文件）
5. 或者手动添加：
   - `lib/gson-2.10.1.jar`
   - `lib/sqlite-jdbc-3.44.1.0.jar`
6. 点击 `Apply`

---

## 步骤5: 验证配置

### 5.1 检查Java文件

1. 打开 `src/main/java/com/xylonet/snake/Main.java`
2. 应该看到：
   - ✅ 代码有语法高亮
   - ✅ import语句没有红色波浪线
   - ✅ 右下角显示 "21"（Java版本）

### 5.2 检查Python文件

1. 打开 `python_ai/ai_service.py`
2. 应该看到：
   - ✅ 代码有语法高亮
   - ✅ import语句没有红色波浪线
   - ✅ 右下角显示 "Python 3.13"

---

## 步骤6: 下载Java依赖库（如果还没有）

项目需要两个JAR文件：

### 方法A: 命令行下载（快速）

```bash
# 创建lib目录
mkdir -p lib
cd lib

# 下载Gson
curl -O https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

# 下载SQLite JDBC
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar

# 返回项目根目录
cd ..
```

下载完成后：
1. `File` → `Project Structure` → `Modules` → `Dependencies`
2. `+` → `JARs or directories...`
3. 选择 `lib` 目录
4. Apply → OK

### 方法B: 手动下载

1. 访问 https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/
   - 下载 `gson-2.10.1.jar`
2. 访问 https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/
   - 下载 `sqlite-jdbc-3.44.1.0.jar`
3. 将两个JAR文件放入项目的 `lib/` 目录
4. 按方法A的最后步骤添加到项目

---

## 步骤7: 刷新项目（重要！）

配置完成后，刷新项目：

1. `File` → `Invalidate Caches...`
2. 勾选：
   - ✅ Invalidate and Restart
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
3. 点击 `Invalidate and Restart`
4. 等待IntelliJ重启并重新索引项目

---

## 步骤8: 测试运行

### 8.1 初始化数据库（首次运行）

在IntelliJ底部工具栏打开 `Terminal`，运行：

```bash
python3 python_ai/scripts/init_database.py
```

应该看到：
```
数据库初始化完成: .../data/snake_game.db
```

### 8.2 运行Main.java

1. 打开 `src/main/java/com/xylonet/snake/Main.java`
2. 右键 → `Run 'Main.main()'`
3. 应该看到：

```
=== Xylonet Snake - Java-Python 通信测试 ===

1. 初始化数据库...
数据库初始化完成: ...

2. 启动Python AI服务...
[Java] 正在启动Python AI服务...
[PYTHON-OUT] [AI服务] 启动成功，监听端口: 50705
[Java] Python AI服务启动成功，端口: 50705

3. 连接到AI服务...
[AIClient] 连接到 localhost:50705
[AIClient] 连接成功

4. 连接数据库...
[DB] 数据库连接成功: ...

✓ 所有组件初始化成功！
```

4. 在交互菜单中输入 `5` 运行完整测试

---

## 常见问题排查

### Q1: Java代码还是显示错误

**检查清单**：
1. ✅ `Project Structure` → `Project` → `SDK` 是否选择了 "21"
2. ✅ `Project Structure` → `Modules` → `Sources` 中 `src/main/java` 是否标记为 Sources
3. ✅ 是否重启了IntelliJ
4. ✅ 是否运行了 `Invalidate Caches`

**解决**：
```
File → Invalidate Caches → Invalidate and Restart
```

### Q2: Python代码还是显示错误

**检查清单**：
1. ✅ `Settings` → `Plugins` 中Python插件是否启用
2. ✅ `Project Structure` → `SDKs` 是否添加了 Python 3.13
3. ✅ 右下角是否显示 "Python 3.13"

**解决**：
- 确认插件安装后重启了IntelliJ
- 手动添加Python SDK（步骤3）

### Q3: 找不到Gson或SQLite类

**原因**: 依赖库未添加

**解决**：
1. 确认 `lib/` 目录下有两个JAR文件
2. `Project Structure` → `Modules` → `Dependencies`
3. 检查是否有 `gson-2.10.1.jar` 和 `sqlite-jdbc-3.44.1.0.jar`
4. 如果没有，按步骤6重新添加

### Q4: 运行Main.java时提示找不到Python

**原因**: `PythonProcessManager` 找不到python3命令

**解决**：
- Mac: 确认命令行运行 `which python3` 有输出
- 如果python3不在PATH中，修改 `PythonProcessManager.java` 第42行为完整路径：
  ```java
  return "/Library/Frameworks/Python.framework/Versions/3.13/bin/python3";
  ```

---

## 项目结构确认

配置完成后，项目视图应该是这样的：

```
Xylonet_Snake
├── 📁 .idea/                  [灰色，配置文件]
├── 📁 data/                   [红色，排除]
├── 📁 lib/                    [包含JAR文件]
│   ├── gson-2.10.1.jar
│   └── sqlite-jdbc-3.44.1.0.jar
├── 📁 out/                    [红色，排除]
├── 📁 python_ai/              [蓝色，源码]
│   ├── 📄 ai_service.py
│   ├── 📁 scripts/
│   │   ├── init_database.py
│   │   └── behavior_analyzer.py
│   └── 📁 models/
├── 📁 src/                    [蓝色，源码]
│   └── 📁 main/java/com/xylonet/snake/
│       ├── Main.java
│       ├── 📁 data/
│       ├── 📁 game/
│       ├── 📁 network/
│       └── 📁 ui/
└── 📄 Xylonet_Snake.iml
```

图标颜色说明：
- 🔵 蓝色文件夹 = 源码根目录
- 🔴 红色文件夹 = 排除目录
- 灰色文件夹 = 配置目录

---

## 成功标志

当配置完全正确时：

✅ Java文件没有红色波浪线错误
✅ Python文件没有红色波浪线错误
✅ 右下角可以看到 "21" (Java)
✅ 打开Python文件时右下角显示 "Python 3.13"
✅ 运行Main.java成功启动Python服务
✅ 测试菜单所有选项都能正常工作

---

## 快速诊断命令

在IntelliJ的Terminal中运行：

```bash
# 检查Java
java -version
# 应输出: java version "21.0.8"

# 检查Python
python3 --version
# 应输出: Python 3.13.5

# 检查项目文件
ls -la lib/
# 应看到两个JAR文件

ls -la data/
# 应看到 snake_game.db（运行过init_database.py后）

# 测试Python服务
python3 python_ai/ai_service.py
# 应输出: [AI服务] 启动成功，监听端口: 50705
# 按Ctrl+C停止
```

---

## 仍有问题？

如果按照所有步骤操作后还有问题：

1. **截图**：
   - `Project Structure` → `Project` 页面
   - `Project Structure` → `Modules` → `Sources` 页面
   - `Project Structure` → `Modules` → `Dependencies` 页面
   - 错误提示的详细信息

2. **检查日志**：
   - `Help` → `Show Log in Finder`
   - 查看 `idea.log` 中的错误信息

3. **完全重置**（最后手段）：
   ```bash
   # 备份代码
   cp -r src src_backup
   cp -r python_ai python_ai_backup

   # 删除IntelliJ配置
   rm -rf .idea/
   rm Xylonet_Snake.iml

   # 重新打开项目，IntelliJ会重新检测
   ```

记住：即使IDE有警告，只要命令行能运行，程序就能工作！
