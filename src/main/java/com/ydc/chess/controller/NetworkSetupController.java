package com.ydc.chess.controller;

import com.ydc.chess.network.NetworkMessage;
import com.ydc.chess.network.NetworkService;
import com.ydc.chess.ui.DialogUtils;
import com.ydc.chess.ui.UIManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkSetupController {

    @FXML private Button createHostButton;
    @FXML private VBox hostStatusBox;
    @FXML private ListView<String> hostListView;
    @FXML private Button joinGameButton;
    @FXML private Button refreshButton;
    @FXML private Label hostIPLabel;
    @FXML private Label statusLabel;
    @FXML private TextField manualIPField;
    @FXML private Button manualConnectButton;
    
    private NetworkService networkService;
    private ExecutorService executorService;
    private String selectedHostIP;
    
    @FXML
    public void initialize() {
        networkService = new NetworkService();
        executorService = Executors.newCachedThreadPool();
        
        // 初始化界面
        hostListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("未找到可用主机")) {
                selectedHostIP = newVal;
                joinGameButton.setDisable(false);
            } else {
                joinGameButton.setDisable(true);
            }
        });
        
        // 在后台线程获取本地IP，避免阻塞UI
        executorService.submit(() -> {
            String localIP = NetworkService.getLocalIP();
            Platform.runLater(() -> {
                hostIPLabel.setText("本机IP: " + localIP);
            });
        });
        
        // 设置手动输入框的默认值（用于单机测试）
        manualIPField.setText("127.0.0.1");
        
        // 不自动搜索，让用户手动点击刷新按钮
        statusLabel.setText("点击刷新按钮搜索局域网内的主机，或手动输入IP");
    }
    
    @FXML
    public void onCreateHostClicked() {
        createHostButton.setDisable(true);
        hostStatusBox.setVisible(true);
        statusLabel.setText("正在创建服务器，等待连接...");
        
        executorService.submit(() -> {
            boolean success = networkService.startHost(NetworkService.DEFAULT_PORT);
            
            Platform.runLater(() -> {
                if (success) {
                    statusLabel.setText("服务器已启动，等待玩家连接...\n端口: " + NetworkService.DEFAULT_PORT);
                    
                    // 监听连接建立
                    networkService.addListener(new NetworkService.NetworkMessageListener() {
                        @Override
                        public void onMessageReceived(com.ydc.chess.network.NetworkMessage message) {
                            // 消息处理在GameBoardController中
                        }
                        
                        @Override
                        public void onConnectionEstablished() {
                            Platform.runLater(() -> {
                                statusLabel.setText("玩家已连接！");
                                // 设置网络服务到管理器
                                NetworkManager.setNetworkService(networkService);
                                // 延迟跳转，让用户看到连接成功
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(1000);
                                        Platform.runLater(() -> {
                                            UIManager.goTo("GameBoard.fxml", "网络对战 (主机)");
                                        });
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            });
                        }
                        
                        @Override
                        public void onConnectionLost() {
                            Platform.runLater(() -> {
                                statusLabel.setText("连接已断开");
                                createHostButton.setDisable(false);
                            });
                        }
                    });
                } else {
                    DialogUtils.showError("错误", "创建服务器失败，请检查端口是否被占用。");
                    hostStatusBox.setVisible(false);
                    createHostButton.setDisable(false);
                }
            });
        });
    }

    @FXML
    public void onJoinGameClicked() {
        if (selectedHostIP == null || selectedHostIP.isEmpty()) {
            DialogUtils.showInfo("提示", "请先选择一个主机");
            return;
        }
        
        joinGameButton.setDisable(true);
        statusLabel.setText("正在连接到 " + selectedHostIP + "...");
        
        // ⭐ 关键修复：在连接之前就添加监听器，避免错过 onConnectionEstablished 回调
        NetworkService.NetworkMessageListener listener = new NetworkService.NetworkMessageListener() {
            @Override
            public void onMessageReceived(com.ydc.chess.network.NetworkMessage message) {
                // 消息处理在GameBoardController中
            }
            
            @Override
            public void onConnectionEstablished() {
                Platform.runLater(() -> {
                    statusLabel.setText("连接成功！");
                    // 设置网络服务到管理器
                    NetworkManager.setNetworkService(networkService);
                    // 延迟跳转
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            Platform.runLater(() -> {
                                UIManager.goTo("GameBoard.fxml", "网络对战 (客机)");
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
            }
            
            @Override
            public void onConnectionLost() {
                Platform.runLater(() -> {
                    DialogUtils.showError("连接错误", "与主机的连接已断开");
                    joinGameButton.setDisable(false);
                });
            }
        };
        
        networkService.addListener(listener);
        
        executorService.submit(() -> {
            boolean success = networkService.connectToHost(selectedHostIP, NetworkService.DEFAULT_PORT);
            
            Platform.runLater(() -> {
                if (!success) {
                    // 连接失败，移除监听器
                    networkService.removeListener(listener);
                    DialogUtils.showError("连接失败", "无法连接到主机，请检查网络连接。");
                    joinGameButton.setDisable(false);
                }
                // 如果成功，监听器会通过 onConnectionEstablished 回调处理
            });
        });
    }
    
    @FXML
    public void onRefreshClicked() {
        refreshHostList();
    }
    
    private void refreshHostList() {
        hostListView.getItems().clear();
        statusLabel.setText("正在搜索局域网内的主机...");
        refreshButton.setDisable(true);
        joinGameButton.setDisable(true);
        
        executorService.submit(() -> {
            // 使用较短的超时时间，快速响应
            List<String> hosts = NetworkService.discoverHosts(200);
            
            Platform.runLater(() -> {
                if (hosts.isEmpty()) {
                    hostListView.getItems().add("未找到可用主机");
                    statusLabel.setText("未找到可用主机，请确保主机已创建游戏并点击刷新");
                } else {
                    hostListView.getItems().addAll(hosts);
                    statusLabel.setText("找到 " + hosts.size() + " 个可用主机，请选择要加入的游戏");
                }
                refreshButton.setDisable(false);
            });
        });
    }

    @FXML
    public void onManualConnectClicked() {
        String ip = manualIPField.getText().trim();
        if (ip.isEmpty()) {
            DialogUtils.showInfo("提示", "请输入IP地址（例如：127.0.0.1）");
            return;
        }
        
        // 支持localhost
        if (ip.equalsIgnoreCase("localhost")) {
            ip = "127.0.0.1";
        }
        
        manualConnectButton.setDisable(true);
        statusLabel.setText("正在连接到 " + ip + "...");

        String finalIp = ip;
        
        // ⭐ 关键修复：在连接之前就添加监听器，避免错过 onConnectionEstablished 回调
        NetworkService.NetworkMessageListener listener = new NetworkService.NetworkMessageListener() {
            @Override
            public void onMessageReceived(com.ydc.chess.network.NetworkMessage message) {
                // 消息处理在GameBoardController中
            }
            
            @Override
            public void onConnectionEstablished() {
                Platform.runLater(() -> {
                    statusLabel.setText("连接成功！");
                    // 设置网络服务到管理器
                    NetworkManager.setNetworkService(networkService);
                    // 延迟跳转
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            Platform.runLater(() -> {
                                UIManager.goTo("GameBoard.fxml", "网络对战 (客机)");
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
            }
            
            @Override
            public void onConnectionLost() {
                Platform.runLater(() -> {
                    DialogUtils.showError("连接错误", "与主机的连接已断开");
                    manualConnectButton.setDisable(false);
                });
            }
        };
        
        networkService.addListener(listener);
        
        executorService.submit(() -> {
            boolean success = networkService.connectToHost(finalIp, NetworkService.DEFAULT_PORT);
            
            Platform.runLater(() -> {
                if (!success) {
                    // 连接失败，移除监听器
                    networkService.removeListener(listener);
                    DialogUtils.showError("连接失败", "无法连接到主机 " + finalIp + "，请检查：\n1. 主机是否已创建游戏\n2. IP地址是否正确\n3. 防火墙设置");
                    manualConnectButton.setDisable(false);
                }
                // 如果成功，监听器会通过 onConnectionEstablished 回调处理
            });
        });
    }
    
    @FXML
    public void onBackClicked() {
        if (networkService != null && networkService.isConnected()) {
            networkService.disconnect();
        }
        NetworkManager.clear();
        UIManager.goTo("MainMenu.fxml", "主菜单");
    }
    
    public NetworkService getNetworkService() {
        return networkService;
    }
}