package com.ydc.chess.controller;

import com.ydc.chess.model.GameRecord;
import com.ydc.chess.service.GameRecordService;
import com.ydc.chess.ui.DialogUtils;
import com.ydc.chess.ui.UIManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * 对局记录界面控制器
 */
public class RecordsController {
    
    @FXML private TableView recordsTable;
    
    private TableColumn<GameRecord, String> dateColumn;
    private TableColumn<GameRecord, String> typeColumn;
    private TableColumn<GameRecord, String> winnerColumn;
    private TableColumn<GameRecord, Integer> movesColumn;
    private TableColumn<GameRecord, String> playersColumn;
    
    @FXML private TextArea recordDetailsArea;
    @FXML private Button replayButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    
    private ObservableList<GameRecord> recordsList;
    
    @FXML
    public void initialize() {
        // 创建表格列
        dateColumn = new TableColumn<>("日期");
        dateColumn.setPrefWidth(180);
        dateColumn.setCellValueFactory(cellData -> {
            String dateStr = GameRecordService.formatDate(cellData.getValue().getDate());
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        
        typeColumn = new TableColumn<>("类型");
        typeColumn.setPrefWidth(80);
        typeColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getGameType() == GameRecord.GameType.LOCAL ? "本地" : "网络";
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        
        winnerColumn = new TableColumn<>("结果");
        winnerColumn.setPrefWidth(100);
        winnerColumn.setCellValueFactory(cellData -> {
            String winner = cellData.getValue().getWinner();
            return new javafx.beans.property.SimpleStringProperty(winner);
        });
        
        movesColumn = new TableColumn<>("步数");
        movesColumn.setPrefWidth(60);
        movesColumn.setCellValueFactory(cellData -> {
            Integer moves = cellData.getValue().getTotalMoves();
            return new javafx.beans.property.SimpleIntegerProperty(moves).asObject();
        });
        
        playersColumn = new TableColumn<>("对局双方");
        playersColumn.setPrefWidth(150);
        playersColumn.setCellValueFactory(cellData -> {
            GameRecord record = cellData.getValue();
            String players = record.getRedPlayerName() + " vs " + record.getBlackPlayerName();
            return new javafx.beans.property.SimpleStringProperty(players);
        });
        
        // 添加列到表格
        recordsTable.getColumns().addAll(dateColumn, typeColumn, winnerColumn, movesColumn, playersColumn);
        
        // 加载记录
        loadRecords();
        
        // 表格选择监听（使用原始类型）
        recordsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal instanceof GameRecord) {
                showRecordDetails((GameRecord) newVal);
                replayButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                recordDetailsArea.clear();
                replayButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        
        // 初始状态
        replayButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    /**
     * 加载所有记录
     */
    private void loadRecords() {
        List<GameRecord> records = GameRecordService.loadAllRecords();
        recordsList = FXCollections.observableArrayList(records);
        recordsTable.setItems(recordsList);
    }
    
    /**
     * 显示记录详情
     */
    private void showRecordDetails(GameRecord record) {
        StringBuilder details = new StringBuilder();
        details.append("对局信息\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        details.append("日期: ").append(GameRecordService.formatDate(record.getDate())).append("\n");
        details.append("类型: ").append(record.getGameType() == GameRecord.GameType.LOCAL ? "本地对战" : "网络对战").append("\n");
        details.append("红方: ").append(record.getRedPlayerName()).append("\n");
        details.append("黑方: ").append(record.getBlackPlayerName()).append("\n");
        details.append("结果: ").append(record.getWinner()).append("\n");
        details.append("总步数: ").append(record.getTotalMoves()).append("\n");
        details.append("\n走棋记录:\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        int step = 1;
        for (GameRecord.MoveRecord move : record.getMoves()) {
            String color = move.getPieceColor().equals("RED") ? "红" : "黑";
            String moveStr = String.format("%d. %s方 %s: (%d,%d) -> (%d,%d)",
                step++, color, move.getPieceName(),
                move.getFromCol(), move.getFromRow(),
                move.getToCol(), move.getToRow());
            
            if (move.getCapturedPieceName() != null) {
                moveStr += " 吃 " + move.getCapturedPieceName();
            }
            
            details.append(moveStr).append("\n");
        }
        
        recordDetailsArea.setText(details.toString());
    }
    
    @FXML
    public void onReplayClicked() {
        Object selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !(selected instanceof GameRecord)) {
            DialogUtils.showInfo("提示", "请先选择一条记录");
            return;
        }
        
        GameRecord record = (GameRecord) selected;
        if (record.getMoves() == null || record.getMoves().isEmpty()) {
            DialogUtils.showError("错误", "该记录没有走棋数据，无法复盘");
            return;
        }
        
        try {
            // 跳转到复盘界面
            ReplayManager.setReplayRecord(record);
            UIManager.goTo("ReplayBoard.fxml", "复盘 - " + GameRecordService.formatDate(record.getDate()));
        } catch (Exception e) {
            System.err.println("加载复盘界面失败: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("错误", "加载复盘界面失败: " + e.getMessage());
        }
    }
    
    @FXML
    public void onDeleteClicked() {
        Object selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !(selected instanceof GameRecord)) {
            DialogUtils.showInfo("提示", "请先选择一条记录");
            return;
        }
        
        GameRecord record = (GameRecord) selected;
        
        boolean confirm = DialogUtils.showConfirm("删除记录", 
            "确定要删除这条对局记录吗？\n日期: " + GameRecordService.formatDate(record.getDate()));
        
        if (confirm) {
            if (GameRecordService.deleteRecord(record.getId())) {
                DialogUtils.showInfo("成功", "记录已删除");
                loadRecords();
                recordDetailsArea.clear();
                replayButton.setDisable(true);
                deleteButton.setDisable(true);
            } else {
                DialogUtils.showError("错误", "删除记录失败");
            }
        }
    }
    
    @FXML
    public void onBackClicked() {
        UIManager.goTo("MainMenu.fxml", "主菜单");
    }
}

