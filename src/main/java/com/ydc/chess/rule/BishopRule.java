package com.ydc.chess.rule;

import com.ydc.chess.model.Bishop;
import com.ydc.chess.model.Piece;

/*
  象（相）规则实现
*/
public class BishopRule extends AbstractRule {
    @Override
    public boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (!(from instanceof Bishop)) return false;
        Bishop e = (Bishop) from;
        Piece to = board[tx][ty];
        if (to != null && sameColor(from, to)) return false;

        int dr = tx - fx, dc = ty - fy;
        if (Math.abs(dr) != 2 || Math.abs(dc) != 2) return false;
        int mr = fx + dr / 2, mc = fy + dc / 2;
        if (board[mr][mc] != null) return false;
        if (e.getColor() == Piece.Color.RED) {
            return tx >= 5;
        } else {
            return tx <= 4;
        }
    }
}
