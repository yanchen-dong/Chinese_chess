package com.ydc.chess.rule;

import com.ydc.chess.model.Cannon;
import com.ydc.chess.model.Piece;

/*
  炮规则实现
*/
public class CannonRule extends AbstractRule {
    @Override
    public boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (!(from instanceof Cannon)) return false;
        Cannon can = (Cannon) from;
        Piece dest = board[tx][ty];
        if (dest != null && sameColor(from, dest)) return false;

        if (fx != tx && fy != ty) return false;
        int between = countBetween(board, fx, fy, tx, ty);
        if (dest == null) {
            return between == 0;
        } else {
            return between == 1 && dest.getColor() != can.getColor();
        }
    }
}
