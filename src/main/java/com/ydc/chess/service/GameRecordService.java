package com.ydc.chess.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ydc.chess.model.Board;
import com.ydc.chess.model.GameRecord;
import com.ydc.chess.model.Move;
import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Pos;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 对局记录服务类
 * 负责保存和加载对局记录
 */
public class GameRecordService {
    private static final String RECORDS_DIR = "game_records";
    private static final String RECORDS_FILE = RECORDS_DIR + File.separator + "records.json";
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setPrettyPrinting()
            .create();
    
    /**
     * 保存对局记录
     */
    public static boolean saveRecord(GameRecord record) {
        try {
            if (record == null) {
                System.err.println("保存失败：记录为空");
                return false;
            }
            
            if (record.getMoves() == null || record.getMoves().isEmpty()) {
                System.err.println("保存失败：记录中没有走棋数据");
                return false;
            }
            
            // 确保目录存在
            Path dir = Paths.get(RECORDS_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("创建记录目录: " + dir.toAbsolutePath());
            }
            
            // 加载现有记录
            List<GameRecord> records = loadAllRecords();
            if (records == null) {
                records = new ArrayList<>();
            }
            
            // 添加新记录
            records.add(record);
            
            // 按日期排序（最新的在前）
            records.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            
            // 保存到文件
            File file = new File(RECORDS_FILE);
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(records, writer);
                writer.flush();
            }
            
            System.out.println("对局记录已保存到: " + file.getAbsolutePath());
            System.out.println("记录ID: " + record.getId() + ", 步数: " + record.getTotalMoves());
            
            return true;
        } catch (IOException e) {
            System.err.println("保存对局记录失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("保存对局记录时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 从棋盘创建对局记录
     */
    public static GameRecord createRecordFromBoard(Board board, GameRecord.GameType gameType, 
                                                   String winner, String redPlayerName, String blackPlayerName) {
        GameRecord record = new GameRecord();
        record.setGameType(gameType);
        record.setWinner(winner);
        record.setRedPlayerName(redPlayerName != null ? redPlayerName : "红方");
        record.setBlackPlayerName(blackPlayerName != null ? blackPlayerName : "黑方");
        record.setTotalMoves(board.getMoveHistory().size());
        
        // 转换走棋历史
        List<GameRecord.MoveRecord> moveRecords = new ArrayList<>();
        Board tempBoard = new Board();
        tempBoard.initialize();
        
        for (Move move : board.getMoveHistory()) {
            Pos from = move.getFromPos();
            Pos to = move.getToPos();
            
            // 从临时棋盘获取棋子信息
            Piece piece = tempBoard.getPiece(from);
            if (piece != null) {
                Piece captured = move.getCapturedPiece();
                
                GameRecord.MoveRecord moveRecord = new GameRecord.MoveRecord(
                    from.getY(), from.getX(),  // 行，列
                    to.getY(), to.getX(),      // 行，列
                    piece.getName(),
                    piece.getColor().toString(),
                    captured != null ? captured.getName() : null
                );
                
                moveRecords.add(moveRecord);
                
                // 在临时棋盘上执行移动（用于复盘）
                // 直接操作棋盘，不进行合法性检查（因为这是从历史记录中恢复的合法移动）
                Piece[][] grid = tempBoard.getGrid();
                grid[to.getY()][to.getX()] = piece;
                grid[from.getY()][from.getX()] = null;
                piece.setPosition(to);
            }
        }
        
        record.setMoves(moveRecords);
        return record;
    }
    
    /**
     * 加载所有记录
     */
    public static List<GameRecord> loadAllRecords() {
        try {
            File file = new File(RECORDS_FILE);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<GameRecord>>(){}.getType();
                List<GameRecord> records = gson.fromJson(reader, listType);
                return records != null ? records : new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("加载对局记录失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 删除记录
     */
    public static boolean deleteRecord(String recordId) {
        try {
            List<GameRecord> records = loadAllRecords();
            records.removeIf(r -> r.getId().equals(recordId));
            
            try (FileWriter writer = new FileWriter(RECORDS_FILE)) {
                gson.toJson(records, writer);
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("删除对局记录失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 格式化日期显示
     */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}

