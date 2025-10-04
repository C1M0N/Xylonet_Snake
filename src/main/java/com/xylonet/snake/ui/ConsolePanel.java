package com.xylonet.snake.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 控制台面板（右下） 可输入命令、显示系统消息、AI 提问 带历史记录功能（上下键翻看）
 */
public class ConsolePanel extends JPanel {

  private static final int PANEL_WIDTH = 400;  // 右侧面板宽度
  private static final Color BG_COLOR = Color.BLACK;
  private static final Color TEXT_COLOR = Color.WHITE;  // 白色文字
  private static final Font CONSOLE_FONT = new Font("Monospaced", Font.PLAIN, 12);

  private JTextPane outputArea;    // 输出区域（显示消息）
  private JTextField inputField;   // 输入框（输入命令）
  private StyledDocument document;

  // 命令历史记录
  private List<String> commandHistory = new ArrayList<>();
  private int historyIndex = -1;

  // 命令处理回调
  private CommandHandler commandHandler;

  public ConsolePanel() {
    setPreferredSize(new Dimension(PANEL_WIDTH, 0));  // 高度自适应
    setBackground(BG_COLOR);
    setLayout(new BorderLayout());

    initializeComponents();
    addComponents();
    setupInputHandler();

    // 显示欢迎消息
    printMessage("=== Xylonet Snake Console ===", new Color(100, 150, 255));
    printMessage("输入 'help' 查看可用命令", TEXT_COLOR);
  }

  /**
   * 初始化组件
   */
  private void initializeComponents() {
    // 输出区域（不可编辑）
    outputArea = new JTextPane();
    outputArea.setEditable(false);
    outputArea.setBackground(BG_COLOR);
    outputArea.setForeground(TEXT_COLOR);
    outputArea.setFont(CONSOLE_FONT);
    outputArea.setCaretColor(TEXT_COLOR);
    document = outputArea.getStyledDocument();

    // 输入框
    inputField = new JTextField();
    inputField.setBackground(BG_COLOR);
    inputField.setForeground(TEXT_COLOR);
    inputField.setFont(CONSOLE_FONT);
    inputField.setCaretColor(TEXT_COLOR);
    inputField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(80, 80, 80)),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));
  }

  /**
   * 添加组件到面板
   */
  private void addComponents() {
    // 输出区域放在滚动窗格中
    JScrollPane scrollPane = new JScrollPane(outputArea);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // 设置滚动条样式（暗色主题）
    scrollPane.getVerticalScrollBar().setBackground(new Color(30, 30, 30));
    scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
      @Override
      protected void configureScrollBarColors() {
        this.thumbColor = new Color(80, 80, 80);
        this.trackColor = new Color(30, 30, 30);
      }
    });

    add(scrollPane, BorderLayout.CENTER);
    add(inputField, BorderLayout.SOUTH);
  }

  /**
   * 设置输入处理（支持历史记录）
   */
  private void setupInputHandler() {
    // 回车键提交命令
    inputField.addActionListener(e -> {
      String command = inputField.getText().trim();
      if (!command.isEmpty()) {
        // 检查命令是否以 / 开头
        if (!command.startsWith("/")) {
          printError("命令必须以 / 开头，例如: /help");
          inputField.setText("");
          return;
        }

        // 添加到历史记录
        commandHistory.add(command);
        historyIndex = commandHistory.size();

        // 显示用户输入
        printMessage("> " + command, new Color(200, 200, 200));

        // 处理命令
        if (commandHandler != null) {
          commandHandler.handleCommand(command);
        } else {
          // 默认处理
          handleDefaultCommand(command);
        }

        // 清空输入框
        inputField.setText("");
      }
    });

    // 上下键翻看历史，ESC 键失去焦点
    inputField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          // ESC 键：失去焦点，返回游戏
          // 获取顶层窗口并将焦点还给它
          java.awt.Window window = SwingUtilities.getWindowAncestor(inputField);
          if (window != null) {
            window.requestFocusInWindow();
          }
          e.consume();
          return;
        }

        if (commandHistory.isEmpty()) {
          return;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
          // 向上翻历史
          if (historyIndex > 0) {
            historyIndex--;
            inputField.setText(commandHistory.get(historyIndex));
          }
          e.consume();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          // 向下翻历史
          if (historyIndex < commandHistory.size() - 1) {
            historyIndex++;
            inputField.setText(commandHistory.get(historyIndex));
          } else if (historyIndex == commandHistory.size() - 1) {
            historyIndex = commandHistory.size();
            inputField.setText("");
          }
          e.consume();
        }
      }
    });
  }

  /**
   * 默认命令处理
   */
  public void handleDefaultCommand(String command) {
    // 移除开头的 /
    String cmd = command.substring(1).toLowerCase();

    switch (cmd) {
      case "help":
        printMessage("可用命令:", TEXT_COLOR);
        printMessage("  /help     - 显示帮助", TEXT_COLOR);
        printMessage("  /clear    - 清空控制台", TEXT_COLOR);
        printMessage("  /status   - 显示游戏状态", TEXT_COLOR);
        printMessage("  /pause    - 暂停/继续游戏", TEXT_COLOR);
        printMessage("  /history  - 查看命令历史", TEXT_COLOR);
        break;

      case "clear":
        clearConsole();
        break;

      case "status":
        printMessage("游戏运行中...", TEXT_COLOR);
        break;

      case "pause":
        printMessage("请使用暂停按钮或 P 键", TEXT_COLOR);
        break;

      case "history":
        if (commandHistory.isEmpty()) {
          printMessage("暂无历史记录", TEXT_COLOR);
        } else {
          printMessage("命令历史:", TEXT_COLOR);
          for (int i = 0; i < commandHistory.size(); i++) {
            printMessage(String.format("  %d. %s", i + 1, commandHistory.get(i)), TEXT_COLOR);
          }
        }
        break;

      default:
        printMessage("未知命令: " + command, new Color(255, 100, 100));
        printMessage("输入 '/help' 查看可用命令", TEXT_COLOR);
    }
  }

  /**
   * 消息类型枚举
   */
  public enum MessageType {
    NORMAL(Color.WHITE),
    SYSTEM(new Color(100, 150, 255)),
    INFO(new Color(200, 200, 200)),
    WARNING(new Color(255, 200, 0)),
    ERROR(new Color(255, 80, 80)),
    AI(new Color(255, 165, 0));

    public final Color color;

    MessageType(Color color) {
      this.color = color;
    }
  }

  /**
   * 添加带类型的消息
   */
  public void addMessage(String message, MessageType type) {
    printMessage(message, type.color);
  }

  /**
   * 打印消息到控制台
   */
  public void printMessage(String message, Color color) {
    try {
      Style style = outputArea.addStyle("Style", null);
      StyleConstants.setForeground(style, color);
      // 设置行间距（稍微宽一点）
      StyleConstants.setLineSpacing(style, 0.3f);  // 30% 额外行距

      document.insertString(document.getLength(), message + "\n", style);

      // 自动滚动到底部
      outputArea.setCaretPosition(document.getLength());

    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * 打印消息（默认白色）
   */
  public void printMessage(String message) {
    printMessage(message, TEXT_COLOR);
  }

  /**
   * 打印错误消息（红色）
   */
  public void printError(String message) {
    printMessage("[ERROR] " + message, new Color(255, 80, 80));
  }

  /**
   * 打印警告消息（黄色）
   */
  public void printWarning(String message) {
    printMessage("[WARNING] " + message, new Color(255, 200, 0));
  }

  /**
   * 打印系统消息（蓝色）
   */
  public void printSystem(String message) {
    printMessage("[SYSTEM] " + message, new Color(100, 150, 255));
  }

  /**
   * AI 提问（用于互动）
   */
  public void askQuestion(String question, QuestionHandler handler) {
    printMessage("[AI] " + question, new Color(255, 165, 0));  // 橙色

    // 临时更换输入处理器
    ActionListener[] listeners = inputField.getActionListeners();
    for (ActionListener listener : listeners) {
      inputField.removeActionListener(listener);
    }

    inputField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String answer = inputField.getText().trim();
        if (!answer.isEmpty()) {
          printMessage("> " + answer, new Color(200, 200, 200));
          handler.handleAnswer(answer);
          inputField.setText("");

          // 恢复原有监听器
          inputField.removeActionListener(this);
          for (ActionListener listener : listeners) {
            inputField.addActionListener(listener);
          }
        }
      }
    });
  }

  /**
   * 清空控制台
   */
  public void clearConsole() {
    try {
      document.remove(0, document.getLength());
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * 设置命令处理器
   */
  public void setCommandHandler(CommandHandler handler) {
    this.commandHandler = handler;
  }

  /**
   * 聚焦到输入框（按 T 键时调用）
   */
  public void focusInput() {
    inputField.requestFocusInWindow();
  }

  /**
   * 获取输入框（供外部判断焦点）
   */
  public JTextField getInputField() {
    return inputField;
  }

  /**
   * 命令处理接口
   */
  public interface CommandHandler {

    void handleCommand(String command);
  }

  /**
   * 问题回答接口
   */
  public interface QuestionHandler {

    void handleAnswer(String answer);
  }
}
