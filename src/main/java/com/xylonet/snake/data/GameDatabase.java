package com.xylonet.snake.data;

import java.sql.*;
import java.util.UUID;

/**
 * 游戏数据库管理类
 * 负责记录玩家操作、游戏状态等数据到SQLite
 */
public class GameDatabase {
    private Connection connection;
    private final String dbPath;
    private String currentSessionId;

    public GameDatabase() {
        String projectRoot = System.getProperty("user.dir");
        this.dbPath = projectRoot + "/data/snake_game.db";
    }

    /**
     * 连接数据库
     */
    public boolean connect() {
        try {
            String url = "jdbc:sqlite:" + dbPath;
            connection = DriverManager.getConnection(url);
            System.out.println("[DB] 数据库连接成功: " + dbPath);
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] 数据库连接失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 开始新的游戏会话
     */
    public String startNewSession() {
        currentSessionId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO game_sessions (session_id, start_time) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, currentSessionId);
            pstmt.setLong(2, currentTime);
            pstmt.executeUpdate();

            System.out.println("[DB] 新游戏会话开始: " + currentSessionId);
            return currentSessionId;

        } catch (SQLException e) {
            System.err.println("[DB] 创建会话失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 结束当前游戏会话
     */
    public void endSession(int finalScore, int snakeLength, boolean victory, String deathReason) {
        if (currentSessionId == null) return;

        String sql = "UPDATE game_sessions SET end_time = ?, final_score = ?, " +
                "snake_length = ?, victory = ?, death_reason = ?, duration_seconds = ? WHERE session_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            long endTime = System.currentTimeMillis();

            // 计算游戏时长
            String query = "SELECT start_time FROM game_sessions WHERE session_id = ?";
            long startTime = 0;
            try (PreparedStatement queryStmt = connection.prepareStatement(query)) {
                queryStmt.setString(1, currentSessionId);
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    startTime = rs.getLong("start_time");
                }
            }
            int durationSeconds = (int) ((endTime - startTime) / 1000);

            pstmt.setLong(1, endTime);
            pstmt.setInt(2, finalScore);
            pstmt.setInt(3, snakeLength);
            pstmt.setBoolean(4, victory);
            pstmt.setString(5, deathReason);
            pstmt.setInt(6, durationSeconds);
            pstmt.setString(7, currentSessionId);
            pstmt.executeUpdate();

            System.out.println("[DB] 游戏会话结束: " + currentSessionId);

        } catch (SQLException e) {
            System.err.println("[DB] 结束会话失败: " + e.getMessage());
        }
    }

    /**
     * 记录玩家操作
     */
    public void recordAction(String actionType, String direction, int snakeLength, int posX, int posY) {
        if (currentSessionId == null) return;

        String sql = "INSERT INTO player_actions (session_id, timestamp, action_type, direction, " +
                "snake_length, position_x, position_y) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, currentSessionId);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, actionType);
            pstmt.setString(4, direction);
            pstmt.setInt(5, snakeLength);
            pstmt.setInt(6, posX);
            pstmt.setInt(7, posY);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DB] 记录操作失败: " + e.getMessage());
        }
    }

    /**
     * 记录射击事件
     */
    public void recordShooting(int targetX, int targetY, boolean hit, int reactionTimeMs) {
        if (currentSessionId == null) return;

        String sql = "INSERT INTO shooting_events (session_id, timestamp, target_x, target_y, hit, reaction_time_ms) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, currentSessionId);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setInt(3, targetX);
            pstmt.setInt(4, targetY);
            pstmt.setBoolean(5, hit);
            pstmt.setInt(6, reactionTimeMs);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DB] 记录射击失败: " + e.getMessage());
        }
    }

    /**
     * 记录食物收集
     */
    public void recordFoodCollection(String foodType, int foodX, int foodY, int distanceTraveled, int timeToCollectMs) {
        if (currentSessionId == null) return;

        String sql = "INSERT INTO food_collection (session_id, timestamp, food_type, food_x, food_y, " +
                "distance_traveled, time_to_collect_ms) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, currentSessionId);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, foodType);
            pstmt.setInt(4, foodX);
            pstmt.setInt(5, foodY);
            pstmt.setInt(6, distanceTraveled);
            pstmt.setInt(7, timeToCollectMs);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DB] 记录食物收集失败: " + e.getMessage());
        }
    }

    /**
     * 记录游戏状态快照
     */
    public void recordSnapshot(int headX, int headY, int length, String direction, int obstaclesCount,
                                double distanceToFood, int health, int attack, int defense) {
        if (currentSessionId == null) return;

        String sql = "INSERT INTO game_snapshots (session_id, timestamp, snake_head_x, snake_head_y, " +
                "snake_length, snake_direction, nearby_obstacles_count, distance_to_food, health, attack_power, defense_power) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, currentSessionId);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setInt(3, headX);
            pstmt.setInt(4, headY);
            pstmt.setInt(5, length);
            pstmt.setString(6, direction);
            pstmt.setInt(7, obstaclesCount);
            pstmt.setDouble(8, distanceToFood);
            pstmt.setInt(9, health);
            pstmt.setInt(10, attack);
            pstmt.setInt(11, defense);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DB] 记录快照失败: " + e.getMessage());
        }
    }

    /**
     * 获取总操作数（用于分析）
     */
    public int getTotalActionCount() {
        String sql = "SELECT COUNT(*) as count FROM player_actions";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("[DB] 查询操作数失败: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 获取当前会话ID
     */
    public String getCurrentSessionId() {
        return currentSessionId;
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] 数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("[DB] 关闭数据库失败: " + e.getMessage());
        }
    }
}
