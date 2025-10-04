# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**重要**: 查看 `DEVELOPMENT_LOG.md` 了解完整的开发历史、设计需求和当前进度！

## Project Overview

Xylonet_Snake is a Snake game with AI-powered MBTI personality analysis. The project uses **Java (Swing UI)** for the game logic and **Python (Socket server)** for real-time behavioral analysis. Communication between Java and Python happens via Socket IPC, with SQLite as the shared data store.

## Architecture

### Java-Python Communication Flow

1. **Java Main** → `PythonProcessManager.startPythonService()` starts Python subprocess
2. **Python** `ai_service.py` binds to dynamic port (50705+), writes port to `data/ai_port.txt`
3. **Java** `AIClient` reads port file and connects via Socket
4. **Runtime**: Game writes player actions to SQLite; Python reads SQLite to perform MBTI analysis
5. **Shutdown**: Java stops Python process automatically

### Key Components

**Java** (`src/main/java/com/xylonet/snake/`)
- `network/PythonProcessManager.java` - Cross-platform Python process lifecycle management
- `network/AIClient.java` - Socket client with async messaging (PING, GAME_STATE, REQUEST_ANALYSIS)
- `data/GameDatabase.java` - SQLite operations (sessions, actions, snapshots, MBTI results)
- `Main.java` - Test harness demonstrating full integration

**Python** (`python_ai/`)
- `ai_service.py` - Socket server handling messages from Java
- `scripts/behavior_analyzer.py` - Calculates 4 dimension scores (aggression, caution, exploration, planning) to infer MBTI
- `scripts/init_database.py` - Creates 7 SQLite tables (game_sessions, player_actions, shooting_events, food_collection, game_snapshots, mbti_analysis, player_stats)

### Database Schema

SQLite database at `data/snake_game.db` contains:
- `game_sessions` - Session metadata (start/end times, score, outcome)
- `player_actions` - Every move, direction change
- `shooting_events` - Projectile usage
- `food_collection` - Food pickup events
- `game_snapshots` - Periodic game state snapshots
- `mbti_analysis` - MBTI analysis results with confidence scores
- `player_stats` - Aggregated statistics

## Development Commands

### Initial Setup

```bash
# 1. Initialize database (run once)
python3 python_ai/scripts/init_database.py

# 2. Download Java dependencies to lib/ (if not already present)
curl -o lib/gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
curl -o lib/sqlite-jdbc-3.44.1.0.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar

# 3. Add JARs to IntelliJ: File → Project Structure → Modules → Dependencies → + → JARs or directories → select lib/
```

### Running the Project

**In IntelliJ**:
- Run `Main.java` → Choose option `5` for full integration test
- The test will: start Python service, connect via Socket, simulate game session, record data to SQLite, request MBTI analysis

**Manual Python Service** (for debugging):
```bash
python3 python_ai/ai_service.py
# Outputs: [AI服务] 启动成功，监听端口: 50705
```

### Testing Individual Components

**Test Python Socket Server**:
```bash
python3 python_ai/ai_service.py
# In another terminal:
nc localhost 50705
# Type: {"type":"PING"}
# Response: {"type":"PONG"}
```

**Test Database Initialization**:
```bash
python3 python_ai/scripts/init_database.py
sqlite3 data/snake_game.db ".tables"
# Should show: food_collection, game_sessions, game_snapshots, mbti_analysis, player_actions, player_stats, shooting_events
```

**Test Behavior Analyzer** (requires existing data):
```bash
# First run Main.java option 2 to generate test data
python3 -c "from python_ai.scripts.behavior_analyzer import analyze_from_database; print(analyze_from_database())"
```

## Project Status

✅ **Phase 1 (Complete)**: Java-Python communication framework
- Socket IPC with dynamic port allocation
- Python process lifecycle management
- SQLite data collection
- Basic MBTI heuristic analysis

🔄 **Phase 2 (Pending)**: Game implementation
- Snake movement, food generation, obstacles
- Shooting mechanics, collision detection
- Win/lose conditions

🔄 **Phase 3 (Pending)**: Swing UI with CLI-style aesthetic

🔄 **Phase 4 (Pending)**: Enhanced AI with PyTorch models

🔄 **Phase 5 (Pending)**: LLM integration for commentary

## Dependencies

**Java** (JDK 8+):
- `gson-2.10.1.jar` - JSON serialization
- `sqlite-jdbc-3.44.1.0.jar` - SQLite JDBC driver

**Python** (3.8+):
- Standard library only (socket, json, sqlite3)
- Optional: PyTorch, NumPy (for future ML features)

## Key Design Decisions

- **Socket vs HTTP**: Socket chosen for lower latency in real-time game loop
- **Dynamic Port Allocation**: Avoids port conflicts (tries 50705-50714)
- **Dual Data Channels**: Socket for real-time messages; SQLite for persistent analysis
- **Process Management**: Java automatically starts/stops Python subprocess
- **Async Communication**: `CompletableFuture` prevents game loop blocking
- **Error Handling**: Python crash causes Java to exit (configurable in `PythonProcessManager`)

## Important Notes

- Python service writes port number to `data/ai_port.txt` - Java reads this to connect
- Database must be initialized before first run via `init_database.py`
- Java dependencies (`lib/*.jar`) must be manually added to IntelliJ project
- Cross-platform compatibility: Auto-detects `python3` (macOS/Linux) vs `python` (Windows)
- Message format: JSON with `type` field (PING, PONG, GAME_STATE, REQUEST_ANALYSIS, ANALYSIS_RESULT)
