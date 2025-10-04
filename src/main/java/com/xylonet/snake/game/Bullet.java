package com.xylonet.snake.game;

import java.awt.Point;

/**
 * 子弹类 - 处理子弹的飞行和碰撞
 * - 符号: @ (浅红色)
 * - 四个方向飞行
 * - 碰到障碍物后消失
 * - 飞出地图边界后消失
 */
public class Bullet {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Point position;
    private Direction direction;
    private boolean active;
    private int damage;  // 造成的伤害值

    /**
     * 创建子弹
     * @param x 起始 X 坐标
     * @param y 起始 Y 坐标
     * @param direction 飞行方向
     */
    public Bullet(int x, int y, Direction direction) {
        this.position = new Point(x, y);
        this.direction = direction;
        this.active = true;
        this.damage = 1;  // 默认伤害为 1
    }

    /**
     * 创建指定伤害的子弹（用于特殊武器）
     */
    public Bullet(int x, int y, Direction direction, int damage) {
        this.position = new Point(x, y);
        this.direction = direction;
        this.active = true;
        this.damage = damage;
    }

    /**
     * 移动子弹（每帧调用）
     * 子弹每帧移动 1 格
     */
    public void move() {
        if (!active) {
            return;
        }

        switch (direction) {
            case UP:
                position.y -= 1;
                break;
            case DOWN:
                position.y += 1;
                break;
            case LEFT:
                position.x -= 1;
                break;
            case RIGHT:
                position.x += 1;
                break;
        }
    }

    /**
     * 检查是否飞出边界
     * @param width 地图宽度
     * @param height 地图高度
     * @return 如果飞出边界，返回 true
     */
    public boolean isOutOfBounds(int width, int height) {
        return position.x < 0 || position.x >= width ||
               position.y < 0 || position.y >= height;
    }

    /**
     * 销毁子弹（碰撞后调用）
     */
    public void destroy() {
        this.active = false;
    }

    /**
     * 获取位置
     */
    public Point getPosition() {
        return new Point(position);
    }

    /**
     * 获取方向
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * 检查子弹是否仍然活跃
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 获取伤害值
     */
    public int getDamage() {
        return damage;
    }

    /**
     * 检查指定坐标是否是子弹位置
     */
    public boolean isAt(int x, int y) {
        return active && position.x == x && position.y == y;
    }

    /**
     * 从 Snake.Direction 转换为 Bullet.Direction
     */
    public static Direction fromSnakeDirection(Snake.Direction snakeDir) {
        switch (snakeDir) {
            case UP: return Direction.UP;
            case DOWN: return Direction.DOWN;
            case LEFT: return Direction.LEFT;
            case RIGHT: return Direction.RIGHT;
            default: return Direction.UP;
        }
    }
}
