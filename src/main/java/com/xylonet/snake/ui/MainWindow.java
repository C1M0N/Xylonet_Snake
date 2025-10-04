package com.xylonet.snake.ui;

import com.xylonet.snake.game.Bullet;
import com.xylonet.snake.game.GameEngine;
import com.xylonet.snake.game.Snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 主游戏窗口 1200x900 分辨率，黑底 ASCII 风格
 */
public class MainWindow extends JFrame {

  private static final int WINDOW_WIDTH = 1200;
  private static final int WINDOW_HEIGHT = 900;

  private GamePanel gamePanel;
  private ProgressBarPanel progressBarPanel;
  private InfoPanel infoPanel;
  private ConsolePanel consolePanel;
  private GameEngine gameEngine;

  public MainWindow() {
    initializeWindow();
    initializeComponents();
    layoutComponents();
    setupKeyboardInput();
    setupConsoleCommands();
    startGame();
  }

  /**
   * 初始化窗口基本设置
   */
  private void initializeWindow() {
    setTitle("Xylonet Snake - ASCII Edition");
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    setLocationRelativeTo(null);  // 居中显示

    // 设置黑色背景
    getContentPane().setBackground(Color.BLACK);
  }

  /**
   * 初始化各个面板组件
   */
  private void initializeComponents() {
    infoPanel = new InfoPanel();
    gamePanel = new GamePanel();
    progressBarPanel = new ProgressBarPanel();
    consolePanel = new ConsolePanel();

    // 创建游戏引擎
    gameEngine = new GameEngine(gamePanel, infoPanel, progressBarPanel, consolePanel);
  }

  /**
   * 布局所有组件
   * <p>
   * 布局结构： ┌───────────────────┬─────────────┐ │                   │             │ │
   *   │  InfoPanel  │ │   GamePanel       │  (右上)     │ │   (游戏区域)       ├─────────────┤ │
   *          │             │ │                   │ Console     │ │                   │ Panel
   * │ └───────────────────┴─────────────┘
   */
  private void layoutComponents() {
    setLayout(new BorderLayout());

    // 左侧：游戏区域（不加边框）
    add(gamePanel, BorderLayout.CENTER);

    // 创建垂直分隔符面板（用 ║ 双线字符）
    JPanel verticalSeparator = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(120, 120, 120));
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        for (int y = 0; y < getHeight(); y += 15) {
          g.drawString("║", 2, y + 13);
        }
      }
    };
    verticalSeparator.setPreferredSize(new Dimension(18, 0));
    verticalSeparator.setBackground(Color.BLACK);

    // 右侧：信息面板 + 控制台面板
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BorderLayout());
    rightPanel.setBackground(Color.BLACK);

    // 创建水平分隔符面板（用 ═ 双线字符）
    JPanel horizontalSeparator = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(120, 120, 120));
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        for (int x = 0; x < getWidth(); x += 9) {
          g.drawString("═", x, 12);
        }
      }
    };
    horizontalSeparator.setPreferredSize(new Dimension(0, 18));
    horizontalSeparator.setBackground(Color.BLACK);

    // 恢复 InfoPanel 原有 padding
    infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // 创建包含 InfoPanel 和分隔符的容器
    JPanel topContainer = new JPanel(new BorderLayout());
    topContainer.setBackground(Color.BLACK);
    topContainer.add(infoPanel, BorderLayout.CENTER);
    topContainer.add(horizontalSeparator, BorderLayout.SOUTH);

    rightPanel.add(topContainer, BorderLayout.NORTH);
    rightPanel.add(consolePanel, BorderLayout.CENTER);

    // 创建顶部进度条容器（进度条 + 下方分隔线）
    JPanel topProgressContainer = new JPanel(new BorderLayout());
    topProgressContainer.setBackground(Color.BLACK);
    topProgressContainer.add(progressBarPanel, BorderLayout.CENTER);

    // 创建进度条下方的水平分隔线
    JPanel progressSeparator = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(120, 120, 120));
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        for (int x = 0; x < getWidth(); x += 9) {
          g.drawString("═", x, 12);
        }
      }
    };
    progressSeparator.setPreferredSize(new Dimension(0, 18));
    progressSeparator.setBackground(Color.BLACK);
    topProgressContainer.add(progressSeparator, BorderLayout.SOUTH);

    // 主游戏区域（游戏面板 + 垂直分隔符）
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(Color.BLACK);
    mainPanel.add(gamePanel, BorderLayout.CENTER);
    mainPanel.add(verticalSeparator, BorderLayout.EAST);

    // 左侧区域（进度条 + 游戏面板）
    JPanel leftArea = new JPanel(new BorderLayout());
    leftArea.setBackground(Color.BLACK);
    leftArea.add(topProgressContainer, BorderLayout.NORTH);
    leftArea.add(mainPanel, BorderLayout.CENTER);

    add(leftArea, BorderLayout.CENTER);
    add(rightPanel, BorderLayout.EAST);
  }

  /**
   * 获取游戏面板（供游戏引擎使用）
   */
  public GamePanel getGamePanel() {
    return gamePanel;
  }

  /**
   * 获取信息面板（供更新显示数据）
   */
  public InfoPanel getInfoPanel() {
    return infoPanel;
  }

  /**
   * 获取控制台面板（供命令处理）
   */
  public ConsolePanel getConsolePanel() {
    return consolePanel;
  }

  /**
   * 设置键盘输入监听
   */
  private void setupKeyboardInput() {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        handleKeyPress(e);
      }

      @Override
      public void keyReleased(KeyEvent e) {
        handleKeyRelease(e);
      }
    });

    // 确保窗口可以接收键盘事件
    setFocusable(true);
    requestFocusInWindow();
  }

  /**
   * 处理键盘按键
   */
  private void handleKeyPress(KeyEvent e) {
    int key = e.getKeyCode();

    // T 键 - 聚焦到 Console
    if (key == KeyEvent.VK_T) {
      consolePanel.focusInput();
      return;
    }

    // 如果焦点在 Console 输入框，不处理游戏按键
    if (consolePanel.getInputField().isFocusOwner()) {
      return;
    }

    // Shift 键 - 开始冲刺
    if (key == KeyEvent.VK_SHIFT) {
      gameEngine.setSprinting(true);
      return;
    }

    // WASD - 移动蛇
    switch (key) {
      case KeyEvent.VK_W:
        gameEngine.moveSnake(Snake.Direction.UP);
        break;
      case KeyEvent.VK_S:
        gameEngine.moveSnake(Snake.Direction.DOWN);
        break;
      case KeyEvent.VK_A:
        gameEngine.moveSnake(Snake.Direction.LEFT);
        break;
      case KeyEvent.VK_D:
        gameEngine.moveSnake(Snake.Direction.RIGHT);
        break;

      // 方向键 - 射击
      case KeyEvent.VK_UP:
        gameEngine.shoot(Bullet.Direction.UP);
        break;
      case KeyEvent.VK_DOWN:
        gameEngine.shoot(Bullet.Direction.DOWN);
        break;
      case KeyEvent.VK_LEFT:
        gameEngine.shoot(Bullet.Direction.LEFT);
        break;
      case KeyEvent.VK_RIGHT:
        gameEngine.shoot(Bullet.Direction.RIGHT);
        break;

      // 空格 - 开始/暂停
      case KeyEvent.VK_SPACE:
        if (gameEngine.getState() == GameEngine.GameState.READY ||
            gameEngine.getState() == GameEngine.GameState.GAME_OVER ||
            gameEngine.getState() == GameEngine.GameState.LEVEL_COMPLETE) {
          gameEngine.start();
        } else {
          gameEngine.togglePause();
        }
        break;

      // P 或 ESC - 暂停
      case KeyEvent.VK_P:
      case KeyEvent.VK_ESCAPE:
        gameEngine.togglePause();
        break;

      // R - 重新开始
      case KeyEvent.VK_R:
        gameEngine.restart();
        break;
    }
  }

  /**
   * 处理键盘释放
   */
  private void handleKeyRelease(KeyEvent e) {
    int key = e.getKeyCode();

    // Shift 键释放 - 停止冲刺
    if (key == KeyEvent.VK_SHIFT) {
      gameEngine.setSprinting(false);
    }
  }

  /**
   * 设置 Console 命令处理
   */
  private void setupConsoleCommands() {
    consolePanel.setCommandHandler(command -> {
      // 移除开头的 /
      String cmd = command.substring(1).toLowerCase();
      String[] parts = cmd.split("\\s+");

      boolean handled = false;

      switch (parts[0]) {
        case "snake":
          handled = true;
          if (parts.length >= 2 && parts[1].equals("speed")) {
            if (parts.length >= 3) {
              if (parts[2].equals("info")) {
                // 显示当前速度信息
                int level = gameEngine.getSpeedLevel();
                int delay = gameEngine.getSnakeSpeed();
                consolePanel.addMessage("当前速度等级: " + level, ConsolePanel.MessageType.INFO);
                consolePanel.addMessage("当前延迟: " + delay + "ms", ConsolePanel.MessageType.INFO);
                consolePanel.addMessage("公式: y = -10ln(x-1) + 70 (等级1固定100ms)",
                    ConsolePanel.MessageType.INFO);
              } else {
                try {
                  int level = Integer.parseInt(parts[2]);
                  gameEngine.setSpeedLevel(level);
                  int delay = gameEngine.getSnakeSpeed();
                  consolePanel.addMessage("速度等级已设置为: " + level + " (延迟: " + delay + "ms)",
                      ConsolePanel.MessageType.SYSTEM);
                } catch (NumberFormatException e) {
                  consolePanel.addMessage("无效的速度等级: " + parts[2],
                      ConsolePanel.MessageType.ERROR);
                }
              }
            } else {
              // 重置为默认（等级 1）
              gameEngine.setSpeedLevel(1);
              consolePanel.addMessage("蛇速度已重置为默认值 (等级1, 100ms)",
                  ConsolePanel.MessageType.SYSTEM);
            }
          } else {
            consolePanel.addMessage("用法: /snake speed [等级] 或 /snake speed info",
                ConsolePanel.MessageType.INFO);
          }
          break;
      }

      // 如果未处理，调用默认处理器
      if (!handled) {
        consolePanel.handleDefaultCommand(command);
      }
    });
  }

  /**
   * 启动游戏
   */
  private void startGame() {
    // 游戏在按空格后才开始
    consolePanel.addMessage("Press SPACE to start the game", ConsolePanel.MessageType.SYSTEM);
    consolePanel.addMessage("Controls: WASD = Move, Arrow Keys = Shoot, P = Pause, T = Console",
        ConsolePanel.MessageType.INFO);
    consolePanel.addMessage("Commands: /snake speed [ms], /help", ConsolePanel.MessageType.INFO);
  }

  /**
   * 显示窗口
   */
  public void display() {
    setVisible(true);
  }

  /**
   * 测试主窗口
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      MainWindow window = new MainWindow();
      window.display();
    });
  }
}
