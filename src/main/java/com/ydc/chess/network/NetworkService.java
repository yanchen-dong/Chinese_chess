package com.ydc.chess.network;

import com.google.gson.Gson;
import com.ydc.chess.model.Pos;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 网络服务类
 * 负责处理P2P网络通信
 */
public class NetworkService {
    public static final int DEFAULT_PORT = 8888;
    private static final int BROADCAST_PORT = 8889;
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isHost = false;
    private boolean isConnected = false;
    private int port = DEFAULT_PORT;
    
    private ExecutorService executorService;
    private List<NetworkMessageListener> listeners = new ArrayList<>();
    private Gson gson = new Gson();
    
    public interface NetworkMessageListener {
        void onMessageReceived(NetworkMessage message);
        void onConnectionEstablished();
        void onConnectionLost();
    }
    
    public NetworkService() {
        executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * 作为主机创建服务器
     */
    public boolean startHost(int port) {
        this.port = port;
        this.isHost = true;
        
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器已启动，等待客户端连接... 端口: " + port);
            
            executorService.submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("客户端已连接: " + clientSocket.getRemoteSocketAddress());
                    
                    setupStreams();
                    isConnected = true;
                    
                    Platform.runLater(() -> {
                        for (NetworkMessageListener listener : listeners) {
                            listener.onConnectionEstablished();
                        }
                    });
                    
                    startListening();
                    
                } catch (IOException e) {
                    System.err.println("接受连接失败: " + e.getMessage());
                    Platform.runLater(() -> {
                        for (NetworkMessageListener listener : listeners) {
                            listener.onConnectionLost();
                        }
                    });
                }
            });
            
            return true;
        } catch (IOException e) {
            System.err.println("启动服务器失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 作为客户端连接服务器
     */
    public boolean connectToHost(String hostAddress, int port) {
        this.port = port;
        this.isHost = false;
        
        try {
            clientSocket = new Socket(hostAddress, port);
            System.out.println("已连接到服务器: " + hostAddress + ":" + port);
            
            setupStreams();
            isConnected = true;
            
            Platform.runLater(() -> {
                for (NetworkMessageListener listener : listeners) {
                    listener.onConnectionEstablished();
                }
            });
            
            startListening();
            
            return true;
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 设置输入输出流
     */
    private void setupStreams() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
    
    /**
     * 开始监听消息
     */
    private void startListening() {
        executorService.submit(() -> {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null && isConnected) {
                    NetworkMessage message = gson.fromJson(inputLine, NetworkMessage.class);
                    
                    Platform.runLater(() -> {
                        for (NetworkMessageListener listener : listeners) {
                            listener.onMessageReceived(message);
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("读取消息失败: " + e.getMessage());
            } finally {
                Platform.runLater(() -> {
                    for (NetworkMessageListener listener : listeners) {
                        listener.onConnectionLost();
                    }
                });
                isConnected = false;
            }
        });
    }
    
    /**
     * 发送消息
     */
    public boolean sendMessage(NetworkMessage message) {
        if (!isConnected || out == null) {
            return false;
        }
        
        try {
            String json = gson.toJson(message);
            out.println(json);
            return true;
        } catch (Exception e) {
            System.err.println("发送消息失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送移动消息
     */
    public boolean sendMove(Pos from, Pos to) {
        NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.MOVE);
        message.setFromPos(from);
        message.setToPos(to);
        return sendMessage(message);
    }
    
    /**
     * 发送悔棋请求
     */
    public boolean sendRegret() {
        NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.REGRET);
        return sendMessage(message);
    }
    
    /**
     * 发送求和请求
     */
    public boolean sendDraw() {
        NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.DRAW);
        return sendMessage(message);
    }
    
    /**
     * 发送认输
     */
    public boolean sendSurrender() {
        NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.SURRENDER);
        return sendMessage(message);
    }
    
    /**
     * 添加消息监听器
     */
    public void addListener(NetworkMessageListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 移除消息监听器
     */
    public void removeListener(NetworkMessageListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        isConnected = false;
        
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("断开连接时出错: " + e.getMessage());
        }
    }
    
    /**
     * 获取本地IP地址
     */
    public static String getLocalIP() {
        try {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("8.8.8.8", 80));
                return socket.getLocalAddress().getHostAddress();
            }
        } catch (Exception e) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                return "127.0.0.1";
            }
        }
    }
    
    /**
     * 搜索局域网内的主机
     * 优化：使用更短的超时时间，限制并发数，提前返回
     */
    public static List<String> discoverHosts(int timeoutMs) {
        List<String> hosts = new ArrayList<>();
        String localIP = getLocalIP();
        String baseIP = localIP.substring(0, localIP.lastIndexOf('.') + 1);
        
        // 限制并发线程数，避免过多线程导致系统资源耗尽
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        // 使用同步列表，确保线程安全
        List<String> synchronizedHosts = new ArrayList<>();
        
        for (int i = 1; i <= 254; i++) {
            final String ip = baseIP + i;
            if (ip.equals(localIP)) continue;
            
            Future<Boolean> future = executor.submit(() -> {
                try (Socket socket = new Socket()) {
                    // 使用更短的超时时间，快速失败
                    socket.connect(new InetSocketAddress(ip, DEFAULT_PORT), Math.min(timeoutMs, 200));
                    synchronized (synchronizedHosts) {
                        synchronizedHosts.add(ip);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
            futures.add(future);
        }
        
        // 等待所有任务完成，但设置总超时时间
        long startTime = System.currentTimeMillis();
        long maxWaitTime = 3000; // 最多等待3秒
        
        for (Future<Boolean> future : futures) {
            try {
                // 如果已经等待超过最大时间，取消剩余任务
                if (System.currentTimeMillis() - startTime > maxWaitTime) {
                    future.cancel(true);
                    continue;
                }
                future.get(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                // 忽略异常和超时
            }
        }
        
        executor.shutdownNow();
        return synchronizedHosts;
    }
    
    // Getters
    public boolean isHost() {
        return isHost;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public int getPort() {
        return port;
    }
}

