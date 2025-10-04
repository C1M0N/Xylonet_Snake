package com.xylonet.snake.ui;

import com.xylonet.snake.game.GameBoard;

import javax.swing.*;
import java.awt.*;

/**
 * 进度条面板 - 显示食物收集进度
 */
public class ProgressBarPanel extends JPanel {
    private static final Color BG_COLOR = Color.BLACK;
    private static final int PANEL_HEIGHT = 30;

    private GameBoard gameBoard;

    public ProgressBarPanel() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(0, PANEL_HEIGHT));
    }

    /**
     * 设置游戏板引用
     */
    public void setGameBoard(GameBoard board) {
        this.gameBoard = board;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameBoard == null || gameBoard.getDoor() == null) {
            return;
        }

        // 如果门已经可见，不显示进度条
        if (gameBoard.getDoor().isVisible()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        // 进度条参数
        int margin = 20;
        int barWidth = getWidth() - (margin * 2);
        int barHeight = 12;
        int barX = margin;
        int barY = (PANEL_HEIGHT - barHeight) / 2;

        // 获取进度信息
        double progress = gameBoard.getDoorProgress();
        int foodNeeded = gameBoard.getFoodNeededForDoor();

        // 绘制背景（空的部分）
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // 绘制进度（已完成的部分）
        int filledWidth = (int) (barWidth * progress);
        g2d.setColor(new Color(0, 200, 255));  // 亮蓝色
        g2d.fillRect(barX, barY, filledWidth, barHeight);

        // 绘制边框
        g2d.setColor(new Color(120, 120, 120));
        g2d.drawRect(barX, barY, barWidth, barHeight);

        // 绘制文字说明（居中）
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
        String text = "Food to door: " + foodNeeded;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = (getWidth() - textWidth) / 2;
        int textY = barY + barHeight + 14;
        g2d.drawString(text, textX, textY);
    }
}
