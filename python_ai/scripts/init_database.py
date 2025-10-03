#!/usr/bin/env python3
"""
初始化SQLite数据库结构
"""

import sqlite3
from pathlib import Path

def init_database(db_path):
    """初始化数据库表结构"""
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # 1. 游戏会话表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS game_sessions (
            session_id TEXT PRIMARY KEY,
            start_time INTEGER NOT NULL,
            end_time INTEGER,
            final_score INTEGER,
            snake_length INTEGER,
            victory BOOLEAN,
            death_reason TEXT,
            duration_seconds INTEGER
        )
    ''')

    # 2. 玩家操作记录表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS player_actions (
            action_id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            action_type TEXT NOT NULL,
            direction TEXT,
            snake_length INTEGER,
            position_x INTEGER,
            position_y INTEGER,
            FOREIGN KEY (session_id) REFERENCES game_sessions(session_id)
        )
    ''')

    # 3. 射击记录表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS shooting_events (
            shoot_id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            target_x INTEGER NOT NULL,
            target_y INTEGER NOT NULL,
            hit BOOLEAN,
            reaction_time_ms INTEGER,
            FOREIGN KEY (session_id) REFERENCES game_sessions(session_id)
        )
    ''')

    # 4. 食物收集记录表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS food_collection (
            collect_id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            food_type TEXT,
            food_x INTEGER,
            food_y INTEGER,
            distance_traveled INTEGER,
            time_to_collect_ms INTEGER,
            FOREIGN KEY (session_id) REFERENCES game_sessions(session_id)
        )
    ''')

    # 5. 游戏状态快照表（每隔N帧记录）
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS game_snapshots (
            snapshot_id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            snake_head_x INTEGER,
            snake_head_y INTEGER,
            snake_length INTEGER,
            snake_direction TEXT,
            nearby_obstacles_count INTEGER,
            distance_to_food REAL,
            health INTEGER,
            attack_power INTEGER,
            defense_power INTEGER,
            FOREIGN KEY (session_id) REFERENCES game_sessions(session_id)
        )
    ''')

    # 6. MBTI分析结果表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS mbti_analysis (
            analysis_id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp INTEGER NOT NULL,
            total_sessions INTEGER,
            total_actions INTEGER,
            mbti_type TEXT,
            confidence REAL,
            aggression_score REAL,
            caution_score REAL,
            exploration_score REAL,
            planning_score REAL,
            notes TEXT
        )
    ''')

    # 7. 玩家特征统计表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS player_stats (
            stat_id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id TEXT NOT NULL,
            avg_reaction_time_ms REAL,
            movement_pattern TEXT,
            risk_taking_score REAL,
            efficiency_score REAL,
            aggression_level REAL,
            FOREIGN KEY (session_id) REFERENCES game_sessions(session_id)
        )
    ''')

    # 创建索引以提高查询性能
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_actions_session ON player_actions(session_id)')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_actions_timestamp ON player_actions(timestamp)')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_shooting_session ON shooting_events(session_id)')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_food_session ON food_collection(session_id)')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_snapshots_session ON game_snapshots(session_id)')

    conn.commit()
    conn.close()

    print(f"数据库初始化完成: {db_path}")

if __name__ == '__main__':
    # 数据库路径
    project_root = Path(__file__).parent.parent.parent
    db_path = project_root / 'data' / 'snake_game.db'
    db_path.parent.mkdir(parents=True, exist_ok=True)

    init_database(db_path)
