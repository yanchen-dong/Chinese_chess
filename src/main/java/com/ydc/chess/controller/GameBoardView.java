package com.ydc.chess.controller;

import com.ydc.chess.model.Board;

public interface GameBoardView {
    // 要求视图绘制当前棋盘
    void refresh(Board board);

    // 向日志区域追加文本
    void appendLog(String msg);

    // 更新回合标签文本
    void updateTurnLabel(String text);

    void startTimer();
}