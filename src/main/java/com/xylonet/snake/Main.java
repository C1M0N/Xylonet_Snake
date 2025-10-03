package com.xylonet.snake;

import com.xylonet.snake.data.GameDatabase;
import com.xylonet.snake.network.AIClient;
import com.xylonet.snake.network.PythonProcessManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * 游戏主类 - 演示Java-Python通信
 * 这是一个测试示例，展示如何集成所有组件
 */
public class Main {
    private static PythonProcessManager pythonManager;
    private static AIClient aiClient;
    private static GameDatabase database;

    public static void main(String[] args) {
        System.out.println("=== Xylonet Snake - Java-Python 通信测试 ===\n");

        // 初始化组件
        if (!initializeComponents()) {
            System.err.println("初始化失败，程序退出");
            System.exit(1);
        }

        // 运行测试
        runCommunicationTest();

        // 清理资源
        cleanup();
    }

    /**
     * 初始化所有组件
     */
    private static boolean initializeComponents() {
        try {
            // 1. 初始化数据库
            System.out.println("1. 初始化数据库...");
            initDatabase();

            // 2. 启动Python AI服务
            System.out.println("\n2. 启动Python AI服务...");
            pythonManager = new PythonProcessManager();
            if (!pythonManager.startPythonService()) {
                return false;
            }

            // 3. 连接到AI服务
            System.out.println("\n3. 连接到AI服务...");
            Thread.sleep(1000);  // 等待Python服务完全启动

            aiClient = new AIClient();
            Integer port = pythonManager.getAiServicePort();
            if (port == null || !aiClient.connect("localhost", port)) {
                return false;
            }

            // 4. 连接数据库
            System.out.println("\n4. 连接数据库...");
            database = new GameDatabase();
            if (!database.connect()) {
                return false;
            }

            System.out.println("\n✓ 所有组件初始化成功！\n");
            return true;

        } catch (Exception e) {
            System.err.println("初始化异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 初始化数据库（运行Python脚本）
     */
    private static void initDatabase() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String pythonCommand = os.contains("win") ? "python" : "python3";

            String projectRoot = System.getProperty("user.dir");
            String scriptPath = projectRoot + "/python_ai/scripts/init_database.py";

            ProcessBuilder pb = new ProcessBuilder(pythonCommand, scriptPath);
            Process process = pb.inheritIO().start();
            process.waitFor();

        } catch (Exception e) {
            System.err.println("初始化数据库失败: " + e.getMessage());
        }
    }

    /**
     * 运行通信测试
     */
    private static void runCommunicationTest() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("通信测试菜单:");
        System.out.println("1. 测试心跳检测");
        System.out.println("2. 模拟游戏会话并记录数据");
        System.out.println("3. 请求MBTI分析");
        System.out.println("4. 发送游戏状态");
        System.out.println("5. 运行完整测试流程");
        System.out.println("0. 退出");
        System.out.println("========================================\n");

        boolean running = true;
        while (running) {
            System.out.print("请选择操作 (0-5): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    testHeartbeat();
                    break;
                case "2":
                    simulateGameSession();
                    break;
                case "3":
                    requestMBTIAnalysis();
                    break;
                case "4":
                    sendGameState();
                    break;
                case "5":
                    runFullTest();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("无效选择，请重试");
            }
            System.out.println();
        }
    }

    /**
     * 测试1: 心跳检测
     */
    private static void testHeartbeat() {
        System.out.println("\n--- 测试心跳检测 ---");
        boolean success = aiClient.sendPing();
        System.out.println("心跳结果: " + (success ? "✓ 成功" : "✗ 失败"));
    }

    /**
     * 测试2: 模拟游戏会话
     */
    private static void simulateGameSession() {
        System.out.println("\n--- 模拟游戏会话 ---");

        // 开始新会话
        String sessionId = database.startNewSession();
        System.out.println("开始新会话: " + sessionId);

        // 模拟一些游戏操作
        System.out.println("模拟玩家操作...");
        String[] directions = {"UP", "RIGHT", "RIGHT", "DOWN", "LEFT", "UP", "UP", "RIGHT"};

        for (int i = 0; i < directions.length; i++) {
            database.recordAction("MOVE", directions[i], 3 + i, 10 + i, 10 + i);

            // 模拟射击
            if (i % 3 == 0) {
                database.recordShooting(15 + i, 15 + i, i % 2 == 0, 200 + (i * 50));
            }

            // 模拟食物收集
            if (i % 4 == 0) {
                database.recordFoodCollection("NORMAL", 12 + i, 12 + i, 5, 1000 + (i * 200));
            }

            // 记录快照
            database.recordSnapshot(10 + i, 10 + i, 3 + i, directions[i], i % 3, 5.5, 100, 10, 5);

            try {
                Thread.sleep(100);  // 模拟时间间隔
            } catch (InterruptedException e) {
                break;
            }
        }

        // 结束会话
        database.endSession(150, 10, false, "Hit obstacle");
        System.out.println("✓ 会话结束，已记录 " + directions.length + " 次操作");
    }

    /**
     * 测试3: 请求MBTI分析
     */
    private static void requestMBTIAnalysis() {
        System.out.println("\n--- 请求MBTI分析 ---");
        int totalActions = database.getTotalActionCount();
        System.out.println("当前总操作数: " + totalActions);

        if (totalActions == 0) {
            System.out.println("⚠ 没有数据，请先运行测试2模拟游戏会话");
            return;
        }

        CompletableFuture<AIClient.AnalysisResult> future = aiClient.requestAnalysis(totalActions);

        future.thenAccept(result -> {
            if (result != null) {
                System.out.println("\n=== 分析结果 ===");
                System.out.println(result);
            } else {
                System.out.println("✗ 分析失败");
            }
        }).join();  // 等待完成
    }

    /**
     * 测试4: 发送游戏状态
     */
    private static void sendGameState() {
        System.out.println("\n--- 发送游戏状态 ---");

        Map<String, Object> gameState = new HashMap<>();
        gameState.put("snake_length", 15);
        gameState.put("score", 300);
        gameState.put("direction", "UP");
        gameState.put("position_x", 25);
        gameState.put("position_y", 30);
        gameState.put("health", 85);

        aiClient.sendGameState(gameState);
        System.out.println("✓ 游戏状态已发送");
    }

    /**
     * 测试5: 完整测试流程
     */
    private static void runFullTest() {
        System.out.println("\n=== 运行完整测试流程 ===\n");

        testHeartbeat();
        System.out.println("\n等待1秒...\n");
        sleep(1000);

        simulateGameSession();
        System.out.println("\n等待1秒...\n");
        sleep(1000);

        sendGameState();
        System.out.println("\n等待1秒...\n");
        sleep(1000);

        requestMBTIAnalysis();

        System.out.println("\n✓ 完整测试流程结束");
    }

    /**
     * 清理资源
     */
    private static void cleanup() {
        System.out.println("\n正在清理资源...");

        if (aiClient != null) {
            aiClient.disconnect();
        }

        if (database != null) {
            database.close();
        }

        if (pythonManager != null) {
            pythonManager.stopPythonService();
        }

        System.out.println("再见！");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
