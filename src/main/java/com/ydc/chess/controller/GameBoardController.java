package com.ydc.chess.controller;

import com.ydc.chess.model.Board;
import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Pos;
import com.ydc.chess.ui.BoardRenderer;
import com.ydc.chess.ui.DialogUtils;
import com.ydc.chess.ui.UIManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * 游戏棋盘界面的控制器
 * 负责初始化棋盘、处理点击事件、响应按钮操作。
 */
public class GameBoardController {

    @FXML private Pane boardPane;       // FXML 中的棋盘容器
    @FXML private Label turnLabel;      // 显示当前回合
    @FXML private Label timerLabel;     // 显示时间
    @FXML private TextArea gameLogArea; // 显示日志

    private Board gameBoard; // 游戏核心数据模型

    // 初始化方法，JavaFX 自动调用
    @FXML
    public void initialize() {
        System.out.println("初始化棋盘界面...");

        // 1. 创建新的棋盘数据 (会自动初始化 32 个棋子)
        gameBoard = new Board();

        // 2. 绘制棋盘和棋子
        refreshBoard();

        // 3. 绑定鼠标点击事件
        boardPane.setOnMouseClicked(this::handleBoardClick);

        turnLabel.setText("当前回合: 红方");
        log("对局开始，红方先行。");
    }

    // 刷新界面的辅助方法
    private void refreshBoard() {
        BoardRenderer.drawBoard(boardPane, gameBoard);
    }

    // 处理棋盘点击事件
    private void handleBoardClick(MouseEvent event) {
        // 获取点击的像素坐标
        double mouseX = event.getX();
        double mouseY = event.getY();

        // 转换为逻辑坐标 (列, 行)
        Pos clickedPos = getLogicalPosition(mouseX, mouseY);

        if (clickedPos != null) {
            // 获取该位置的棋子
            Piece piece = gameBoard.getPiece(clickedPos);

            if (piece != null && !piece.ispicked()) {
                log("选中: " + piece.getName() + " " + clickedPos.toString());
                gameBoard.clearpicked();
                piece.setpicked(true);
            }
            else if (piece != null && piece.ispicked()) {
                log("取消选中: " + piece.getName() + " " + clickedPos.toString());
                piece.setpicked(false);
            }
            else {
                log("点击空地: " + clickedPos.toString());
                gameBoard.clearpicked();
                // TODO: 这里后续添加“移动棋子”的逻辑
            }
        }
    }

    // 将像素坐标转换为逻辑坐标 (反向计算)
    private Pos getLogicalPosition(double x, double y) {
        double margin = BoardRenderer.MARGIN;
        double cellSize = BoardRenderer.CELL_SIZE;

        // 计算距离网格起点的偏移量
        double offsetX = x - margin;
        double offsetY = y - margin;

        // 四舍五入找到最近的交叉点
        int col = (int) Math.round(offsetX / cellSize);
        int row = (int) Math.round(offsetY / cellSize);

        // 点击容差范围 (比如点击点必须在交叉点周围 20像素内才算有效)
        double clickRadius = 20.0;
        double targetX = margin + col * cellSize;
        double targetY = margin + row * cellSize;

        if (Math.abs(x - targetX) > clickRadius || Math.abs(y - targetY) > clickRadius) {
            // 点歪了，不算有效点击
            return null;
        }

        // 检查边界
        if (col >= 0 && col < 9 && row >= 0 && row < 10) {
            return new Pos(col, row);
        }
        return null;
    }

    // --- 按钮事件 ---

    @FXML
    public void onRegretClicked() {
        if (DialogUtils.showConfirm("请求悔棋", "您确定要悔棋吗？")) {
            log("已发送悔棋请求...");
        }
    }

    @FXML
    public void onDrawClicked() {
        if (DialogUtils.showConfirm("请求求和", "您确定要提议和局吗？")) {
            log("已发送求和请求...");
        }
    }

    @FXML
    public void onSurrenderClicked() {
        if (DialogUtils.showConfirm("认输", "确定认输？游戏将结束。")) {
            log("您认输了。游戏结束。");
            DialogUtils.showInfo("游戏结束", "您认输了。");
            UIManager.goTo("MainMenu.fxml", "主菜单");
        }
    }

    @FXML
    public void onBackClicked() {
        if (DialogUtils.showConfirm("退出", "强制退出将判负，确定吗？")) {
            UIManager.goTo("MainMenu.fxml", "主菜单");
        }
    }

    private void log(String msg) {
        gameLogArea.appendText(msg + "\n");
    }
}