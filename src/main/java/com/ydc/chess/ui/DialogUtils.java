package com.ydc.chess.ui;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;

/**
 * 弹窗工具类
 * 用于显示 提示框、确认框、游戏结束对话框 等
 */
public class DialogUtils {

    /**
     * 显示普通信息提示框
     */
    public static void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 显示确认框
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

    /**
     * 显示游戏结束对话框
     *
     * @param winner 获胜方（如："红方" / "黑方"）
     * @return
     *  0 = 再来一局
     *  2 = 返回主菜单
     * -1 = 未选择
     */
    public static int showGameOverDialog(String winner) {

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("对局结束");
        dialog.setHeaderText(null);

        // 初始内容
        dialog.setContentText("游戏结束，" + winner + " 获胜！");

        ButtonType replayBtn =
                new ButtonType("再来一局", ButtonBar.ButtonData.OK_DONE);
        ButtonType saveBtn =
                new ButtonType("存储棋局记录", ButtonBar.ButtonData.OTHER);
        ButtonType backBtn =
                new ButtonType("返回主菜单", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(replayBtn, saveBtn, backBtn);

        // ====== 拦截“存储棋局记录”按钮 ======
        Node saveButton = dialog.getDialogPane().lookupButton(saveBtn);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {

            // 1️⃣ 执行保存逻辑（此处先占位）
            // TODO: 调用真实的棋局保存服务
            System.out.println("棋局记录已保存");

            // 2️⃣ 更新对话框内容（核心）
            dialog.setContentText("棋局记录已成功保存！\n\n您可以选择继续游戏或返回主菜单。");

            // 3️⃣ 阻止对话框关闭
            event.consume();
        });

        dialog.setResultConverter(button -> {
            if (button == replayBtn) return 0;
            if (button == backBtn) return 2;
            return -1;
        });

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1);
    }

}
