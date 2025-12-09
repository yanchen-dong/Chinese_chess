package com.ydc.chess.rule;

import com.ydc.chess.model.Chariot;
import com.ydc.chess.model.Piece;

/*
  车规则实现
*/
public class ChariotRule extends AbstractRule {
    @Override
    public boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (!(from instanceof Chariot)) return false;
        Piece to = board[tx][ty];
        if (to != null && sameColor(from, to)) return false;

        if (fx != tx && fy != ty) return false;
        return countBetween(board, fx, fy, tx, ty) == 0;
    }
}
