package com.xylonet.snake.network;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/**
 * Python AI进程管理器
 * 负责启动、监控和关闭Python AI服务
 */
public class PythonProcessManager {
    private Process pythonProcess;
    private final String pythonScriptPath;
    private final String portFilePath;
    private Integer aiServicePort = null;

    public PythonProcessManager() {
        // 获取项目根目录
        String projectRoot = System.getProperty("user.dir");
        this.pythonScriptPath = projectRoot + "/python_ai/ai_service.py";
        this.portFilePath = projectRoot + "/data/ai_port.txt";
    }

    /**
     * 启动Python AI服务进程
     * @return 如果启动成功返回true
     */
    public boolean startPythonService() {
        try {
            System.out.println("[Java] 正在启动Python AI服务...");

            // 删除旧的端口文件
            File portFile = new File(portFilePath);
            if (portFile.exists()) {
                portFile.delete();
            }

            // 检测操作系统
            String os = System.getProperty("os.name").toLowerCase();
            String pythonCommand = getPythonCommand(os);

            // 启动Python进程
            ProcessBuilder processBuilder = new ProcessBuilder(pythonCommand, pythonScriptPath);
            processBuilder.redirectErrorStream(false);

            pythonProcess = processBuilder.start();

            // 在后台线程中打印Python输出
            startOutputReader(pythonProcess.getInputStream(), "PYTHON-OUT");
            startOutputReader(pythonProcess.getErrorStream(), "PYTHON-ERR");

            // 等待端口文件生成（最多等待5秒）
            if (waitForPortFile(5000)) {
                aiServicePort = readPortFromFile();
                System.out.println("[Java] Python AI服务启动成功，端口: " + aiServicePort);
                return true;
            } else {
                System.err.println("[Java] 等待Python服务启动超时");
                stopPythonService();
                return false;
            }

        } catch (IOException e) {
            System.err.println("[Java] 启动Python服务失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据操作系统获取Python命令
     */
    private String getPythonCommand(String os) {
        if (os.contains("win")) {
            return "python";  // Windows
        } else {
            return "python3";  // macOS/Linux
        }
    }

    /**
     * 等待端口文件生成
     */
    private boolean waitForPortFile(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        File portFile = new File(portFilePath);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (portFile.exists() && portFile.length() > 0) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 从文件读取端口号
     */
    private Integer readPortFromFile() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(portFilePath))).trim();
            return Integer.parseInt(content);
        } catch (IOException | NumberFormatException e) {
            System.err.println("[Java] 读取端口文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 启动输出读取线程
     */
    private void startOutputReader(InputStream inputStream, String prefix) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[" + prefix + "] " + line);
                }
            } catch (IOException e) {
                // 进程结束时会抛出异常，这是正常的
            }
        }).start();
    }

    /**
     * 停止Python AI服务
     */
    public void stopPythonService() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            System.out.println("[Java] 正在关闭Python AI服务...");

            pythonProcess.destroy();

            try {
                // 等待最多3秒
                if (!pythonProcess.waitFor(3, TimeUnit.SECONDS)) {
                    System.out.println("[Java] 强制终止Python进程");
                    pythonProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                pythonProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }

            System.out.println("[Java] Python AI服务已关闭");
        }

        // 清理端口文件
        try {
            File portFile = new File(portFilePath);
            if (portFile.exists()) {
                portFile.delete();
            }
        } catch (Exception e) {
            // 忽略清理错误
        }
    }

    /**
     * 获取AI服务端口号
     */
    public Integer getAiServicePort() {
        return aiServicePort;
    }

    /**
     * 检查Python进程是否运行
     */
    public boolean isPythonServiceRunning() {
        return pythonProcess != null && pythonProcess.isAlive();
    }
}
