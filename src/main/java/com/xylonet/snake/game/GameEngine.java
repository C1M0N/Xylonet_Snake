package com.xylonet.snake.game;

import com.xylonet.snake.ui.GamePanel;
import com.xylonet.snake.ui.InfoPanel;

import javax.swing.Timer;
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
    private Timer gameTimer;
    private GameState state;

    private long lastShootTime;  // 上次射击时间
    private static final long SHOOT_COOLDOWN = 1000;  // 射击冷却 1 秒

    private int snakeSpeed = DEFAULT_FRAME_DELAY;  // 蛇的移动速度（毫秒）
    private int frameCounter = 0;  // 帧计数器

    /**
     * 创建游戏引擎
     * @param gamePanel 游戏渲染面板
     * @param infoPanel 信息面板
     */
    public GameEngine(GamePanel gamePanel, InfoPanel infoPanel) {
        this.board = new GameBoard();
        this.gamePanel = gamePanel;
        this.infoPanel = infoPanel;
        this.state = GameState.READY;
        this.lastShootTime = 0;

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

        // 根据设定的速度更新蛇的位置
        if (frameCounter >= snakeSpeed) {
            frameCounter = 0;
            // 更新游戏逻辑
            board.update();
        } else {
            // 只更新子弹，不更新蛇
            board.updateBulletsOnly();
        }

        // 检查游戏结束条件
        if (!board.getSnake().isAlive()) {
            state = GameState.GAME_OVER;
            gameTimer.stop();
            System.out.println("[GameEngine] 游戏结束！");
            return;
        }

        // 检查是否超时
        if (board.isTimeUp()) {
            state = GameState.GAME_OVER;
            board.getSnake().kill();
            gameTimer.stop();
            System.out.println("[GameEngine] 时间到！游戏结束！");
            return;
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
        infoPanel.setScore(board.getScore());
        infoPanel.setLength(board.getSnake().getLength());
        infoPanel.setLevel(board.getLevel());
        infoPanel.setTime(board.getRemainingTime());
        infoPanel.setBulletCount("\u221E");  // 无限子弹
    }

    /**
     * 开始游戏
     */
    public void start() {
        if (state == GameState.READY || state == GameState.GAME_OVER) {
            board = new GameBoard();  // 重新初始化
            state = GameState.RUNNING;
            gameTimer.start();
            System.out.println("[GameEngine] 游戏开始！");
        } else if (state == GameState.PAUSED) {
            resume();
        }
    }

    /**
     * 暂停游戏
     */
    public void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            gameTimer.stop();
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
        System.out.println("[GameEngine] 游戏重置");
        start();
    }

    /**
     * 处理蛇的移动（WASD）
     */
    public void moveSnake(Snake.Direction direction) {
        if (state == GameState.RUNNING) {
            board.getSnake().setDirection(direction);
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
}
