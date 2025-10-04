package com.xylonet.snake.game;

import java.awt.Point;
import java.util.LinkedList;

/**
 * 蛇类 - 处理蛇的移动、转向和生长
 * - 使用 LinkedList 存储身体节点（头部在前，尾部在后）
 * - 支持 WASD 四向移动，带转向延迟
 * - 不能 180° 掉头
 */
public class Snake {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private LinkedList<Point> body;  // 蛇的身体，head = body.getFirst()
    private Direction currentDirection;
    private Direction pendingDirection;  // 待转向（下一帧生效）
    private boolean isAlive;

    /**
     * 在指定位置创建蛇（初始长度为 3）
     * @param startX 起始 X 坐标
     * @param startY 起始 Y 坐标
     * @param initialDirection 初始方向
     */
    public Snake(int startX, int startY, Direction initialDirection) {
        body = new LinkedList<>();
        this.currentDirection = initialDirection;
        this.pendingDirection = initialDirection;
        this.isAlive = true;

        // 初始化 3 节身体
        for (int i = 0; i < 3; i++) {
            switch (initialDirection) {
                case RIGHT:
                    body.add(new Point(startX - i, startY));
                    break;
                case LEFT:
                    body.add(new Point(startX + i, startY));
                    break;
                case DOWN:
                    body.add(new Point(startX, startY - i));
                    break;
                case UP:
                    body.add(new Point(startX, startY + i));
                    break;
            }
        }
    }

    /**
     * 设置待转向方向（下一帧生效）
     * 拒绝 180° 掉头
     */
    public void setDirection(Direction newDirection) {
        // 防止 180° 掉头
        if (isOppositeDirection(currentDirection, newDirection)) {
            return;
        }
        this.pendingDirection = newDirection;
    }

    /**
     * 判断两个方向是否相反
     */
    private boolean isOppositeDirection(Direction d1, Direction d2) {
        return (d1 == Direction.UP && d2 == Direction.DOWN) ||
               (d1 == Direction.DOWN && d2 == Direction.UP) ||
               (d1 == Direction.LEFT && d2 == Direction.RIGHT) ||
               (d1 == Direction.RIGHT && d2 == Direction.LEFT);
    }

    /**
     * 移动蛇（每帧调用一次）
     * @param grow 是否生长（吃到食物时为 true）
     */
    public void move(boolean grow) {
        if (!isAlive) {
            return;
        }

        // 应用待转向
        currentDirection = pendingDirection;

        // 计算新头部位置
        Point head = body.getFirst();
        Point newHead = new Point(head);

        switch (currentDirection) {
            case UP:
                newHead.y -= 1;
                break;
            case DOWN:
                newHead.y += 1;
                break;
            case LEFT:
                newHead.x -= 1;
                break;
            case RIGHT:
                newHead.x += 1;
                break;
        }

        // 在头部添加新节点
        body.addFirst(newHead);

        // 如果不生长，移除尾部节点
        if (!grow) {
            body.removeLast();
        }
    }

    /**
     * 检查蛇头是否与身体相撞
     */
    public boolean checkSelfCollision() {
        Point head = body.getFirst();
        for (int i = 1; i < body.size(); i++) {
            if (body.get(i).equals(head)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查指定坐标是否在蛇身上
     */
    public boolean occupies(int x, int y) {
        for (Point p : body) {
            if (p.x == x && p.y == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取蛇头位置
     */
    public Point getHead() {
        return new Point(body.getFirst());
    }

    /**
     * 获取整个身体（用于渲染）
     */
    public LinkedList<Point> getBody() {
        return body;
    }

    /**
     * 获取当前方向
     */
    public Direction getCurrentDirection() {
        return currentDirection;
    }

    /**
     * 获取蛇的长度
     */
    public int getLength() {
        return body.size();
    }

    /**
     * 标记蛇死亡
     */
    public void kill() {
        this.isAlive = false;
    }

    /**
     * 检查蛇是否存活
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * 重置蛇（用于重新开始游戏）
     */
    public void reset(int startX, int startY, Direction initialDirection) {
        body.clear();
        this.currentDirection = initialDirection;
        this.pendingDirection = initialDirection;
        this.isAlive = true;

        for (int i = 0; i < 3; i++) {
            switch (initialDirection) {
                case RIGHT:
                    body.add(new Point(startX - i, startY));
                    break;
                case LEFT:
                    body.add(new Point(startX + i, startY));
                    break;
                case DOWN:
                    body.add(new Point(startX, startY - i));
                    break;
                case UP:
                    body.add(new Point(startX, startY + i));
                    break;
            }
        }
    }
}
