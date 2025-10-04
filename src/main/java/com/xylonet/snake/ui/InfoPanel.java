package com.xylonet.snake.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 信息显示面板（顶部）
 * 显示：分数、蛇长度、关卡、时间、MBTI 分析、子弹数、暂停按钮
 */
public class InfoPanel extends JPanel {
    private static final int PANEL_WIDTH = 400;  // 右侧面板宽度
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;  // 白色文字
    private static final Font INFO_FONT = new Font("Monospaced", Font.BOLD, 13);

    // 信息标签
    private JLabel scoreLabel;
    private JLabel lengthLabel;
    private JLabel levelLabel;
    private JLabel timeLabel;
    private JLabel mbtiLabel;
    private JLabel bulletsLabel;
    private JButton pauseButton;

    // 数据
    private int score = 0;
    private int snakeLength = 3;
    private int level = 1;
    private int timeRemaining = 180;  // 3 分钟 = 180 秒
    private String mbtiType = "分析中...";
    private int bulletsCount = 999;  // 无限子弹显示为 ∞

    public InfoPanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, 300));  // 设置固定高度
        setBackground(BG_COLOR);
        setLayout(new GridLayout(8, 1, 5, 8));  // 8 行 1 列布局（垂直排列）
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initializeLabels();
        addComponents();
        updateDisplay();
    }

    /**
     * 初始化所有标签
     */
    private void initializeLabels() {
        scoreLabel = createLabel();
        lengthLabel = createLabel();
        levelLabel = createLabel();
        timeLabel = createLabel();
        mbtiLabel = createLabel();
        bulletsLabel = createLabel();

        // 暂停按钮
        pauseButton = new JButton("⏸ PAUSE");
        pauseButton.setFont(INFO_FONT);
        pauseButton.setBackground(new Color(30, 30, 30));
        pauseButton.setForeground(TEXT_COLOR);
        pauseButton.setFocusPainted(false);
        pauseButton.setBorderPainted(false);
    }

    /**
     * 创建统一样式的标签
     */
    private JLabel createLabel() {
        JLabel label = new JLabel();
        label.setFont(INFO_FONT);
        label.setForeground(TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * 添加所有组件到面板
     */
    private void addComponents() {
        // 第一行
        add(scoreLabel);
        add(lengthLabel);
        add(levelLabel);
        add(timeLabel);

        // 第二行
        add(mbtiLabel);
        add(bulletsLabel);
        add(new JLabel());  // 空白占位
        add(pauseButton);
    }

    /**
     * 更新所有显示文本
     */
    private void updateDisplay() {
        scoreLabel.setText(String.format("分数: %d", score));
        lengthLabel.setText(String.format("长度: %d", snakeLength));
        levelLabel.setText(String.format("关卡: %d", level));
        timeLabel.setText(String.format("时间: %d:%02d", timeRemaining / 60, timeRemaining % 60));
        mbtiLabel.setText(String.format("MBTI: %s", mbtiType));
        bulletsLabel.setText(bulletsCount == 999 ? "子弹: ∞" : String.format("子弹: %d", bulletsCount));
    }

    // ==================== 公开更新方法 ====================

    public void setScore(int score) {
        this.score = score;
        updateDisplay();
    }

    public void setSnakeLength(int length) {
        this.snakeLength = length;
        updateDisplay();
    }

    public void setLength(int length) {
        setSnakeLength(length);
    }

    public void setLevel(int level) {
        this.level = level;
        updateDisplay();
    }

    public void setTimeRemaining(int seconds) {
        this.timeRemaining = seconds;
        updateDisplay();
    }

    public void setTime(int seconds) {
        setTimeRemaining(seconds);
    }

    public void setMbtiType(String mbtiType) {
        this.mbtiType = mbtiType;
        updateDisplay();
    }

    public void setBulletsCount(int count) {
        this.bulletsCount = count;
        updateDisplay();
    }

    public void setBulletCount(String displayText) {
        if ("∞".equals(displayText) || "\u221E".equals(displayText)) {
            this.bulletsCount = 999;
        } else {
            try {
                this.bulletsCount = Integer.parseInt(displayText);
            } catch (NumberFormatException e) {
                this.bulletsCount = 999;
            }
        }
        updateDisplay();
    }

    /**
     * 获取暂停按钮（供添加事件监听器）
     */
    public JButton getPauseButton() {
        return pauseButton;
    }

    /**
     * 切换暂停按钮文本
     */
    public void setPaused(boolean paused) {
        pauseButton.setText(paused ? "▶ RESUME" : "⏸ PAUSE");
    }
}
