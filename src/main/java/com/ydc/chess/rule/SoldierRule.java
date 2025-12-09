package com.ydc.chess.rule;

import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Soldier;

/*
  兵（卒）规则实现
*/
public class SoldierRule extends AbstractRule {
    @Override
    public boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (!(from instanceof Soldier)) return false;
        Soldier s = (Soldier) from;
        Piece to = board[tx][ty];
        if (to != null && sameColor(from, to)) return false;

        int dr = tx - fx, dc = ty - fy;
        Piece.Color color = s.getColor();
        boolean crossed = (color == Piece.Color.RED) ? (fx <= 4) : (fx >= 5);
        int forward = (color == Piece.Color.RED) ? -1 : 1;
        if (dc == 0 && dr == forward) return true;
        if (crossed) {
            if (Math.abs(dc) == 1 && dr == 0) return true;
        }
        return false;
    }
}
