package com.ydc.chess.controller;

import com.ydc.chess.ui.UIManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class NetworkSetupController {

    @FXML private Button createHostButton;
    @FXML private VBox hostStatusBox;
    @FXML private ListView<String> hostListView;
    @FXML private Button joinGameButton;

    @FXML
    public void onCreateHostClicked() {
        // TODO: 这里以后要写 启动服务器 的逻辑
        // 暂时先模拟跳转进游戏
        System.out.println("主机建立，准备进入游戏...");
        UIManager.goTo("GameBoard.fxml", "网络对战 (主机)");
    }

    @FXML
    public void onJoinGameClicked() {
        // TODO: 这里以后要写 连接服务器 的逻辑
        System.out.println("连接成功，准备进入游戏...");
        UIManager.goTo("GameBoard.fxml", "网络对战 (客机)");
    }

    @FXML
    public void onBackClicked() {
        // 返回主菜单
        UIManager.goTo("MainMenu.fxml", "主菜单");
    }
}