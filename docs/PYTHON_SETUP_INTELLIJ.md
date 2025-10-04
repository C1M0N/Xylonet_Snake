# IntelliJ Python配置指南

## 问题：Python文件显示错误

即使你安装了Python插件，如果看到Python文件全是error，这是因为IntelliJ没有配置Python SDK。

## 解决方法（按步骤操作）

### 步骤1: 确认Python插件已安装

1. 打开 `IntelliJ IDEA` → `Settings` (Mac: `⌘,`)
2. 左侧选择 `Plugins`
3. 搜索 "Python"
4. 确认 **"Python"** 或 **"Python Community Edition"** 已安装并启用
5. 如果刚安装，**重启IntelliJ**

### 步骤2: 添加Python SDK

1. 打开 `File` → `Project Structure` (Mac: `⌘;`)
2. 左侧选择 `SDKs`
3. 点击上方的 `+` 按钮
4. 选择 `Add Python SDK...`
5. 在弹出窗口中：
   - 选择 `System Interpreter`
   - 点击 `...` 浏览按钮
   - 导航到: `/Library/Frameworks/Python.framework/Versions/3.13/bin/python3`
   - 或者在路径框中直接粘贴上面的路径
6. 点击 `OK`
7. SDK名称会自动设置为 "Python 3.13"

### 步骤3: 配置项目使用Python SDK

#### 方法A: 配置整个项目（推荐）

1. 仍在 `Project Structure` 窗口中
2. 左侧选择 `Project`
3. 在右侧的 `SDK` 下拉菜单中：
   - 你会看到 "Python 3.13" 选项
   - 选择它
4. 点击 `Apply` → `OK`

#### 方法B: 只配置Python模块（备选）

1. 在 `Project Structure` 窗口中
2. 左侧选择 `Modules`
3. 点击 `+` → `New Module`
4. 选择 `Python`
5. Content root 设置为项目的 `python_ai` 目录
6. Module SDK 选择 "Python 3.13"
7. 点击 `Finish`

### 步骤4: 验证配置

1. 打开任意Python文件（如 `python_ai/ai_service.py`）
2. 右下角应该显示 "Python 3.13" 而不是错误提示
3. Python代码应该有语法高亮
4. import语句不应该显示红色波浪线

### 步骤5: 如果还有问题

#### 5.1 使标记目录为Python源码根

1. 在项目视图中右键点击 `python_ai` 文件夹
2. 选择 `Mark Directory as` → `Sources Root`

#### 5.2 配置Python facet

1. `File` → `Project Structure` → `Facets`
2. 点击 `+` → `Python`
3. 选择项目模块
4. 在 `Interpreter` 中选择 "Python 3.13"
5. Apply → OK

### 步骤6: 重新索引项目

1. `File` → `Invalidate Caches...`
2. 选择 `Invalidate and Restart`
3. 等待IntelliJ重启和重新索引

---

## 快速命令行验证（确认Python能运行）

```bash
# 1. 确认Python已安装
which python3
# 输出: /Library/Frameworks/Python.framework/Versions/3.13/bin/python3

python3 --version
# 输出: Python 3.13.5

# 2. 测试Python脚本能否运行
cd "/Users/lainos/Dropbox/Ptolemaeus Studio/Turner Sienter/Java projects/IntelliJ/Xylonet_Snake"
python3 python_ai/scripts/init_database.py
# 应该输出: 数据库初始化完成: .../data/snake_game.db

# 3. 测试AI服务能否启动
python3 python_ai/ai_service.py
# 应该输出: [AI服务] 启动成功，监听端口: 50705
# 按 Ctrl+C 停止
```

如果命令行测试都通过，说明Python环境没问题，只是IntelliJ配置问题。

---

## 重要提示

### ⚠️ 即使IntelliJ显示错误，Java程序也能正常运行！

因为：
- Java通过 `ProcessBuilder` 直接调用系统的 `python3` 命令
- 不依赖IntelliJ的Python配置
- 只要命令行能运行Python，Java就能启动Python服务

### 你可以选择：

**选项1: 配置IntelliJ（推荐）**
- 优点：可以在IntelliJ中编辑Python代码，有代码补全和错误检查
- 按上面步骤配置

**选项2: 忽略错误继续开发**
- 如果主要开发Java部分
- Python文件只是外部服务
- 可以用其他编辑器（VS Code/PyCharm）编辑Python代码
- Java程序仍然能正常运行

---

## 推荐的工作流程

如果你主要在IntelliJ开发Java：

1. **IntelliJ**: 开发Java游戏主体
2. **命令行/VS Code**: 编辑Python AI代码
3. **IntelliJ运行Main.java**: 测试整个系统

这样可以避免IntelliJ配置的复杂性。

---

## 故障排查

### Q: 步骤2找不到"Add Python SDK"选项
**A**: Python插件没有正确安装或启用
- 重新检查Plugins
- 重启IntelliJ
- 确认插件版本兼容

### Q: SDK列表中看不到Python 3.13
**A**: 浏览到Python可执行文件
- Mac路径: `/Library/Frameworks/Python.framework/Versions/3.13/bin/python3`
- 或运行 `which python3` 找到路径

### Q: 配置后还是显示错误
**A**:
1. 检查右下角是否显示"Python 3.13"
2. 尝试 `File` → `Invalidate Caches` → Restart
3. 关闭所有Python文件，重新打开

### Q: 不想配置IntelliJ，能直接运行吗？
**A**: 能！
- 只要命令行 `python3 python_ai/ai_service.py` 能运行
- Java的Main.java就能正常启动整个系统
- IntelliJ的错误提示可以忽略

---

## 当前你的系统状态

✅ Python已安装: `/Library/Frameworks/Python.framework/Versions/3.13/bin/python3`
✅ Python版本: `3.13.5`
✅ Python脚本能运行: 数据库初始化成功
❓ IntelliJ配置: 需要按上面步骤配置

现在你可以：
1. 按步骤1-4配置IntelliJ获得完整IDE支持
2. 或者直接忽略错误，运行Main.java测试系统

两种方式都能让项目正常工作！
