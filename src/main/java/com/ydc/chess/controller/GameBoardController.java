package com.ydc.chess.controller;

import com.ydc.chess.model.Board;
import com.ydc.chess.ui.BoardRenderer;
import com.ydc.chess.ui.DialogUtils;
import com.ydc.chess.ui.UIManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class GameBoardController {

    @FXML private Pane boardPane;
    @FXML private Label turnLabel;
    @FXML private Label timerLabel;
    @FXML private TextArea gameLogArea;

    private Board gameBoard;
    private GameBoardService boardService;

    @FXML
    public void initialize() {
        System.out.println("初始化棋盘界面...");

        // 创建棋盘数据
        gameBoard = new Board();

        // 创建服务并把 view 实现传入（将 UI 操作委托给控制器中的节点）
        boardService = new GameBoardService(gameBoard, new GameBoardView() {
            @Override
            public void refresh(Board board) {
                BoardRenderer.drawBoard(boardPane, board);
            }

            @Override
            public void appendLog(String msg) {
                gameLogArea.appendText(msg + "\n");
            }

            @Override
            public void updateTurnLabel(String text) {
                turnLabel.setText(text);
            }
        });

        // 初始化服务并绑定鼠标事件
        boardService.init();
        boardPane.setOnMouseClicked((MouseEvent e) -> boardService.handleClick(e.getX(), e.getY()));
    }

    // --- 按钮事件（保留在控制器中） ---

    @FXML
    public void onRegretClicked() {
        if (DialogUtils.showConfirm("请求悔棋", "您确定要悔棋吗？")) {
            gameLogArea.appendText("已发送悔棋请求...\n");
        }
    }

    @FXML
    public void onDrawClicked() {
        if (DialogUtils.showConfirm("请求求和", "您确定要提议和局吗？")) {
            gameLogArea.appendText("已发送求和请求...\n");
        }
    }

    @FXML
    public void onSurrenderClicked() {
        if (DialogUtils.showConfirm("认输", "确定认输？游戏将结束。")) {
            gameLogArea.appendText("您认输了。游戏结束。\n");
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
}
