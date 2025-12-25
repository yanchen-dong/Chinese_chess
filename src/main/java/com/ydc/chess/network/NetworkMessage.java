package com.ydc.chess.network;

import com.ydc.chess.model.Pos;

/**
 * 网络消息类
 * 用于在客户端和服务器之间传递游戏状态
 */
public class NetworkMessage {
    public enum MessageType {
        MOVE,           // 移动棋子
        REGRET,         // 悔棋请求
        DRAW,           // 求和请求
        DRAW_ACCEPTED,  // 求和被接受
        DRAW_REJECTED,  // 求和被拒绝
        SURRENDER,      // 认输
        CHAT,           // 聊天消息
        CONNECT,        // 连接
        DISCONNECT,     // 断开连接
        GAME_START,     // 游戏开始
        GAME_END        // 游戏结束
    }
    
    private MessageType type;
    private Pos fromPos;
    private Pos toPos;
    private String message;
    private String playerName;
    
    public NetworkMessage() {
    }
    
    public NetworkMessage(MessageType type) {
        this.type = type;
    }
    
    // Getters and Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public Pos getFromPos() {
        return fromPos;
    }
    
    public void setFromPos(Pos fromPos) {
        this.fromPos = fromPos;
    }
    
    public Pos getToPos() {
        return toPos;
    }
    
    public void setToPos(Pos toPos) {
        this.toPos = toPos;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

