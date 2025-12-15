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
    @FXML private Label redRegretLabel;
    @FXML private Label blackRegretLabel;

    private Board gameBoard;
    private GameBoardService boardService;
    private TimerService timerService;
    @FXML
    public void initialize() {
        System.out.println("初始化棋盘界面...");
        // 创建棋盘数据
        gameBoard = new Board();
        timerService = new TimerService();
        // 创建服务并把 view 实现传入（将 UI 操作委托给控制器中的节点）
        boardService = new GameBoardService(gameBoard, timerService , new GameBoardView() {
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

            @Override
            public void startTimer() {
                // 将秒数格式化为 00:00
                timerService.secondsProperty().addListener((obs, oldVal, newVal) -> {
                    int sec = newVal.intValue();
                    int min = sec / 60;
                    int s = sec % 60;
                    timerLabel.setText(String.format("时间: %02d:%02d", min, s));
                });

                // 游戏一开始自动启动
                timerService.startNewTimer();
            }

            @Override
            public void updateRegretCount(int redLeft, int blackLeft) {
                redRegretLabel.setText("红方剩余悔棋次数：" + redLeft);
                blackRegretLabel.setText("黑方剩余悔棋次数：" + blackLeft);
            }

            @Override
            public void clearLog() {
                gameLogArea.clear();
            }
            @Override
            public void showGameOverDialog(String winner) {
                int choice = DialogUtils.showGameOverDialog(winner);

                switch (choice) {
                    case 0: // 再来一局
                        restartGame();
                        break;
                    case 1: // 存储棋局
                        saveGameRecord();
                        break;
                    case 2: // 返回主菜单
                        backToMainMenu();
                        break;
                    default:
                        break;
                }
            }
        });
        // 初始化服务并绑定鼠标事件
        boardService.init();
        boardPane.setOnMouseClicked((MouseEvent e) -> boardService.handleClick(e.getX(), e.getY()));
    }
    private void restartGame() {
        gameBoard.initialize();
        gameLogArea.clear();
        BoardRenderer.drawBoard(boardPane, gameBoard);
        turnLabel.setText("当前回合: 红方");
        gameLogArea.appendText("新的一局开始，红方先行。\n");
        timerService.startNewTimer();
    }
    private void saveGameRecord() {
        gameLogArea.appendText("【提示】棋局记录已保存（功能待实现）\n");
    }

    private void backToMainMenu() {
        UIManager.goTo("MainMenu.fxml", "主菜单");
    }
    // --- 按钮事件（保留在控制器中） ---
    @FXML
    public void onRegretClicked() {

        int left = boardService.getCurrentSideRegretLeft();

        if (left <= 0) {
            DialogUtils.showInfo("无法悔棋", "悔棋次数已用尽。");
            return;
        }

        boolean confirm = DialogUtils.showConfirm(
                "请求悔棋",
                "确认悔棋？\n剩余次数：" + left
        );

        if (!confirm) return;

        boolean success = boardService.tryRegret();
        if (!success) {
            DialogUtils.showError("悔棋失败", "当前无法悔棋。");
        }
    }


    @FXML
    public void onDrawClicked() {

        boolean agreed = twoPhaseConfirm(
                "请求求和",
                "您确定要提议和局吗？",
                "求和确认",
                "对方请求和局，您是否接受？"
        );
        if (agreed) {
            gameLogArea.appendText("双方同意和局，游戏结束。\n");
            timerService.stop();
            // ⭐ 关键：使用“游戏结束对话框”，而不是 showInfo
            int choice = DialogUtils.showGameOverDialog("和局,无人");

            switch (choice) {
                case 0:
                    restartGame();
                    break;
                case 1:
                    saveGameRecord();
                    break;
                case 2:
                    backToMainMenu();
                    break;
                default:
                    break;
            }
        }
        else {
            gameLogArea.appendText("求和未达成，对局继续。\n");
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
    private boolean twoPhaseConfirm(String title1, String content1,
                                    String title2, String content2) {
        // 第一阶段：发起请求
        boolean firstAgree = DialogUtils.showConfirm(title1, content1);
        if (!firstAgree) {
            return false;
        }
        // 第二阶段：模拟对方确认（将来联网时替换这里）
        boolean secondAgree = DialogUtils.showConfirm(title2, content2);
        return secondAgree;
    }
}
