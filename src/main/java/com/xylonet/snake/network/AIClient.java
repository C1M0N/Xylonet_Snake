package com.xylonet.snake.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

/**
 * AI服务Socket客户端
 * 负责与Python AI服务进行双向通信
 */
public class AIClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final Gson gson;
    private final ExecutorService executorService;
    private boolean connected = false;

    public AIClient() {
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 连接到AI服务
     */
    public boolean connect(String host, int port) {
        try {
            System.out.println("[AIClient] 连接到 " + host + ":" + port);

            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            connected = true;
            System.out.println("[AIClient] 连接成功");

            // 发送初始心跳
            return sendPing();

        } catch (IOException e) {
            System.err.println("[AIClient] 连接失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送心跳检测
     */
    public boolean sendPing() {
        JsonObject message = new JsonObject();
        message.addProperty("type", "PING");
        message.addProperty("timestamp", System.currentTimeMillis());

        try {
            String response = sendAndReceive(message, 2000);
            if (response != null) {
                JsonObject responseObj = gson.fromJson(response, JsonObject.class);
                return "PONG".equals(responseObj.get("type").getAsString());
            }
        } catch (Exception e) {
            System.err.println("[AIClient] 心跳失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 发送游戏状态
     * @param gameState 游戏状态数据
     */
    public void sendGameState(Map<String, Object> gameState) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "GAME_STATE");
        message.addProperty("timestamp", System.currentTimeMillis());
        message.add("data", gson.toJsonTree(gameState));

        sendAsync(message);
    }

    /**
     * 请求玩家行为分析
     * @param dataPoints 已收集的数据点数量
     * @return 分析结果（包含MBTI等信息）
     */
    public CompletableFuture<AnalysisResult> requestAnalysis(int dataPoints) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject message = new JsonObject();
            message.addProperty("type", "REQUEST_ANALYSIS");
            message.addProperty("timestamp", System.currentTimeMillis());
            message.addProperty("data_points", dataPoints);

            try {
                String response = sendAndReceive(message, 5000);
                if (response != null) {
                    JsonObject responseObj = gson.fromJson(response, JsonObject.class);
                    return parseAnalysisResult(responseObj);
                }
            } catch (Exception e) {
                System.err.println("[AIClient] 请求分析失败: " + e.getMessage());
            }
            return null;
        }, executorService);
    }

    /**
     * 发送消息并等待响应
     */
    private String sendAndReceive(JsonObject message, long timeoutMs) throws IOException, TimeoutException {
        if (!connected) {
            throw new IOException("未连接到AI服务");
        }

        // 发送消息
        String jsonMessage = gson.toJson(message);
        writer.println(jsonMessage);

        // 等待响应
        Future<String> future = executorService.submit(() -> {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("等待响应超时");
        }
    }

    /**
     * 异步发送消息（不等待响应）
     */
    private void sendAsync(JsonObject message) {
        if (!connected) {
            System.err.println("[AIClient] 未连接到AI服务，无法发送消息");
            return;
        }

        executorService.submit(() -> {
            try {
                String jsonMessage = gson.toJson(message);
                writer.println(jsonMessage);
            } catch (Exception e) {
                System.err.println("[AIClient] 发送消息失败: " + e.getMessage());
            }
        });
    }

    /**
     * 解析分析结果
     */
    private AnalysisResult parseAnalysisResult(JsonObject response) {
        AnalysisResult result = new AnalysisResult();

        if (response.has("mbti")) {
            result.mbti = response.get("mbti").getAsString();
        }
        if (response.has("confidence")) {
            result.confidence = response.get("confidence").getAsDouble();
        }
        if (response.has("sample_size")) {
            result.sampleSize = response.get("sample_size").getAsInt();
        }
        if (response.has("traits")) {
            JsonObject traits = response.getAsJsonObject("traits");
            result.aggression = traits.has("aggression") ? traits.get("aggression").getAsDouble() : 0.0;
            result.caution = traits.has("caution") ? traits.get("caution").getAsDouble() : 0.0;
            result.exploration = traits.has("exploration") ? traits.get("exploration").getAsDouble() : 0.0;
        }

        return result;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        connected = false;

        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
            executorService.shutdown();
            System.out.println("[AIClient] 已断开连接");
        } catch (IOException e) {
            System.err.println("[AIClient] 断开连接时出错: " + e.getMessage());
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * 分析结果数据类
     */
    public static class AnalysisResult {
        public String mbti;
        public double confidence;
        public int sampleSize;
        public double aggression;
        public double caution;
        public double exploration;

        @Override
        public String toString() {
            return String.format("MBTI: %s (置信度: %.2f, 样本量: %d)\n特质 - 攻击性: %.2f, 谨慎度: %.2f, 探索性: %.2f",
                    mbti, confidence, sampleSize, aggression, caution, exploration);
        }
    }
}
