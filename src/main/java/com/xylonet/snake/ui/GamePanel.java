package com.xylonet.snake.ui;

import com.xylonet.snake.game.GameBoard;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏渲染面板
 * 64x64 网格，ASCII 风格渲染
 */
public class GamePanel extends JPanel {
    // 网格配置
    public static final int GRID_SIZE = 64;
    public static final int CELL_SIZE = 10;  // 每个格子 10x10 像素

    // 总尺寸：64 * 10 = 640x640
    private static final int PANEL_WIDTH = GRID_SIZE * CELL_SIZE;
    private static final int PANEL_HEIGHT = GRID_SIZE * CELL_SIZE;

    // 颜色方案（ASCII 风格）
    public static final Color BG_COLOR = Color.BLACK;                     // 背景：纯黑
    public static final Color SNAKE_BODY_COLOR = Color.WHITE;             // 蛇身：白色
    public static final Color SNAKE_HEAD_COLOR = new Color(200, 200, 200);// 蛇头：浅灰
    public static final Color FOOD_COLOR = new Color(255, 215, 0);        // 食物：金色 ◉
    public static final Color OBSTACLE_COLOR = new Color(128, 128, 128);  // 障碍：灰色
    public static final Color BOUNDARY_COLOR = new Color(200, 0, 0);      // 边界：深红
    public static final Color BULLET_COLOR = new Color(255, 100, 100);    // 子弹：浅红 @
    public static final Color DOOR_COLOR = new Color(0, 200, 255);        // 门：亮蓝

    // 符号定义
    public static final char FOOD_SYMBOL = '◉';     // 食物符号
    public static final char BULLET_SYMBOL = '@';    // 子弹符号
    public static final char SNAKE_BODY_SYMBOL = '#'; // 蛇身符号
    public static final char SNAKE_HEAD_SYMBOL = '□'; // 蛇头符号
    public static final char OBSTACLE_SYMBOL = '■';   // 障碍符号

    private GameBoard gameBoard;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(BG_COLOR);
        setDoubleBuffered(true);  // 启用双缓冲减少闪烁
    }

    /**
     * 设置游戏板引用（供游戏引擎使用）
     */
    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    /**
     * 渲染游戏（供游戏引擎调用）
     */
    public void render(GameBoard board) {
        this.gameBoard = board;
        repaint();
    }

    /**
     * 渲染游戏画面
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // 设置抗锯齿（可选，ASCII 风格可能不需要）
        // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 如果游戏板未初始化，显示提示
        if (gameBoard == null) {
            drawWelcomeScreen(g2d);
            return;
        }

        // 绘制游戏内容
        drawGrid(g2d);
        drawGameElements(g2d);
    }

    /**
     * 绘制网格线（调试用，可选）
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(30, 30, 30));  // 深灰色网格线

        // 绘制垂直线
        for (int x = 0; x <= GRID_SIZE; x++) {
            g2d.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, PANEL_HEIGHT);
        }

        // 绘制水平线
        for (int y = 0; y <= GRID_SIZE; y++) {
            g2d.drawLine(0, y * CELL_SIZE, PANEL_WIDTH, y * CELL_SIZE);
        }
    }

    /**
     * 绘制游戏元素（蛇、食物、障碍等）
     */
    private void drawGameElements(Graphics2D g2d) {
        drawObstacles(g2d);
        drawFood(g2d);
        drawBullets(g2d);
        drawDoor(g2d);
        drawSnake(g2d);  // 蛇最后绘制，在最上层
    }

    /**
     * 绘制障碍物
     */
    private void drawObstacles(Graphics2D g2d) {
        for (var obstacle : gameBoard.getObstacles()) {
            if (obstacle.isDestroyed()) continue;

            Point pos = obstacle.getPosition();
            Color color = obstacle.getType() == com.xylonet.snake.game.Obstacle.Type.WALL
                    ? BOUNDARY_COLOR : OBSTACLE_COLOR;

            drawCell(g2d, pos.x, pos.y, color, true);
            drawSymbol(g2d, pos.x, pos.y, OBSTACLE_SYMBOL, Color.BLACK);
        }
    }

    /**
     * 绘制食物
     */
    private void drawFood(Graphics2D g2d) {
        var food = gameBoard.getFood();
        if (food != null && food.exists()) {
            Point pos = food.getPosition();
            drawCell(g2d, pos.x, pos.y, FOOD_COLOR, true);
            drawSymbol(g2d, pos.x, pos.y, FOOD_SYMBOL, Color.BLACK);
        }
    }

    /**
     * 绘制子弹
     */
    private void drawBullets(Graphics2D g2d) {
        for (var bullet : gameBoard.getBullets()) {
            if (!bullet.isActive()) continue;

            Point pos = bullet.getPosition();
            drawCell(g2d, pos.x, pos.y, BULLET_COLOR, true);
            drawSymbol(g2d, pos.x, pos.y, BULLET_SYMBOL, Color.WHITE);
        }
    }

    /**
     * 绘制门
     */
    private void drawDoor(Graphics2D g2d) {
        var door = gameBoard.getDoor();
        if (door != null && door.isVisible()) {
            Point pos = door.getPosition();
            drawCell(g2d, pos.x, pos.y, DOOR_COLOR, true);
            drawSymbol(g2d, pos.x, pos.y, '\u25C7', Color.BLACK);  // ◇ 菱形
        }
    }

    /**
     * 绘制蛇
     */
    private void drawSnake(Graphics2D g2d) {
        var snake = gameBoard.getSnake();
        if (snake == null || !snake.isAlive()) return;

        var body = snake.getBody();
        for (int i = 0; i < body.size(); i++) {
            Point p = body.get(i);

            if (i == 0) {
                // 蛇头 □
                drawCell(g2d, p.x, p.y, SNAKE_HEAD_COLOR, true);
                drawSymbol(g2d, p.x, p.y, SNAKE_HEAD_SYMBOL, Color.BLACK);
            } else {
                // 蛇身 #
                drawCell(g2d, p.x, p.y, SNAKE_BODY_COLOR, true);
                drawSymbol(g2d, p.x, p.y, SNAKE_BODY_SYMBOL, Color.BLACK);
            }
        }
    }

    /**
     * 绘制欢迎屏幕（游戏未开始时）
     */
    private void drawWelcomeScreen(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 24));

        String message = "Xylonet Snake";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        int y = PANEL_HEIGHT / 2;

        g2d.drawString(message, x, y);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        String hint = "Press SPACE to start";
        x = (PANEL_WIDTH - g2d.getFontMetrics().stringWidth(hint)) / 2;
        g2d.drawString(hint, x, y + 40);
    }

    /**
     * 绘制单个方块（通用方法）
     * @param g2d Graphics2D 对象
     * @param gridX 网格 X 坐标
     * @param gridY 网格 Y 坐标
     * @param color 颜色
     * @param filled 是否填充（true=实心，false=空心）
     */
    public void drawCell(Graphics2D g2d, int gridX, int gridY, Color color, boolean filled) {
        int pixelX = gridX * CELL_SIZE;
        int pixelY = gridY * CELL_SIZE;

        g2d.setColor(color);
        if (filled) {
            g2d.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
        } else {
            g2d.drawRect(pixelX, pixelY, CELL_SIZE - 1, CELL_SIZE - 1);
        }
    }

    /**
     * 绘制文字符号（ASCII 字符渲染）
     * @param g2d Graphics2D 对象
     * @param gridX 网格 X 坐标
     * @param gridY 网格 Y 坐标
     * @param symbol 符号字符（如 '#', '□', '■' 等）
     * @param color 颜色
     */
    public void drawSymbol(Graphics2D g2d, int gridX, int gridY, char symbol, Color color) {
        int pixelX = gridX * CELL_SIZE;
        int pixelY = gridY * CELL_SIZE;

        g2d.setColor(color);
        g2d.setFont(new Font("Monospaced", Font.BOLD, CELL_SIZE));

        // 居中对齐
        FontMetrics fm = g2d.getFontMetrics();
        int charWidth = fm.charWidth(symbol);
        int charHeight = fm.getAscent();

        int x = pixelX + (CELL_SIZE - charWidth) / 2;
        int y = pixelY + (CELL_SIZE + charHeight) / 2 - 2;

        g2d.drawString(String.valueOf(symbol), x, y);
    }
}
