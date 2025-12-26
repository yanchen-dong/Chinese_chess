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
    private ServerSocket discoveryServerSocket; // 发现服务端口
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
            // 启动游戏服务器
            serverSocket = new ServerSocket(port);
            System.out.println("服务器已启动，等待客户端连接... 端口: " + port);
            
            // 启动发现服务（在独立端口）
            startDiscoveryService();
            
            executorService.submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("客户端已连接: " + clientSocket.getRemoteSocketAddress());
                    
                    // 由于搜索功能现在使用独立的8889端口，连接到8888端口的都是正式连接
                    handleClientConnection();
                    
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
     * 处理客户端连接
     */
    private void handleClientConnection() {
        try {
            setupStreams();
            isConnected = true;
            
            Platform.runLater(() -> {
                for (NetworkMessageListener listener : listeners) {
                    listener.onConnectionEstablished();
                }
            });
            
            startListening();
        } catch (IOException e) {
            System.err.println("设置连接流失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动发现服务（在8889端口）
     */
    private void startDiscoveryService() {
        executorService.submit(() -> {
            try {
                discoveryServerSocket = new ServerSocket(BROADCAST_PORT);
                System.out.println("发现服务已启动，端口: " + BROADCAST_PORT);
                
                while (!discoveryServerSocket.isClosed()) {
                    try {
                        Socket discoverySocket = discoveryServerSocket.accept();
                        // 处理发现请求
                        executorService.submit(() -> {
                            try {
                                BufferedReader disIn = new BufferedReader(
                                    new InputStreamReader(discoverySocket.getInputStream()));
                                PrintWriter disOut = new PrintWriter(
                                    discoverySocket.getOutputStream(), true);
                                
                                // 读取发现请求
                                String request = disIn.readLine();
                                if ("DISCOVER".equals(request)) {
                                    // 回复发现响应
                                    disOut.println("HOST_FOUND");
                                    System.out.println("收到发现请求，已回复: " + 
                                        discoverySocket.getRemoteSocketAddress());
                                }
                                
                                discoverySocket.close();
                            } catch (IOException e) {
                                // 忽略发现连接的异常
                            }
                        });
                    } catch (IOException e) {
                        if (!discoveryServerSocket.isClosed()) {
                            System.err.println("发现服务接受连接失败: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("启动发现服务失败: " + e.getMessage());
            }
        });
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
            if (discoveryServerSocket != null && !discoveryServerSocket.isClosed()) {
                discoveryServerSocket.close();
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
     * 使用独立的发现端口（8889）进行搜索，避免干扰游戏连接
     */
    public static List<String> discoverHosts(int timeoutMs) {
        String localIP = getLocalIP();
        String baseIP = localIP.substring(0, localIP.lastIndexOf('.') + 1);
        
        // 限制并发线程数，避免过多线程导致系统资源耗尽
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        // 使用同步列表，确保线程安全
        List<String> synchronizedHosts = Collections.synchronizedList(new ArrayList<>());
        
        // 总是检查 127.0.0.1 和本地IP
        futures.add(executor.submit(() -> {
            if (checkHostDiscovery("127.0.0.1", timeoutMs)) {
                synchronized (synchronizedHosts) {
                    if (!synchronizedHosts.contains("127.0.0.1")) {
                        synchronizedHosts.add("127.0.0.1");
                    }
                }
                return true;
            }
            return false;
        }));
        
        if (!"127.0.0.1".equals(localIP)) {
            futures.add(executor.submit(() -> {
                if (checkHostDiscovery(localIP, timeoutMs)) {
                    synchronized (synchronizedHosts) {
                        if (!synchronizedHosts.contains(localIP)) {
                            synchronizedHosts.add(localIP);
                        }
                    }
                    return true;
                }
                return false;
            }));
        }
        
        // 扫描局域网内的其他IP
        for (int i = 1; i <= 254; i++) {
            final String ip = baseIP + i;
            if (ip.equals(localIP) || ip.equals("127.0.0.1")) continue;
            
            Future<Boolean> future = executor.submit(() -> {
                if (checkHostDiscovery(ip, timeoutMs)) {
                    synchronized (synchronizedHosts) {
                        synchronizedHosts.add(ip);
                    }
                    return true;
                }
                return false;
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
    
    /**
     * 检查指定IP是否有主机在运行（通过发现端口）
     */
    private static boolean checkHostDiscovery(String ip, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, BROADCAST_PORT), Math.min(timeoutMs, 200));
            
            // 发送发现请求
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println("DISCOVER");
            
            // 读取响应
            String response = in.readLine();
            if ("HOST_FOUND".equals(response)) {
                return true;
            }
            
            socket.close();
            return false;
        } catch (Exception e) {
            return false;
        }
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

