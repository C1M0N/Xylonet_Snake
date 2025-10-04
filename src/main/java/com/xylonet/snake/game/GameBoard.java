package com.xylonet.snake.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏板 - 64x64 网格
 * 管理所有游戏实体（蛇、食物、障碍物、子弹、门）
 * 处理碰撞检测和游戏规则
 */
public class GameBoard {
    public static final int GRID_SIZE = 64;

    private Snake snake;
    private Food food;
    private Door door;
    private List<Obstacle> obstacles;
    private List<Bullet> bullets;
    private Random random;

    private int level;           // 当前关卡
    private int score;           // 分数
    private long levelStartTime; // 关卡开始时间
    private static final int TIME_LIMIT = 180 * 1000; // 3 分钟 (毫秒)

    /**
     * 创建游戏板
     */
    public GameBoard() {
        this.random = new Random();
        this.obstacles = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.level = 1;
        this.score = 0;

        // 初始化游戏实体
        initializeLevel(1);
    }

    /**
     * 初始化关卡
     * @param levelNumber 关卡编号
     */
    public void initializeLevel(int levelNumber) {
        this.level = levelNumber;
        this.levelStartTime = System.currentTimeMillis();

        // 创建蛇（从中心位置开始，向右）
        this.snake = new Snake(GRID_SIZE / 2, GRID_SIZE / 2, Snake.Direction.RIGHT);

        // 创建食物
        this.food = new Food();
        spawnFood();

        // 创建门（第一关需要长度达到 10）
        int requiredLength = 8 + (levelNumber * 2);  // 每关增加 2
        this.door = new Door(requiredLength);

        // 清空子弹
        this.bullets.clear();

        // 生成边界墙和障碍物
        generateObstacles(levelNumber);
    }

    /**
     * 生成边界墙和障碍物
     */
    private void generateObstacles(int levelNumber) {
        obstacles.clear();

        // 生成边界墙 (9999 血)
        for (int i = 0; i < GRID_SIZE; i++) {
            obstacles.add(new Obstacle(i, 0, Obstacle.Type.WALL));           // 上边界
            obstacles.add(new Obstacle(i, GRID_SIZE - 1, Obstacle.Type.WALL)); // 下边界
            obstacles.add(new Obstacle(0, i, Obstacle.Type.WALL));           // 左边界
            obstacles.add(new Obstacle(GRID_SIZE - 1, i, Obstacle.Type.WALL)); // 右边界
        }

        // 生成随机障碍物（随关卡增加数量）
        int obstacleCount = 5 + (levelNumber * 3);
        for (int i = 0; i < obstacleCount; i++) {
            Point pos = findEmptyPosition();
            if (pos != null) {
                obstacles.add(new Obstacle(pos.x, pos.y, Obstacle.Type.BLOCK));
            }
        }
    }

    /**
     * 查找空位置（不被蛇、食物、障碍物、门占据）
     */
    private Point findEmptyPosition() {
        int maxAttempts = 1000;
        for (int i = 0; i < maxAttempts; i++) {
            int x = 1 + random.nextInt(GRID_SIZE - 2);  // 避开边界
            int y = 1 + random.nextInt(GRID_SIZE - 2);

            if (!isOccupied(x, y)) {
                return new Point(x, y);
            }
        }
        return null;  // 找不到空位
    }

    /**
     * 检查指定位置是否被占据
     */
    public boolean isOccupied(int x, int y) {
        // 检查蛇
        if (snake != null && snake.occupies(x, y)) {
            return true;
        }

        // 检查食物
        if (food != null && food.isAt(x, y)) {
            return true;
        }

        // 检查门
        if (door != null && door.isAt(x, y)) {
            return true;
        }

        // 检查障碍物
        for (Obstacle obs : obstacles) {
            if (obs.isAt(x, y)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 生成食物
     */
    public void spawnFood() {
        if (food != null) {
            food.spawn(GRID_SIZE, GRID_SIZE, this::isOccupied);
        }
    }

    /**
     * 更新游戏状态（每帧调用）
     */
    public void update() {
        if (!snake.isAlive()) {
            return;
        }

        // 移动蛇
        boolean ateFood = food.exists() && food.getPosition().equals(snake.getHead());
        snake.move(ateFood);

        // 如果吃到食物
        if (ateFood) {
            food.consume();
            score += 10;
            spawnFood();

            // 检查是否应该激活门
            if (door.shouldActivate(snake.getLength())) {
                Point doorPos = findEmptyPosition();
                if (doorPos != null) {
                    door.activate(doorPos.x, doorPos.y);
                }
            }
        }

        // 更新子弹
        updateBullets();

        // 碰撞检测
        checkCollisions();
    }

    /**
     * 只更新子弹（不更新蛇，用于速度控制）
     */
    public void updateBulletsOnly() {
        updateBullets();
    }

    /**
     * 更新所有子弹
     */
    private void updateBullets() {
        List<Bullet> toRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            bullet.move();

            // 检查是否飞出边界
            if (bullet.isOutOfBounds(GRID_SIZE, GRID_SIZE)) {
                toRemove.add(bullet);
                continue;
            }

            // 检查是否击中障碍物
            Point bulletPos = bullet.getPosition();
            for (Obstacle obs : obstacles) {
                if (obs.isAt(bulletPos.x, bulletPos.y)) {
                    obs.takeDamage(bullet.getDamage());
                    toRemove.add(bullet);
                    break;
                }
            }
        }

        // 移除失效的子弹和障碍物
        bullets.removeAll(toRemove);
        obstacles.removeIf(Obstacle::isDestroyed);
    }

    /**
     * 碰撞检测
     */
    private void checkCollisions() {
        Point head = snake.getHead();

        // 检查是否撞墙/障碍物
        for (Obstacle obs : obstacles) {
            if (obs.isAt(head.x, head.y)) {
                snake.kill();
                return;
            }
        }

        // 检查是否撞到自己
        if (snake.checkSelfCollision()) {
            snake.kill();
            return;
        }

        // 检查是否进门
        if (door.isTouched(head)) {
            nextLevel();
        }
    }

    /**
     * 进入下一关
     */
    private void nextLevel() {
        level++;
        initializeLevel(level);
    }

    /**
     * 发射子弹
     * @param direction 射击方向
     * @return 是否成功发射
     */
    public boolean shootBullet(Bullet.Direction direction) {
        Point head = snake.getHead();

        // 计算子弹起始位置（蛇头前方一格）
        int bulletX = head.x;
        int bulletY = head.y;

        switch (direction) {
            case UP: bulletY -= 1; break;
            case DOWN: bulletY += 1; break;
            case LEFT: bulletX -= 1; break;
            case RIGHT: bulletX += 1; break;
        }

        // 创建子弹
        Bullet bullet = new Bullet(bulletX, bulletY, direction);
        bullets.add(bullet);
        return true;
    }

    /**
     * 获取剩余时间（秒）
     */
    public int getRemainingTime() {
        long elapsed = System.currentTimeMillis() - levelStartTime;
        int remaining = (int)((TIME_LIMIT - elapsed) / 1000);
        return Math.max(0, remaining);
    }

    /**
     * 检查是否超时
     */
    public boolean isTimeUp() {
        return getRemainingTime() <= 0;
    }

    // ===== Getters =====

    public Snake getSnake() { return snake; }
    public Food getFood() { return food; }
    public Door getDoor() { return door; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public List<Bullet> getBullets() { return bullets; }
    public int getLevel() { return level; }
    public int getScore() { return score; }
}
