# Xylonet Snake - ASCII Edition

一个带有 AI 行为分析的贪吃蛇游戏，采用复古 ASCII 风格界面。

## 快速开始

### 运行游戏
```bash
# 编译并运行
javac -d out -cp "lib/*" src/main/java/com/xylonet/snake/**/*.java
java -cp "out:lib/*" com.xylonet.snake.ui.MainWindow
```

### 游戏控制
- **WASD** - 移动蛇
- **方向键** - 射击
- **空格** - 开始/暂停游戏
- **T** - 打开控制台
- **ESC** - 从控制台返回游戏
- **P** - 暂停

### 控制台命令
- `/help` - 显示帮助
- `/snake speed [毫秒]` - 调整蛇的速度（不带参数则重置）
- `/clear` - 清空控制台
- `/history` - 查看命令历史

## 项目结构
```
Xylonet_Snake/
├── src/main/java/      # Java 源代码
├── python_ai/          # Python AI 分析
├── lib/                # Java 依赖库
├── data/               # 数据库文件
├── docs/               # 项目文档
│   └── claude-context/ # Claude Code 上下文文件
└── out/                # 编译输出
```

## 文档

- 📖 [项目概述](docs/claude-context/CLAUDE.md) - 架构、命令、设计决策
- 📝 [开发日志](docs/claude-context/DEVELOPMENT_LOG.md) - 完整开发历史
- 🚀 [快速入门](docs/QUICKSTART.md) - 详细安装和运行指南
- 🔧 [IntelliJ 设置](docs/INTELLIJ_COMPLETE_SETUP.md) - IDE 配置指南

## 技术栈

- **Java** - 游戏逻辑和 Swing UI
- **Python** - AI 行为分析（Socket 服务）
- **SQLite** - 数据持久化
- **ASCII 风格** - 复古终端美学

## 开发状态

- ✅ Phase 1: UI 框架
- ✅ Phase 2: 游戏主体实现
- 🔄 Phase 3: AI 分析集成
- 🔄 Phase 4: 关卡生成系统

## 许可证

MIT License
