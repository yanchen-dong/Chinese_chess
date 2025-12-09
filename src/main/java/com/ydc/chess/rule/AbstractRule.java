package com.ydc.chess.rule;

import com.ydc.chess.model.General;
import com.ydc.chess.model.Piece;

/*
  工具方法集中在此，供各具体规则类复用。
*/
public abstract class AbstractRule implements Rule {
    protected static boolean inBounds(int r, int c) {
        return r >= 0 && r <= 9 && c >= 0 && c <= 8;
    }

    protected static boolean sameColor(Piece a, Piece b) {
        if (a == null || b == null) return false;
        return a.getColor() == b.getColor();
    }

    protected static boolean inPalace(Piece.Color color, int r, int c) {
        if (c < 3 || c > 5) return false;
        if (color == Piece.Color.RED) {
            return r >= 7 && r <= 9;
        } else {
            return r >= 0 && r <= 2;
        }
    }

    protected static Piece findOppositeGeneral(Piece[][] board, Piece.Color color) {
        for (int r = 0; r <= 9; r++) {
            for (int c = 0; c <= 8; c++) {
                Piece p = board[r][c];
                if (p != null && p instanceof General && p.getColor() != color) return p;
            }
        }
        return null;
    }

    protected static int countBetween(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (fx == tx) {
            int cnt = 0;
            for (int c = Math.min(fy, ty) + 1; c <= Math.max(fy, ty) - 1; c++) {
                if (board[fx][c] != null) cnt++;
            }
            return cnt;
        } else if (fy == ty) {
            int cnt = 0;
            for (int r = Math.min(fx, tx) + 1; r <= Math.max(fx, tx) - 1; r++) {
                if (board[r][fy] != null) cnt++;
            }
            return cnt;
        } else {
            return -1;
        }
    }
}
