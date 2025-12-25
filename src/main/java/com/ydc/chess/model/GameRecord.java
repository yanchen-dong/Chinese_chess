package com.ydc.chess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 对局记录数据模型
 */
public class GameRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum GameType {
        LOCAL,      // 本地对战
        NETWORK     // 网络对战
    }
    
    private String id;                  // 记录ID（时间戳）
    private Date date;                  // 对局日期
    private GameType gameType;          // 游戏类型
    private String winner;              // 获胜方（"红方"、"黑方"、"和局"）
    private List<MoveRecord> moves;     // 走棋记录
    private String redPlayerName;        // 红方玩家名称
    private String blackPlayerName;      // 黑方玩家名称
    private int totalMoves;             // 总步数
    
    public GameRecord() {
        this.moves = new ArrayList<>();
        this.date = new Date();
        this.id = String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * 走棋记录（序列化用）
     */
    public static class MoveRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int fromRow, fromCol;   // 起始位置（行，列）
        private int toRow, toCol;        // 目标位置（行，列）
        private String pieceName;       // 棋子名称
        private String pieceColor;       // 棋子颜色（RED/BLACK）
        private String capturedPieceName; // 被吃的棋子名称（如果有）
        
        public MoveRecord() {
        }
        
        public MoveRecord(int fromRow, int fromCol, int toRow, int toCol, 
                         String pieceName, String pieceColor, String capturedPieceName) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.pieceName = pieceName;
            this.pieceColor = pieceColor;
            this.capturedPieceName = capturedPieceName;
        }
        
        // Getters and Setters
        public int getFromRow() { return fromRow; }
        public void setFromRow(int fromRow) { this.fromRow = fromRow; }
        
        public int getFromCol() { return fromCol; }
        public void setFromCol(int fromCol) { this.fromCol = fromCol; }
        
        public int getToRow() { return toRow; }
        public void setToRow(int toRow) { this.toRow = toRow; }
        
        public int getToCol() { return toCol; }
        public void setToCol(int toCol) { this.toCol = toCol; }
        
        public String getPieceName() { return pieceName; }
        public void setPieceName(String pieceName) { this.pieceName = pieceName; }
        
        public String getPieceColor() { return pieceColor; }
        public void setPieceColor(String pieceColor) { this.pieceColor = pieceColor; }
        
        public String getCapturedPieceName() { return capturedPieceName; }
        public void setCapturedPieceName(String capturedPieceName) { this.capturedPieceName = capturedPieceName; }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public GameType getGameType() { return gameType; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }
    
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    
    public List<MoveRecord> getMoves() { return moves; }
    public void setMoves(List<MoveRecord> moves) { this.moves = moves; }
    
    public String getRedPlayerName() { return redPlayerName; }
    public void setRedPlayerName(String redPlayerName) { this.redPlayerName = redPlayerName; }
    
    public String getBlackPlayerName() { return blackPlayerName; }
    public void setBlackPlayerName(String blackPlayerName) { this.blackPlayerName = blackPlayerName; }
    
    public int getTotalMoves() { return totalMoves; }
    public void setTotalMoves(int totalMoves) { this.totalMoves = totalMoves; }
}

