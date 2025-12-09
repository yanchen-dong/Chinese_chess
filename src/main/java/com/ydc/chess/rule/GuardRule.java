package com.ydc.chess.rule;

import com.ydc.chess.model.Guard;
import com.ydc.chess.model.Piece;

/*
  士（仕）规则实现
*/
public class GuardRule extends AbstractRule {
    @Override
    public boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (!(from instanceof Guard)) return false;
        Guard s = (Guard) from;
        Piece to = board[tx][ty];
        if (to != null && sameColor(from, to)) return false;

        if (!inPalace(s.getColor(), tx, ty)) return false;
        int dr = Math.abs(tx - fx), dc = Math.abs(ty - fy);
        return dr == 1 && dc == 1;
    }
}
