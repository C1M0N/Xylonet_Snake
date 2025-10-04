package com.xylonet.snake.game;

import com.xylonet.snake.ui.GamePanel;
import com.xylonet.snake.ui.InfoPanel;
import com.xylonet.snake.data.GameDatabase;

import javax.swing.Timer;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 游戏引擎 - 30 FPS 游戏循环
 * - 定时器驱动的游戏循环
 * - 逻辑更新和渲染分离
 * - 处理游戏状态（运行、暂停、结束）
 */
public class GameEngine {

    public enum GameState {
        READY,      // 准备开始
        RUNNING,    // 运行中
        PAUSED,     // 暂停
        GAME_OVER,  // 游戏结束
        LEVEL_COMPLETE // 关卡完成
    }

    private static final int DEFAULT_FPS = 30;
    private static final int DEFAULT_FRAME_DELAY = 1000 / DEFAULT_FPS;  // 约 33ms

    private GameBoard board;
    private GamePanel gamePanel;
    private InfoPanel infoPanel;
    private com.xylonet.snake.ui.ProgressBarPanel progressBarPanel;
    private com.xylonet.snake.ui.ConsolePanel consolePanel;
    private Timer gameTimer;
    private GameState state;

    private long lastShootTime;  // 上次射击时间
    private static final long SHOOT_COOLDOWN = 1000;  // 射击冷却 1 秒

    private int snakeSpeed = DEFAULT_FRAME_DELAY;  // 蛇的移动速度（毫秒）
    private int speedLevel = 1;  // 速度等级（1, 2, 3...）
    private int frameCounter = 0;  // 帧计数器
    private boolean isSprinting = false;  // 是否正在冲刺
    private static final int SPRINT_SPEED = 33;  // 冲刺速度（33ms，即30 FPS）

    private int lastScore = -1;  // 上次显示的分数
    private int lastLength = -1;  // 上次显示的长度
    private int lastLevel = -1;  // 上次显示的关卡

    // 数据记录
    private GameDatabase database;
    private int currentSessionId;  // 当前会话 ID
    private long lastSnapshotTime;  // 上次快照时间
    private static final long SNAPSHOT_INTERVAL = 5000;  // 快照间隔 5 秒
    private Snake.Direction lastDirection;  // 上次移动方向

    /**
     * 创建游戏引擎
     * @param gamePanel 游戏渲染面板
     * @param infoPanel 信息面板
     * @param progressBarPanel 进度条面板
     * @param consolePanel 控制台面板
     */
    public GameEngine(GamePanel gamePanel, InfoPanel infoPanel, com.xylonet.snake.ui.ProgressBarPanel progressBarPanel, com.xylonet.snake.ui.ConsolePanel consolePanel) {
        this.board = new GameBoard();
        this.gamePanel = gamePanel;
        this.infoPanel = infoPanel;
        this.progressBarPanel = progressBarPanel;
        this.consolePanel = consolePanel;
        this.state = GameState.READY;
        this.lastShootTime = 0;
        this.lastSnapshotTime = 0;
        this.lastDirection = null;

        // 初始化数据库
        this.database = new GameDatabase();

        // 初始化为速度等级 1
        setSpeedLevel(1);

        // 创建定时器（30 FPS，但蛇移动速度可调）
        this.gameTimer = new Timer(DEFAULT_FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameLoop();
            }
        });
    }

    /**
     * 游戏循环（每 33ms 调用一次）
     */
    private void gameLoop() {
        if (state != GameState.RUNNING) {
            return;
        }

        frameCounter += DEFAULT_FRAME_DELAY;

        // 根据设定的速度更新蛇的位置（冲刺时使用冲刺速度）
        int currentSpeed = isSprinting ? SPRINT_SPEED : snakeSpeed;
        if (frameCounter >= currentSpeed) {
            frameCounter = 0;
            // 更新游戏逻辑
            board.update();
        } else {
            // 只更新子弹，不更新蛇
            board.updateBulletsOnly();
        }

        // 检查关卡完成
        if (board.isLevelCompleted()) {
            state = GameState.LEVEL_COMPLETE;
            gameTimer.stop();
            gamePanel.setOverlay("LEVEL " + (board.getLevel() - 1) + " COMPLETE!", new Color(0, 255, 100));
            board.acknowledgeLevelComplete();
            if (consolePanel != null) {
                consolePanel.addMessage("关卡 " + (board.getLevel() - 1) + " 完成！按 SPACE 继续", com.xylonet.snake.ui.ConsolePanel.MessageType.SYSTEM);
            }
            System.out.println("[GameEngine] 关卡完成！进入关卡 " + board.getLevel());
            return;
        }

        // 检查游戏结束条件
        if (!board.getSnake().isAlive()) {
            state = GameState.GAME_OVER;
            gameTimer.stop();
            gamePanel.setOverlay("GAME OVER", new Color(255, 80, 80));

            // 更新会话信息
            database.endSession(currentSessionId, board.getScore(), board.getLevel(), "died");

            if (consolePanel != null) {
                consolePanel.addMessage("游戏结束！按 R 重新开始", com.xylonet.snake.ui.ConsolePanel.MessageType.ERROR);
            }
            System.out.println("[GameEngine] 游戏结束！");
            return;
        }

        // 检查是否超时
        if (board.isTimeUp()) {
            state = GameState.GAME_OVER;
            board.getSnake().kill();
            gameTimer.stop();
            gamePanel.setOverlay("TIME'S UP", new Color(255, 80, 80));

            // 更新会话信息
            database.endSession(currentSessionId, board.getScore(), board.getLevel(), "timeout");

            if (consolePanel != null) {
                consolePanel.addMessage("时间到！游戏结束", com.xylonet.snake.ui.ConsolePanel.MessageType.ERROR);
            }
            System.out.println("[GameEngine] 时间到！游戏结束！");
            return;
        }

        // 定期记录游戏快照（每 5 秒）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSnapshotTime >= SNAPSHOT_INTERVAL) {
            recordSnapshot();
            lastSnapshotTime = currentTime;
        }

        // 更新 UI
        updateUI();

        // 重绘游戏面板
        gamePanel.render(board);
    }

    /**
     * 更新信息面板
     */
    private void updateUI() {
        int currentScore = board.getScore();
        int currentLength = board.getSnake().getLength();
        int currentLevel = board.getLevel();

        // 更新 InfoPanel（保留用于兼容）
        infoPanel.setScore(currentScore);
        infoPanel.setLength(currentLength);
        infoPanel.setLevel(currentLevel);
        infoPanel.setTime(board.getRemainingTime());
        infoPanel.setBulletCount("\u221E");  // 无限子弹

        // 更新进度条
        if (progressBarPanel != null) {
            progressBarPanel.setGameBoard(board);
        }

        // 更新 Console 显示系统级别信息（只显示关卡变化）
        if (consolePanel != null) {
            if (currentLevel != lastLevel) {
                consolePanel.addMessage("=== Level " + currentLevel + " ===", com.xylonet.snake.ui.ConsolePanel.MessageType.SYSTEM);
                lastLevel = currentLevel;
            }
        }
    }

    /**
     * 开始游戏
     */
    public void start() {
        if (state == GameState.READY || state == GameState.GAME_OVER) {
            board = new GameBoard();  // 重新初始化
            state = GameState.RUNNING;
            gamePanel.clearOverlay();  // 清除覆盖层

            // 创建新的游戏会话
            currentSessionId = database.createSession();
            lastSnapshotTime = System.currentTimeMillis();
            lastDirection = null;

            gameTimer.start();
            if (consolePanel != null) {
                consolePanel.addMessage("游戏开始！", com.xylonet.snake.ui.ConsolePanel.MessageType.SYSTEM);
            }
            System.out.println("[GameEngine] 游戏开始！会话 ID: " + currentSessionId);
        } else if (state == GameState.PAUSED) {
            resume();
        } else if (state == GameState.LEVEL_COMPLETE) {
            state = GameState.RUNNING;
            gamePanel.clearOverlay();
            gameTimer.start();
        }
    }

    /**
     * 暂停游戏
     */
    public void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            gameTimer.stop();
            if (consolePanel != null) {
                consolePanel.addMessage("游戏暂停", com.xylonet.snake.ui.ConsolePanel.MessageType.WARNING);
            }
            System.out.println("[GameEngine] 游戏暂停");
        }
    }

    /**
     * 恢复游戏
     */
    public void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            gameTimer.start();
            if (consolePanel != null) {
                consolePanel.addMessage("游戏继续", com.xylonet.snake.ui.ConsolePanel.MessageType.SYSTEM);
            }
            System.out.println("[GameEngine] 游戏恢复");
        }
    }

    /**
     * 切换暂停状态
     */
    public void togglePause() {
        if (state == GameState.RUNNING) {
            pause();
        } else if (state == GameState.PAUSED) {
            resume();
        }
    }

    /**
     * 重新开始游戏
     */
    public void restart() {
        gameTimer.stop();
        board = new GameBoard();
        state = GameState.READY;
        if (consolePanel != null) {
            consolePanel.addMessage("游戏重置", com.xylonet.snake.ui.ConsolePanel.MessageType.SYSTEM);
        }
        System.out.println("[GameEngine] 游戏重置");
        start();
    }

    /**
     * 处理蛇的移动（WASD）
     */
    public void moveSnake(Snake.Direction direction) {
        if (state == GameState.RUNNING) {
            board.getSnake().setDirection(direction);

            // 记录方向改变
            if (direction != lastDirection) {
                Point headPos = board.getSnake().getHead();
                database.recordAction(currentSessionId, "direction_change", headPos.x, headPos.y, direction.name());
                lastDirection = direction;
            }
        }
    }

    /**
     * 处理射击（方向键）
     */
    public void shoot(Bullet.Direction direction) {
        if (state != GameState.RUNNING) {
            return;
        }

        // 检查冷却时间
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime < SHOOT_COOLDOWN) {
            return;  // 冷却中
        }

        // 发射子弹
        if (board.shootBullet(direction)) {
            lastShootTime = currentTime;

            // 记录射击事件
            Point headPos = board.getSnake().getHead();
            database.recordShooting(currentSessionId, headPos.x, headPos.y, direction.name());
        }
    }

    /**
     * 获取当前游戏状态
     */
    public GameState getState() {
        return state;
    }

    /**
     * 获取游戏板（用于渲染）
     */
    public GameBoard getBoard() {
        return board;
    }

    /**
     * 停止游戏引擎
     */
    public void stop() {
        gameTimer.stop();
        state = GameState.GAME_OVER;
    }

    /**
     * 获取射击冷却剩余时间（毫秒）
     */
    public long getShootCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastShootTime;
        return Math.max(0, SHOOT_COOLDOWN - elapsed);
    }

    /**
     * 检查是否可以射击
     */
    public boolean canShoot() {
        return getShootCooldownRemaining() == 0;
    }

    /**
     * 设置蛇的移动速度
     * @param speedMs 移动间隔（毫秒），数值越小速度越快
     */
    public void setSnakeSpeed(int speedMs) {
        if (speedMs <= 0) {
            this.snakeSpeed = DEFAULT_FRAME_DELAY;
            System.out.println("[GameEngine] 蛇速度已重置为默认值: " + DEFAULT_FRAME_DELAY + "ms");
        } else {
            this.snakeSpeed = speedMs;
            System.out.println("[GameEngine] 蛇速度已设置为: " + speedMs + "ms");
        }
        this.frameCounter = 0;  // 重置计数器
    }

    /**
     * 获取当前蛇速度
     */
    public int getSnakeSpeed() {
        return snakeSpeed;
    }

    /**
     * 根据速度等级计算延迟
     * 公式: y = -10ln(x-1) + 70
     * 特殊情况: 等级 1 = 100ms
     * @param level 速度等级（1, 2, 3...）
     * @return 延迟（毫秒）
     */
    public static int calculateDelayFromLevel(int level) {
        if (level <= 1) {
            return 100;  // 等级 1 固定为 100ms
        }
        // y = -10ln(x-1) + 70
        double delay = -10.0 * Math.log(level - 1) + 70.0;
        return (int) Math.round(delay);
    }

    /**
     * 设置速度等级
     * @param level 速度等级（1, 2, 3...）
     */
    public void setSpeedLevel(int level) {
        if (level < 1) {
            level = 1;
        }
        this.speedLevel = level;
        this.snakeSpeed = calculateDelayFromLevel(level);
        this.frameCounter = 0;
        System.out.println("[GameEngine] 速度等级设置为: " + level + " (延迟: " + snakeSpeed + "ms)");
    }

    /**
     * 获取当前速度等级
     */
    public int getSpeedLevel() {
        return speedLevel;
    }

    /**
     * 设置冲刺状态
     * @param sprinting 是否冲刺
     */
    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting;
    }

    /**
     * 检查是否正在冲刺
     */
    public boolean isSprinting() {
        return isSprinting;
    }

    /**
     * 记录游戏快照
     */
    private void recordSnapshot() {
        Point headPos = board.getSnake().getHead();
        int snakeLength = board.getSnake().getLength();
        int obstacleCount = (int) board.getObstacles().stream()
                .filter(o -> !o.isDestroyed())
                .count();

        database.recordSnapshot(
                currentSessionId,
                headPos.x,
                headPos.y,
                snakeLength,
                board.getScore(),
                board.getLevel(),
                board.getRemainingTime(),
                obstacleCount
        );
    }
}
