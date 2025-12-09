package com.ydc.chess.rule;

import com.ydc.chess.model.Piece;

/*
  棋子移动规则接口：实现类负责返回从 (fx,fy) 到 (tx,ty) 的合法性。
*/
public interface Rule {
    boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty);
}