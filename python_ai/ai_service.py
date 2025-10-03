#!/usr/bin/env python3
"""
Xylonet Snake AI Service
实时Socket服务，接收游戏状态并返回AI分析
"""

import socket
import json
import threading
import time
import sys
import os
from pathlib import Path

# 导入行为分析器
sys.path.append(str(Path(__file__).parent / 'scripts'))
from behavior_analyzer import analyze_from_database

class SnakeAIService:
    def __init__(self, start_port=50705, max_attempts=10):
        self.start_port = start_port
        self.max_attempts = max_attempts
        self.server_socket = None
        self.actual_port = None
        self.running = False
        self.client_socket = None

    def find_available_port(self):
        """动态分配可用端口"""
        for port in range(self.start_port, self.start_port + self.max_attempts):
            try:
                test_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                test_socket.bind(('localhost', port))
                test_socket.close()
                return port
            except OSError:
                continue
        raise RuntimeError(f"无法在 {self.start_port}-{self.start_port + self.max_attempts} 范围内找到可用端口")

    def start(self):
        """启动Socket服务"""
        try:
            # 找到可用端口
            self.actual_port = self.find_available_port()

            # 创建Socket服务器
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind(('localhost', self.actual_port))
            self.server_socket.listen(1)

            # 将端口号写入文件供Java读取
            port_file = Path(__file__).parent.parent / 'data' / 'ai_port.txt'
            port_file.parent.mkdir(parents=True, exist_ok=True)
            with open(port_file, 'w') as f:
                f.write(str(self.actual_port))

            print(f"[AI服务] 启动成功，监听端口: {self.actual_port}")
            print(f"[AI服务] 端口信息已写入: {port_file}")
            sys.stdout.flush()

            self.running = True
            self.accept_connections()

        except Exception as e:
            print(f"[AI服务] 启动失败: {e}", file=sys.stderr)
            sys.stderr.flush()
            sys.exit(1)

    def accept_connections(self):
        """接受客户端连接"""
        print("[AI服务] 等待Java客户端连接...")
        sys.stdout.flush()

        while self.running:
            try:
                self.client_socket, address = self.server_socket.accept()
                print(f"[AI服务] 客户端已连接: {address}")
                sys.stdout.flush()

                # 处理客户端请求
                self.handle_client()

            except Exception as e:
                if self.running:
                    print(f"[AI服务] 连接错误: {e}", file=sys.stderr)
                    sys.stderr.flush()

    def handle_client(self):
        """处理客户端请求"""
        buffer = ""

        while self.running:
            try:
                # 接收数据
                data = self.client_socket.recv(4096).decode('utf-8')
                if not data:
                    print("[AI服务] 客户端断开连接")
                    sys.stdout.flush()
                    break

                buffer += data

                # 处理完整的JSON消息（以换行符分隔）
                while '\n' in buffer:
                    line, buffer = buffer.split('\n', 1)
                    if line.strip():
                        self.process_message(line.strip())

            except Exception as e:
                print(f"[AI服务] 处理消息错误: {e}", file=sys.stderr)
                sys.stderr.flush()
                break

        if self.client_socket:
            self.client_socket.close()
            self.client_socket = None

    def process_message(self, message):
        """处理接收到的消息"""
        try:
            data = json.loads(message)
            msg_type = data.get('type')

            if msg_type == 'PING':
                # 心跳检测
                response = {'type': 'PONG', 'timestamp': time.time()}
                self.send_response(response)

            elif msg_type == 'GAME_STATE':
                # 游戏状态更新
                response = self.analyze_game_state(data)
                self.send_response(response)

            elif msg_type == 'REQUEST_ANALYSIS':
                # 请求MBTI分析
                response = self.analyze_player_behavior(data)
                self.send_response(response)

            else:
                print(f"[AI服务] 未知消息类型: {msg_type}")
                sys.stdout.flush()

        except json.JSONDecodeError as e:
            print(f"[AI服务] JSON解析错误: {e}", file=sys.stderr)
            sys.stderr.flush()

    def analyze_game_state(self, data):
        """分析游戏状态（示例实现）"""
        # TODO: 这里可以加入实时AI决策逻辑
        return {
            'type': 'GAME_STATE_ACK',
            'timestamp': time.time(),
            'message': 'State received'
        }

    def analyze_player_behavior(self, data):
        """分析玩家行为并返回MBTI"""
        try:
            # 获取数据库路径
            db_path = Path(__file__).parent.parent / 'data' / 'snake_game.db'

            if not db_path.exists():
                print(f"[AI服务] 数据库不存在: {db_path}", file=sys.stderr)
                sys.stderr.flush()
                return {
                    'type': 'ANALYSIS_RESULT',
                    'timestamp': time.time(),
                    'error': 'Database not found',
                    'mbti': 'XXXX',
                    'confidence': 0.0,
                    'sample_size': 0,
                    'traits': {}
                }

            # 使用行为分析器分析数据
            result = analyze_from_database(str(db_path))

            return {
                'type': 'ANALYSIS_RESULT',
                'timestamp': time.time(),
                'mbti': result['mbti'],
                'confidence': result['confidence'],
                'sample_size': result['total_actions'],
                'traits': result['traits']
            }

        except Exception as e:
            print(f"[AI服务] 分析失败: {e}", file=sys.stderr)
            sys.stderr.flush()
            return {
                'type': 'ANALYSIS_RESULT',
                'timestamp': time.time(),
                'error': str(e),
                'mbti': 'XXXX',
                'confidence': 0.0,
                'sample_size': 0,
                'traits': {}
            }

    def send_response(self, response):
        """发送响应到客户端"""
        try:
            message = json.dumps(response) + '\n'
            self.client_socket.sendall(message.encode('utf-8'))
        except Exception as e:
            print(f"[AI服务] 发送响应失败: {e}", file=sys.stderr)
            sys.stderr.flush()

    def shutdown(self):
        """关闭服务"""
        print("[AI服务] 正在关闭...")
        sys.stdout.flush()
        self.running = False

        if self.client_socket:
            self.client_socket.close()
        if self.server_socket:
            self.server_socket.close()

        # 删除端口文件
        try:
            port_file = Path(__file__).parent.parent / 'data' / 'ai_port.txt'
            if port_file.exists():
                port_file.unlink()
        except:
            pass

def main():
    service = SnakeAIService(start_port=50705)

    try:
        service.start()
    except KeyboardInterrupt:
        print("\n[AI服务] 收到中断信号")
        sys.stdout.flush()
    finally:
        service.shutdown()

if __name__ == '__main__':
    main()
