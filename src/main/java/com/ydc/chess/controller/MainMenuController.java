package com.ydc.chess.controller;
import com.ydc.chess.ui.UIManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainMenuController {

    @FXML private Button localPlayButton;
    @FXML private Button networkPlayButton;
    @FXML private Button exitButton;
    @FXML private Button settingsButton;
    @FXML private Button recordsButton;

    @FXML
    public void onLocalPlayClicked() {
        // 跳转到游戏界面 (本地模式)
        // 注意：这里只是跳转界面，后续需要传递参数告诉 GameBoard 是本地模式
        UIManager.goTo("GameBoard.fxml", "本地对战");
    }

    @FXML
    public void onNetworkPlayClicked() {
        // 跳转到网络设置界面
        UIManager.goTo("NetworkSetup.fxml", "网络大厅");
    }

    @FXML
    public void onExitClicked() {
        // 退出程序
        System.exit(0);
    }

    @FXML
    public void onSettingsClicked() {
        System.out.println("功能: 打开设置界面 (暂未实现)");
    }

    @FXML
    public void onRecordsClicked() {
        System.out.println("功能: 查看对局记录 (暂未实现)");
    }
}