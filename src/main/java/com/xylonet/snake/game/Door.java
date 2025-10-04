package com.xylonet.snake.game;

import java.awt.Point;

/**
 * 过关门类 - 处理关卡出口
 * - 蛇达到指定长度后出现
 * - 蛇头触碰后进入下一关
 * - 一旦出现就一直存在
 */
public class Door {

    private Point position;
    private boolean visible;
    private int requiredLength;  // 需要蛇达到的长度

    /**
     * 创建门（初始不可见）
     * @param requiredLength 激活门所需的蛇长度
     */
    public Door(int requiredLength) {
        this.requiredLength = requiredLength;
        this.visible = false;
    }

    /**
     * 设置门的位置并激活
     * @param x X 坐标
     * @param y Y 坐标
     */
    public void activate(int x, int y) {
        this.position = new Point(x, y);
        this.visible = true;
    }

    /**
     * 检查是否应该激活门
     * @param snakeLength 当前蛇的长度
     * @return 如果达到长度要求且门尚未出现，返回 true
     */
    public boolean shouldActivate(int snakeLength) {
        return !visible && snakeLength >= requiredLength;
    }

    /**
     * 检查蛇是否触碰到门
     * @param snakeHead 蛇头位置
     * @return 如果触碰到门，返回 true
     */
    public boolean isTouched(Point snakeHead) {
        return visible && position != null && position.equals(snakeHead);
    }

    /**
     * 获取门的位置
     */
    public Point getPosition() {
        return position != null ? new Point(position) : null;
    }

    /**
     * 检查门是否可见
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * 获取所需长度
     */
    public int getRequiredLength() {
        return requiredLength;
    }

    /**
     * 检查指定坐标是否是门
     */
    public boolean isAt(int x, int y) {
        return visible && position != null && position.x == x && position.y == y;
    }

    /**
     * 重置门（用于新关卡）
     * @param newRequiredLength 新的所需长度
     */
    public void reset(int newRequiredLength) {
        this.requiredLength = newRequiredLength;
        this.visible = false;
        this.position = null;
    }

    /**
     * 隐藏门
     */
    public void hide() {
        this.visible = false;
    }
}
