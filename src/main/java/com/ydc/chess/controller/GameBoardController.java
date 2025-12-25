 package com.ydc.chess.controller;
import com.ydc.chess.model.Board;
import com.ydc.chess.model.GameRecord;
import com.ydc.chess.model.GameSettings;
import com.ydc.chess.model.Piece;
import com.ydc.chess.network.NetworkMessage;
import com.ydc.chess.network.NetworkService;
import com.ydc.chess.service.GameRecordService;
import com.ydc.chess.ui.BoardRenderer;
import com.ydc.chess.ui.DialogUtils;
import com.ydc.chess.ui.UIManager;
import javafx.application.Platform;
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
    private NetworkService networkService;
    private boolean waitingForRestartResponse = false; // 是否正在等待对方对再来一局的响应
    private boolean gameOverDialogShowing = false; // 游戏结束对话框是否正在显示
    private javafx.scene.control.Dialog<Integer> currentGameOverDialog = null; // 当前显示的游戏结束对话框
    
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
                // 如果正在等待再来一局的响应，不显示游戏结束对话框
                if (waitingForRestartResponse) {
                    return;
                }
                
                gameOverDialogShowing = true;
                int choice = showGameOverDialogInternal(winner);
                gameOverDialogShowing = false;
                currentGameOverDialog = null;

                switch (choice) {
                    case 0: // 再来一局
                        requestRestart();
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
        
        // 如果是网络模式，设置网络消息监听器
        if (NetworkManager.isNetworkMode()) {
            networkService = NetworkManager.getNetworkService();
            setupNetworkListener();
            
            // 显示网络模式提示
            if (networkService.isHost()) {
                gameLogArea.appendText("网络模式：您是红方（主机）\n");
            } else {
                gameLogArea.appendText("网络模式：您是黑方（客机）\n");
            }
        }
    }
    
    /**
     * 设置网络消息监听器
     */
    private void setupNetworkListener() {
        networkService.addListener(new NetworkService.NetworkMessageListener() {
            @Override
            public void onMessageReceived(NetworkMessage message) {
                Platform.runLater(() -> {
                    switch (message.getType()) {
                        case MOVE:
                            if (message.getFromPos() != null && message.getToPos() != null) {
                                boardService.handleNetworkMove(message.getFromPos(), message.getToPos());
                            }
                            break;
                        case REGRET:
                            boardService.handleNetworkRegret();
                            break;
                        case DRAW:
                            handleNetworkDraw();
                            break;
                        case DRAW_ACCEPTED:
                            handleNetworkDrawAccepted();
                            break;
                        case DRAW_REJECTED:
                            handleNetworkDrawRejected();
                            break;
                        case SURRENDER:
                            handleNetworkSurrender();
                            break;
                        case RESTART_REQUEST:
                            handleNetworkRestartRequest();
                            break;
                        case RESTART_ACCEPTED:
                            handleNetworkRestartAccepted();
                            break;
                        case RESTART_REJECTED:
                            handleNetworkRestartRejected();
                            break;
                        case DISCONNECT:
                            DialogUtils.showError("连接断开", "对方已断开连接");
                            backToMainMenu();
                            break;
                        default:
                            break;
                    }
                });
            }
            
            @Override
            public void onConnectionEstablished() {
                Platform.runLater(() -> {
                    gameLogArea.appendText("网络连接已建立\n");
                });
            }
            
            @Override
            public void onConnectionLost() {
                Platform.runLater(() -> {
                    DialogUtils.showError("连接错误", "网络连接已断开");
                    backToMainMenu();
                });
            }
        });
    }
    
    private void handleNetworkDraw() {
        boolean agreed = DialogUtils.showConfirm("对方请求和局", "对方请求和局，您是否接受？");
        if (agreed) {
            // 发送接受消息
            NetworkMessage acceptMessage = new NetworkMessage(NetworkMessage.MessageType.DRAW_ACCEPTED);
            networkService.sendMessage(acceptMessage);
            
            gameLogArea.appendText("双方同意和局，游戏结束。\n");
            timerService.stop();
            gameOverDialogShowing = true;
            int choice = showGameOverDialogInternal("和局,无人");
            gameOverDialogShowing = false;
            currentGameOverDialog = null;
            switch (choice) {
                case 0:
                    requestRestart();
                    break;
                case 1:
                    saveGameRecord();
                    break;
                case 2:
                    backToMainMenu();
                    break;
            }
        } else {
            // 发送拒绝消息
            NetworkMessage rejectMessage = new NetworkMessage(NetworkMessage.MessageType.DRAW_REJECTED);
            networkService.sendMessage(rejectMessage);
            gameLogArea.appendText("您拒绝了和局请求。\n");
        }
    }
    
    private void handleNetworkDrawAccepted() {
        gameLogArea.appendText("对方接受了和局请求，游戏结束。\n");
        timerService.stop();
        gameOverDialogShowing = true;
        int choice = showGameOverDialogInternal("和局,无人");
        gameOverDialogShowing = false;
        currentGameOverDialog = null;
        switch (choice) {
            case 0:
                requestRestart();
                break;
            case 1:
                saveGameRecord();
                break;
            case 2:
                backToMainMenu();
                break;
        }
    }
    
    private void handleNetworkDrawRejected() {
        gameLogArea.appendText("对方拒绝了和局请求，对局继续。\n");
    }
    
    private void handleNetworkSurrender() {
        gameLogArea.appendText("对方认输，您获胜！\n");
        timerService.stop();
        // 确定获胜方：如果我是主机（红方），对方是客机（黑方）认输，我获胜，所以获胜方是"红方"
        // 如果我是客机（黑方），对方是主机（红方）认输，我获胜，所以获胜方是"黑方"
        String winner = networkService.isHost() ? "红方" : "黑方";
        int choice = showGameOverDialogInternal(winner);
        gameOverDialogShowing = false;
        currentGameOverDialog = null;
        
        switch (choice) {
            case 0:
                requestRestart();
                break;
            case 1:
                saveGameRecord();
                break;
            case 2:
                backToMainMenu();
                break;
        }
    }
    /**
     * 请求再来一局（网络模式下发送邀请，本地模式直接重启）
     */
    private void requestRestart() {
        if (NetworkManager.isNetworkMode() && networkService != null && networkService.isConnected()) {
            // 网络模式：发送再来一局请求
            waitingForRestartResponse = true;
            NetworkMessage restartMessage = new NetworkMessage(NetworkMessage.MessageType.RESTART_REQUEST);
            networkService.sendMessage(restartMessage);
            gameLogArea.appendText("已发送再来一局请求，等待对方回应...\n");
        } else {
            // 本地模式：直接重启
            restartGame();
        }
    }
    
    /**
     * 重启游戏（实际执行重启操作）
     */
    private void restartGame() {
        gameBoard.initialize();
        gameLogArea.clear();
        BoardRenderer.drawBoard(boardPane, gameBoard);
        turnLabel.setText("当前回合: 红方");
        gameLogArea.appendText("新的一局开始，红方先行。\n");
        timerService.startNewTimer();
        waitingForRestartResponse = false;
    }
    
    /**
     * 处理网络再来一局请求
     */
    private void handleNetworkRestartRequest() {
        // 如果游戏结束对话框正在显示，先关闭它
        if (gameOverDialogShowing && currentGameOverDialog != null) {
            javafx.scene.control.Dialog<Integer> dialogToClose = currentGameOverDialog;
            Platform.runLater(() -> {
                // 使用 setResult 来关闭对话框，这样会触发 resultConverter
                dialogToClose.setResult(-1);
                dialogToClose.close();
            });
            gameOverDialogShowing = false;
            currentGameOverDialog = null;
        }
        
        // 延迟一下，确保对话框已关闭
        Platform.runLater(() -> {
            boolean agreed = DialogUtils.showConfirm("对方请求再来一局", "对方请求再来一局，您是否同意？");
            if (agreed) {
                // 发送接受消息
                NetworkMessage acceptMessage = new NetworkMessage(NetworkMessage.MessageType.RESTART_ACCEPTED);
                networkService.sendMessage(acceptMessage);
                
                gameLogArea.appendText("您同意了再来一局请求。\n");
                restartGame();
            } else {
                // 发送拒绝消息
                NetworkMessage rejectMessage = new NetworkMessage(NetworkMessage.MessageType.RESTART_REJECTED);
                networkService.sendMessage(rejectMessage);
                
                gameLogArea.appendText("您拒绝了再来一局请求。\n");
                backToMainMenu();
            }
        });
    }
    
    /**
     * 处理网络再来一局被接受
     */
    private void handleNetworkRestartAccepted() {
        if (waitingForRestartResponse) {
            waitingForRestartResponse = false;
            gameLogArea.appendText("对方同意了再来一局请求。\n");
            
            // 如果游戏结束对话框正在显示，先关闭它
            if (gameOverDialogShowing && currentGameOverDialog != null) {
                javafx.scene.control.Dialog<Integer> dialogToClose = currentGameOverDialog;
                Platform.runLater(() -> {
                    // 使用 setResult 来关闭对话框
                    dialogToClose.setResult(-1);
                    dialogToClose.close();
                });
                gameOverDialogShowing = false;
                currentGameOverDialog = null;
            }
            
            // 延迟一下，确保对话框已关闭
            Platform.runLater(() -> {
                restartGame();
            });
        }
    }
    
    /**
     * 处理网络再来一局被拒绝
     */
    private void handleNetworkRestartRejected() {
        if (waitingForRestartResponse) {
            waitingForRestartResponse = false;
            gameLogArea.appendText("对方拒绝了再来一局请求。\n");
            
            // 如果游戏结束对话框正在显示，先关闭它
            if (gameOverDialogShowing && currentGameOverDialog != null) {
                javafx.scene.control.Dialog<Integer> dialogToClose = currentGameOverDialog;
                Platform.runLater(() -> {
                    // 使用 setResult 来关闭对话框
                    dialogToClose.setResult(-1);
                    dialogToClose.close();
                });
                gameOverDialogShowing = false;
                currentGameOverDialog = null;
            }
            
            // 延迟一下，确保对话框已关闭
            Platform.runLater(() -> {
                DialogUtils.showInfo("再来一局", "对方拒绝了再来一局请求。");
                backToMainMenu();
            });
        }
    }
    
    /**
     * 内部方法：显示游戏结束对话框（保存对话框引用以便可以关闭）
     */
    private int showGameOverDialogInternal(String winner) {
        javafx.scene.control.Dialog<Integer> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("对局结束");
        dialog.setHeaderText(null);
        dialog.setContentText("游戏结束，" + winner + " 获胜！");

        javafx.scene.control.ButtonType replayBtn =
                new javafx.scene.control.ButtonType("再来一局", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType saveBtn =
                new javafx.scene.control.ButtonType("存储棋局记录", javafx.scene.control.ButtonBar.ButtonData.OTHER);
        javafx.scene.control.ButtonType backBtn =
                new javafx.scene.control.ButtonType("返回主菜单", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(replayBtn, saveBtn, backBtn);

        dialog.setResultConverter(button -> {
            if (button == replayBtn) return 0;
            if (button == saveBtn) return 1;
            if (button == backBtn) return 2;
            return -1;
        });

        currentGameOverDialog = dialog;
        java.util.Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1);
    }
    private void saveGameRecord() {
        try {
            // 确定游戏类型
            GameRecord.GameType gameType = NetworkManager.isNetworkMode() 
                ? GameRecord.GameType.NETWORK 
                : GameRecord.GameType.LOCAL;
            
            // 获取玩家名称
            String redPlayerName = GameSettings.getInstance().getPlayerName();
            String blackPlayerName = "对手";
            if (NetworkManager.isNetworkMode() && networkService != null) {
                if (networkService.isHost()) {
                    blackPlayerName = "网络玩家";
                } else {
                    redPlayerName = "网络玩家";
                    blackPlayerName = GameSettings.getInstance().getPlayerName();
                }
            }
            
            // 创建记录
            GameRecord record = GameRecordService.createRecordFromBoard(
                gameBoard, gameType, "已保存", redPlayerName, blackPlayerName);
            
            // 保存记录
            if (GameRecordService.saveRecord(record)) {
                gameLogArea.appendText("【提示】棋局记录已保存成功！\n");
                DialogUtils.showInfo("保存成功", "对局记录已保存，可以在主菜单的\"对局记录\"中查看。");
            } else {
                gameLogArea.appendText("【提示】棋局记录保存失败\n");
                DialogUtils.showError("保存失败", "保存对局记录时出错，请重试。");
            }
        } catch (Exception e) {
            System.err.println("保存记录时出错: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("错误", "保存对局记录时发生错误。");
        }
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
        // 网络模式下，直接发送求和请求
        if (NetworkManager.isNetworkMode() && networkService != null && networkService.isConnected()) {
            networkService.sendDraw();
            gameLogArea.appendText("已发送和局请求，等待对方回应...\n");
            return;
        }
        
        // 本地模式
        boolean agreed = twoPhaseConfirm(
                "请求求和",
                "您确定要提议和局吗？",
                "求和确认",
                "对方请求和局，您是否接受？"
        );
        if (agreed) {
            gameLogArea.appendText("双方同意和局，游戏结束。\n");
            timerService.stop();
            // ⭐ 关键：使用"游戏结束对话框"，而不是 showInfo
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
            // 网络模式下，发送认输消息
            if (NetworkManager.isNetworkMode() && networkService != null && networkService.isConnected()) {
                networkService.sendSurrender();
            }
            
            gameLogArea.appendText("您认输了。游戏结束。\n");
            timerService.stop();
            
            // 确定获胜方
            String winner;
            if (NetworkManager.isNetworkMode() && networkService != null) {
                // 网络模式：如果我是主机（红方），我认输，则获胜方是客机（黑方）
                // 如果我是客机（黑方），我认输，则获胜方是主机（红方）
                winner = networkService.isHost() ? "黑方" : "红方";
            } else {
                // 本地模式：认输方的对方获胜（当前回合是认输方，对方是获胜方）
                winner = (gameBoard.getCurrentTurn() == Piece.Color.RED) ? "黑方" : "红方";
            }
            gameOverDialogShowing = true;
            int choice = showGameOverDialogInternal(winner);
            gameOverDialogShowing = false;
            currentGameOverDialog = null;
            switch (choice) {
                case 0:
                    requestRestart();
                    break;
                case 1:
                    saveGameRecord();
                    break;
                case 2:
                    backToMainMenu();
                    break;
            }
        }
    }
    @FXML
    public void onBackClicked() {
        if (DialogUtils.showConfirm("退出", "强制退出将判负，确定吗？")) {
            if (NetworkManager.isNetworkMode() && networkService != null) {
                networkService.disconnect();
                NetworkManager.clear();
            }
            backToMainMenu();
        }
    }
    
    private void backToMainMenu() {
        if (NetworkManager.isNetworkMode() && networkService != null) {
            networkService.disconnect();
            NetworkManager.clear();
        }
        UIManager.goTo("MainMenu.fxml", "主菜单");
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
