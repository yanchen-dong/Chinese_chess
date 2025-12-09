package com.ydc.chess.rule;

import com.ydc.chess.model.General;
import com.ydc.chess.model.Piece;

/*
  将（帅）规则实现
*/
public class GeneralRule extends AbstractRule {
    @Override
    public boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (!(from instanceof General)) return false;
        General g = (General) from;

        Piece to = board[tx][ty];
        if (to != null && sameColor(from, to)) return false;

        if (!inPalace(g.getColor(), tx, ty)) return false;
        int dr = Math.abs(tx - fx);
        int dc = Math.abs(ty - fy);
        if (dr + dc != 1) return false;

        Piece other = findOppositeGeneral(board, g.getColor());
        if (other != null) {
            int or = -1, oc = -1;
            for (int r = 0; r <= 9; r++) {
                for (int c = 0; c <= 8; c++) {
                    Piece p = board[r][c];
                    if (p != null && p instanceof General && p.getColor() != g.getColor()) {
                        or = r; oc = c;
                        break;
                    }
                }
                if (or != -1) break;
            }
            if (oc == ty) {
                int min = Math.min(tx, or), max = Math.max(tx, or);
                boolean block = false;
                for (int r = min + 1; r < max; r++) {
                    if (board[r][oc] != null) { block = true; break; }
                }
                if (!block) return false;
            }
        }
        return true;
    }
}
