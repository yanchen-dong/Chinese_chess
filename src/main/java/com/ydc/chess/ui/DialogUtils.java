package com.ydc.chess.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * 弹窗工具类
 * 用于显示 提示框、确认框 等
 */
public class DialogUtils {

    /**
     * 显示普通信息提示框
     * @param title 标题
     * @param content 内容
     */
    public static void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // 不显示头部
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 显示确认框 (例如：确认悔棋？确认认输？)
     * @param title 标题
     * @param content 内容
     * @return 如果用户点击了“是/确定”，返回 true；否则返回 false
     */
    public static boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * 显示错误提示框
     */
    public static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}