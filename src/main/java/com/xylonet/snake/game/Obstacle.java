package com.xylonet.snake.game;

import java.awt.Point;

/**
 * 障碍物类 - 处理可破坏的障碍物
 * - 符号: ■ (灰色)
 * - 有血量系统：普通障碍物 1-3 血，边界墙 9999 血
 * - 被子弹击中会扣血，血量为 0 时消失
 * - 蛇撞到障碍物会死亡
 */
public class Obstacle {

    public enum Type {
        WALL,       // 边界墙 (9999 血)
        BLOCK       // 普通障碍物 (1-3 血)
    }

    private Point position;
    private int health;
    private int maxHealth;
    private Type type;
    private boolean destroyed;

    /**
     * 创建障碍物
     * @param x X 坐标
     * @param y Y 坐标
     * @param type 障碍物类型
     */
    public Obstacle(int x, int y, Type type) {
        this.position = new Point(x, y);
        this.type = type;
        this.destroyed = false;

        // 根据类型设置血量
        if (type == Type.WALL) {
            this.maxHealth = 9999;
            this.health = 9999;
        } else {
            // 普通障碍物随机 1-3 血
            this.maxHealth = 1 + (int)(Math.random() * 3);
            this.health = this.maxHealth;
        }
    }

    /**
     * 创建指定血量的障碍物（用于关卡设计）
     */
    public Obstacle(int x, int y, Type type, int health) {
        this.position = new Point(x, y);
        this.type = type;
        this.maxHealth = health;
        this.health = health;
        this.destroyed = false;
    }

    /**
     * 受到伤害
     * @param damage 伤害值
     * @return 是否被摧毁
     */
    public boolean takeDamage(int damage) {
        if (destroyed) {
            return true;
        }

        health -= damage;
        if (health <= 0) {
            destroyed = true;
            return true;
        }
        return false;
    }

    /**
     * 获取位置
     */
    public Point getPosition() {
        return new Point(position);
    }

    /**
     * 检查指定坐标是否是该障碍物
     */
    public boolean isAt(int x, int y) {
        return !destroyed && position.x == x && position.y == y;
    }

    /**
     * 获取当前血量
     */
    public int getHealth() {
        return health;
    }

    /**
     * 获取最大血量
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * 获取类型
     */
    public Type getType() {
        return type;
    }

    /**
     * 检查是否被摧毁
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * 获取血量百分比（用于显示）
     */
    public double getHealthPercent() {
        return (double) health / maxHealth;
    }

    /**
     * 重置障碍物（恢复满血）
     */
    public void reset() {
        this.health = this.maxHealth;
        this.destroyed = false;
    }
}