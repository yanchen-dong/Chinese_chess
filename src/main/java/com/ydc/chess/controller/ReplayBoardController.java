package com.ydc.chess.controller;

import com.ydc.chess.model.Board;
import com.ydc.chess.model.GameRecord;
import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Pos;
import com.ydc.chess.service.GameRecordService;
import com.ydc.chess.ui.BoardRenderer;
import com.ydc.chess.ui.UIManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.List;

/**
 * 复盘界面控制器
 */
public class ReplayBoardController {
    
    @FXML private Pane boardPane;
    @FXML private Label stepLabel;
    @FXML private Label infoLabel;
    @FXML private TextArea moveLogArea;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button autoPlayButton;
    @FXML private Button backButton;
    @FXML private Slider speedSlider;
    
    private Board board;
    private GameRecord record;
    private List<GameRecord.MoveRecord> moves;
    private int currentStep = 0;
    private boolean isAutoPlaying = false;
    private PauseTransition autoPlayTransition;
    
    @FXML
    public void initialize() {
        // 获取复盘记录
        record = ReplayManager.getReplayRecord();
        if (record == null) {
            UIManager.goTo("MainMenu.fxml", "主菜单");
            return;
        }
        
        // 初始化棋盘
        board = new Board();
        board.initialize();
        BoardRenderer.drawBoard(boardPane, board);
        
        moves = record.getMoves();
        currentStep = 0;
        
        // 显示对局信息
        updateInfo();
        updateButtons();
        
        // 速度滑块（如果已注入）
        if (speedSlider != null) {
            speedSlider.setMin(0.5);
            speedSlider.setMax(3.0);
            speedSlider.setValue(1.0);
            speedSlider.setShowTickLabels(true);
            speedSlider.setShowTickMarks(true);
            speedSlider.setMajorTickUnit(0.5);
            
            // 添加速度滑块监听器
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                onSpeedChanged();
            });
        }
        
        // 初始化日志
        moveLogArea.appendText("复盘开始：\n");
        moveLogArea.appendText("对局类型: " + (record.getGameType() == GameRecord.GameType.LOCAL ? "本地" : "网络") + "\n");
        moveLogArea.appendText("红方: " + record.getRedPlayerName() + "\n");
        moveLogArea.appendText("黑方: " + record.getBlackPlayerName() + "\n");
        moveLogArea.appendText("结果: " + record.getWinner() + "\n");
        moveLogArea.appendText("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
    
    /**
     * 更新信息显示
     */
    private void updateInfo() {
        stepLabel.setText("步数: " + currentStep + " / " + moves.size());
        
        if (currentStep == 0) {
            infoLabel.setText("初始局面");
        } else if (currentStep <= moves.size()) {
            GameRecord.MoveRecord move = moves.get(currentStep - 1);
            String color = move.getPieceColor().equals("RED") ? "红方" : "黑方";
            String info = String.format("%s %s: (%d,%d) -> (%d,%d)",
                color, move.getPieceName(),
                move.getFromCol(), move.getFromRow(),
                move.getToCol(), move.getToRow());
            if (move.getCapturedPieceName() != null) {
                info += " 吃 " + move.getCapturedPieceName();
            }
            infoLabel.setText(info);
        }
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtons() {
        prevButton.setDisable(currentStep == 0 || isAutoPlaying);
        nextButton.setDisable(currentStep >= moves.size() || isAutoPlaying);
        autoPlayButton.setText(isAutoPlaying ? "暂停" : "自动播放");
    }
    
    @FXML
    public void onPrevClicked() {
        if (currentStep > 0 && !isAutoPlaying) {
            // 回退一步（重新执行到上一步）
            board.initialize();
            currentStep--;
            for (int i = 0; i < currentStep; i++) {
                executeMove(moves.get(i));
            }
            BoardRenderer.drawBoard(boardPane, board);
            updateInfo();
            updateButtons();
            
            // 更新日志
            if (currentStep > 0) {
                moveLogArea.appendText("← 回退到第 " + currentStep + " 步\n");
            } else {
                moveLogArea.appendText("← 回退到初始局面\n");
            }
        }
    }
    
    @FXML
    public void onNextClicked() {
        if (currentStep < moves.size() && !isAutoPlaying) {
            executeMove(moves.get(currentStep));
            currentStep++;
            BoardRenderer.drawBoard(boardPane, board);
            updateInfo();
            updateButtons();
            
            // 更新日志
            GameRecord.MoveRecord move = moves.get(currentStep - 1);
            String color = move.getPieceColor().equals("RED") ? "红方" : "黑方";
            String log = String.format("第 %d 步: %s %s (%d,%d) -> (%d,%d)",
                currentStep, color, move.getPieceName(),
                move.getFromCol(), move.getFromRow(),
                move.getToCol(), move.getToRow());
            if (move.getCapturedPieceName() != null) {
                log += " 吃 " + move.getCapturedPieceName();
            }
            moveLogArea.appendText(log + "\n");
        }
    }
    
    @FXML
    public void onAutoPlayClicked() {
        if (isAutoPlaying) {
            stopAutoPlay();
        } else {
            startAutoPlay();
        }
    }
    
    /**
     * 开始自动播放
     */
    private void startAutoPlay() {
        if (currentStep >= moves.size()) {
            // 从头开始
            board.initialize();
            currentStep = 0;
            BoardRenderer.drawBoard(boardPane, board);
            moveLogArea.clear();
            moveLogArea.appendText("自动播放开始：\n");
        }
        
        isAutoPlaying = true;
        updateButtons();
        
        autoPlayTransition = new PauseTransition(Duration.seconds(2.0 / speedSlider.getValue()));
        autoPlayTransition.setOnFinished(e -> {
            if (currentStep < moves.size() && isAutoPlaying) {
                executeMove(moves.get(currentStep));
                currentStep++;
                BoardRenderer.drawBoard(boardPane, board);
                updateInfo();
                
                if (currentStep >= moves.size()) {
                    stopAutoPlay();
                    moveLogArea.appendText("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    moveLogArea.appendText("复盘结束\n");
                } else {
                    // 继续下一步
                    autoPlayTransition.setDuration(Duration.seconds(2.0 / speedSlider.getValue()));
                    autoPlayTransition.play();
                }
            } else {
                stopAutoPlay();
            }
        });
        
        autoPlayTransition.play();
    }
    
    /**
     * 停止自动播放
     */
    private void stopAutoPlay() {
        isAutoPlaying = false;
        if (autoPlayTransition != null) {
            autoPlayTransition.stop();
        }
        updateButtons();
    }
    
    /**
     * 执行一步移动
     */
    private void executeMove(GameRecord.MoveRecord move) {
        Pos from = new Pos(move.getFromCol(), move.getFromRow());
        Pos to = new Pos(move.getToCol(), move.getToRow());
        board.move(from, to);
    }
    
    @FXML
    public void onBackClicked() {
        stopAutoPlay();
        ReplayManager.clear();
        UIManager.goTo("Records.fxml", "对局记录");
    }
    
    public void onSpeedChanged() {
        if (isAutoPlaying && autoPlayTransition != null) {
            autoPlayTransition.setDuration(Duration.seconds(2.0 / speedSlider.getValue()));
        }
    }
}

