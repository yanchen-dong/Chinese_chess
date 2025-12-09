package com.ydc.chess.rule;

import com.ydc.chess.model.Piece;

/*
  棋子移动规则接口：实现类负责返回从 (fx,fy) 到 (tx,ty) 的合法性。
*/
public interface Rule {
    boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty);
}//判断非法移动（包括移动后被将军的情况），因此需要后面再进行更改