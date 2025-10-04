package com.xylonet.snake.game;

import java.awt.Point;
import java.util.Random;

/**
 * 食物类 - 处理食物的生成和消耗
 * - 符号: ◉ (金色)
 * - 随机生成在空位置
 * - 被吃掉后蛇长度 +1
 */
public class Food {

    private Point position;
    private Random random;
    private boolean exists;

    public Food() {
        this.random = new Random();
        this.exists = false;
    }

    /**
     * 在指定范围内随机生成食物
     * 会自动避开蛇和障碍物占据的位置
     *
     * @param boardWidth 地图宽度
     * @param boardHeight 地图高度
     * @param occupied 不可放置食物的坐标检查函数
     */
    public void spawn(int boardWidth, int boardHeight, java.util.function.BiPredicate<Integer, Integer> occupied) {
        int maxAttempts = 1000;  // 防止无限循环
        int attempts = 0;

        while (attempts < maxAttempts) {
            int x = random.nextInt(boardWidth);
            int y = random.nextInt(boardHeight);

            // 检查该位置是否被占用
            if (!occupied.test(x, y)) {
                this.position = new Point(x, y);
                this.exists = true;
                return;
            }
            attempts++;
        }

        // 如果尝试 1000 次都找不到空位，说明地图太满了
        System.err.println("[Food] 警告: 找不到合适的生成位置！");
        this.exists = false;
    }

    /**
     * 消耗食物（被蛇吃掉）
     */
    public void consume() {
        this.exists = false;
    }

    /**
     * 获取食物位置
     */
    public Point getPosition() {
        return position;
    }

    /**
     * 检查食物是否存在
     */
    public boolean exists() {
        return exists;
    }

    /**
     * 检查指定坐标是否是食物位置
     */
    public boolean isAt(int x, int y) {
        return exists && position != null && position.x == x && position.y == y;
    }

    /**
     * 手动设置食物位置（用于测试或关卡设计）
     */
    public void setPosition(int x, int y) {
        this.position = new Point(x, y);
        this.exists = true;
    }

    /**
     * 移除食物
     */
    public void remove() {
        this.exists = false;
    }
}
