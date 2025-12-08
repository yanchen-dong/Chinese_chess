package com.ydc.chess;

import com.ydc.chess.ui.UIManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. 把舞台交给 UIManager 管理
        UIManager.setPrimaryStage(stage);

        // 2. 使用 UIManager 跳转到主菜单
        UIManager.goTo("MainMenu.fxml", "主菜单");
    }

    public static void main(String[] args) {
        launch();
    }
}