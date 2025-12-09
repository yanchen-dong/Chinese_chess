package com.ydc.chess.rule;

import com.ydc.chess.model.Knight;
import com.ydc.chess.model.Piece;

/*
  马规则实现
*/
public class KnightRule extends AbstractRule {
    @Override
    public boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (!(from instanceof Knight)) return false;
        Piece to = board[tx][ty];
        if (to != null && sameColor(from, to)) return false;

        int dr = tx - fx, dc = ty - fy;
        int adr = Math.abs(dr), adc = Math.abs(dc);
        if (!((adr == 2 && adc == 1) || (adr == 1 && adc == 2))) return false;
        if (adr == 2) {
            int br = fx + dr / 2, bc = fy;
            if (board[br][bc] != null) return false;
        } else {
            int br = fx, bc = fy + dc / 2;
            if (board[br][bc] != null) return false;
        }
        return true;
    }
}
