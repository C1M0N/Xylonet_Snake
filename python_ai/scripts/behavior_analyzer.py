#!/usr/bin/env python3
"""
玩家行为分析器
基于SQLite数据分析玩家游戏风格并推测MBTI
"""

import sqlite3
from pathlib import Path
from typing import Dict, Tuple
import math

class BehaviorAnalyzer:
    """玩家行为分析器"""

    def __init__(self, db_path: str):
        self.db_path = db_path
        self.conn = None

    def connect(self):
        """连接数据库"""
        self.conn = sqlite3.connect(self.db_path)
        self.conn.row_factory = sqlite3.Row

    def close(self):
        """关闭数据库连接"""
        if self.conn:
            self.conn.close()

    def analyze_player_behavior(self) -> Dict:
        """分析玩家行为并返回MBTI推测"""
        if not self.conn:
            self.connect()

        # 收集各种统计数据
        stats = {
            'total_sessions': self._get_total_sessions(),
            'total_actions': self._get_total_actions(),
            'avg_reaction_time': self._get_avg_reaction_time(),
            'aggression_score': self._calculate_aggression(),
            'caution_score': self._calculate_caution(),
            'exploration_score': self._calculate_exploration(),
            'planning_score': self._calculate_planning()
        }

        # 基于统计数据推测MBTI
        mbti, confidence = self._infer_mbti(stats)

        return {
            'mbti': mbti,
            'confidence': confidence,
            'total_sessions': stats['total_sessions'],
            'total_actions': stats['total_actions'],
            'traits': {
                'aggression': stats['aggression_score'],
                'caution': stats['caution_score'],
                'exploration': stats['exploration_score'],
                'planning': stats['planning_score']
            },
            'avg_reaction_time_ms': stats['avg_reaction_time']
        }

    def _get_total_sessions(self) -> int:
        """获取总游戏会话数"""
        cursor = self.conn.execute("SELECT COUNT(*) as count FROM game_sessions")
        row = cursor.fetchone()
        return row['count'] if row else 0

    def _get_total_actions(self) -> int:
        """获取总操作数"""
        cursor = self.conn.execute("SELECT COUNT(*) as count FROM player_actions")
        row = cursor.fetchone()
        return row['count'] if row else 0

    def _get_avg_reaction_time(self) -> float:
        """计算平均反应时间"""
        cursor = self.conn.execute(
            "SELECT AVG(reaction_time_ms) as avg_time FROM shooting_events WHERE reaction_time_ms > 0"
        )
        row = cursor.fetchone()
        return row['avg_time'] if row and row['avg_time'] else 0.0

    def _calculate_aggression(self) -> float:
        """
        计算攻击性分数 (0-1)
        基于: 射击频率、主动射击比例、快速决策
        """
        total_actions = self._get_total_actions()
        if total_actions == 0:
            return 0.5  # 默认中等

        # 射击次数占总操作的比例
        cursor = self.conn.execute("SELECT COUNT(*) as count FROM shooting_events")
        shoot_count = cursor.fetchone()['count']
        shoot_ratio = shoot_count / total_actions if total_actions > 0 else 0

        # 射击命中率（高命中率可能表示谨慎，低命中率表示激进）
        cursor = self.conn.execute(
            "SELECT AVG(CASE WHEN hit THEN 1 ELSE 0 END) as hit_rate FROM shooting_events"
        )
        row = cursor.fetchone()
        hit_rate = row['hit_rate'] if row and row['hit_rate'] else 0.5

        # 综合计算：高射击率 + 不太在乎命中率 = 高攻击性
        aggression = (shoot_ratio * 0.7) + ((1 - hit_rate) * 0.3)

        return min(max(aggression, 0.0), 1.0)

    def _calculate_caution(self) -> float:
        """
        计算谨慎度分数 (0-1)
        基于: 平均存活时间、避免危险的行为、失败后的调整
        """
        # 平均游戏时长（存活越久越谨慎）
        cursor = self.conn.execute(
            "SELECT AVG(duration_seconds) as avg_duration FROM game_sessions WHERE duration_seconds > 0"
        )
        row = cursor.fetchone()
        avg_duration = row['avg_duration'] if row and row['avg_duration'] else 0

        # 归一化到0-1（假设300秒为满分）
        duration_score = min(avg_duration / 300.0, 1.0) if avg_duration else 0.5

        # 射击准确率（高准确率表示谨慎）
        cursor = self.conn.execute(
            "SELECT AVG(CASE WHEN hit THEN 1 ELSE 0 END) as hit_rate FROM shooting_events"
        )
        row = cursor.fetchone()
        hit_rate = row['hit_rate'] if row and row['hit_rate'] else 0.5

        # 综合计算
        caution = (duration_score * 0.6) + (hit_rate * 0.4)

        return min(max(caution, 0.0), 1.0)

    def _calculate_exploration(self) -> float:
        """
        计算探索性分数 (0-1)
        基于: 移动多样性、访问地图区域的广度
        """
        # 获取所有移动记录
        cursor = self.conn.execute(
            "SELECT direction FROM player_actions WHERE action_type = 'MOVE' ORDER BY timestamp"
        )
        directions = [row['direction'] for row in cursor.fetchall()]

        if len(directions) < 10:
            return 0.5  # 数据不足

        # 计算方向变化频率（频繁改变方向 = 高探索性）
        direction_changes = 0
        for i in range(1, len(directions)):
            if directions[i] != directions[i-1]:
                direction_changes += 1

        change_ratio = direction_changes / len(directions)

        # 获取访问过的不同位置数量（通过快照）
        cursor = self.conn.execute(
            "SELECT COUNT(DISTINCT snake_head_x || ',' || snake_head_y) as unique_positions FROM game_snapshots"
        )
        row = cursor.fetchone()
        unique_positions = row['unique_positions'] if row else 0

        # 归一化（假设100个不同位置为满分）
        position_score = min(unique_positions / 100.0, 1.0) if unique_positions else 0.5

        # 综合计算
        exploration = (change_ratio * 0.5) + (position_score * 0.5)

        return min(max(exploration, 0.0), 1.0)

    def _calculate_planning(self) -> float:
        """
        计算计划性分数 (0-1)
        基于: 路径效率、食物收集效率、反应时间稳定性
        """
        # 食物收集的平均路径效率
        cursor = self.conn.execute(
            "SELECT AVG(time_to_collect_ms) as avg_time, AVG(distance_traveled) as avg_distance FROM food_collection"
        )
        row = cursor.fetchone()

        if row and row['avg_time'] and row['avg_distance']:
            # 时间越短、路径越短 = 计划性越强
            efficiency = 1.0 - min((row['avg_time'] / 10000.0), 1.0)  # 归一化
        else:
            efficiency = 0.5

        # 反应时间的标准差（稳定性）
        cursor = self.conn.execute(
            "SELECT AVG(reaction_time_ms) as avg_rt, "
            "(SELECT AVG((reaction_time_ms - avg_rt) * (reaction_time_ms - avg_rt)) FROM shooting_events) as variance "
            "FROM shooting_events"
        )
        row = cursor.fetchone()

        if row and row['variance']:
            std_dev = math.sqrt(row['variance'])
            consistency = 1.0 - min(std_dev / 500.0, 1.0)  # 归一化
        else:
            consistency = 0.5

        # 综合计算
        planning = (efficiency * 0.6) + (consistency * 0.4)

        return min(max(planning, 0.0), 1.0)

    def _infer_mbti(self, stats: Dict) -> Tuple[str, float]:
        """
        基于统计数据推测MBTI类型
        这是一个简化的启发式算法
        """
        mbti = ""

        # E vs I (外向 vs 内向) - 基于探索性和攻击性
        ei_score = (stats['exploration_score'] + stats['aggression_score']) / 2
        mbti += 'E' if ei_score > 0.5 else 'I'

        # S vs N (感觉 vs 直觉) - 基于谨慎度和计划性
        sn_score = (stats['caution_score'] + stats['planning_score']) / 2
        mbti += 'S' if sn_score > 0.5 else 'N'

        # T vs F (思考 vs 情感) - 基于攻击性和计划性
        tf_score = (stats['aggression_score'] + stats['planning_score']) / 2
        mbti += 'T' if tf_score > 0.5 else 'F'

        # J vs P (判断 vs 知觉) - 基于计划性
        jp_score = stats['planning_score']
        mbti += 'J' if jp_score > 0.6 else 'P'

        # 计算置信度（基于数据量）
        min_actions_for_confidence = 200
        data_confidence = min(stats['total_actions'] / min_actions_for_confidence, 1.0)

        # 基于分数的置信度（分数越接近0.5越不确定）
        score_confidence = sum([
            abs(ei_score - 0.5) * 2,
            abs(sn_score - 0.5) * 2,
            abs(tf_score - 0.5) * 2,
            abs(jp_score - 0.5) * 2
        ]) / 4

        overall_confidence = (data_confidence * 0.6) + (score_confidence * 0.4)

        return mbti, overall_confidence


def analyze_from_database(db_path: str) -> Dict:
    """从数据库分析玩家行为的便捷函数"""
    analyzer = BehaviorAnalyzer(db_path)
    try:
        analyzer.connect()
        result = analyzer.analyze_player_behavior()
        return result
    finally:
        analyzer.close()


if __name__ == '__main__':
    # 测试分析器
    project_root = Path(__file__).parent.parent.parent
    db_path = project_root / 'data' / 'snake_game.db'

    if db_path.exists():
        result = analyze_from_database(str(db_path))
        print("=== 玩家行为分析结果 ===")
        print(f"MBTI类型: {result['mbti']}")
        print(f"置信度: {result['confidence']:.2%}")
        print(f"总会话数: {result['total_sessions']}")
        print(f"总操作数: {result['total_actions']}")
        print(f"平均反应时间: {result['avg_reaction_time_ms']:.1f}ms")
        print("\n特质分数:")
        print(f"  攻击性: {result['traits']['aggression']:.2f}")
        print(f"  谨慎度: {result['traits']['caution']:.2f}")
        print(f"  探索性: {result['traits']['exploration']:.2f}")
        print(f"  计划性: {result['traits']['planning']:.2f}")
    else:
        print(f"数据库不存在: {db_path}")
