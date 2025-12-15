package com.ydc.chess.ui;
import com.ydc.chess.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * 界面管理器
 * 负责加载 FXML 文件并切换窗口场景
 */
public class UIManager {
    // 保持对主窗口（舞台）的引用
    private static Stage primaryStage;
    // 初始化方法，在程序启动时调用一次
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    /**
     * 通用的界面跳转方法
     * @param fxmlName FXML文件的名称（不带路径，例如 "MainMenu.fxml"）
     * @param title 窗口标题
     */
    public static void goTo(String fxmlName, String title) {
        try {
            String path = "/com/ydc/chess/ui/" + fxmlName;
            URL resource = MainApplication.class.getResource(path);

            if (resource == null) {
                System.err.println("错误：找不到界面文件 -> " + path);
                return;
            }
            // 2. 加载 FXML
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            // 3. 创建新场景或替换根节点
            // 这里我们直接替换 Scene 的根节点，或者创建一个新 Scene
            Scene scene = new Scene(root, 850, 675); // 默认大小，可调整

            // 4. 设置到主窗口
            primaryStage.setScene(scene);
            primaryStage.setTitle("中国象棋 - " + title);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("界面跳转失败：" + e.getMessage());
        }
    }
}