package com.ydc.chess.controller;

import com.ydc.chess.model.GameRecord;

/**
 * 复盘管理器
 * 用于在不同控制器之间传递复盘记录
 */
public class ReplayManager {
    private static GameRecord replayRecord;
    
    public static void setReplayRecord(GameRecord record) {
        replayRecord = record;
    }
    
    public static GameRecord getReplayRecord() {
        return replayRecord;
    }
    
    public static void clear() {
        replayRecord = null;
    }
}

