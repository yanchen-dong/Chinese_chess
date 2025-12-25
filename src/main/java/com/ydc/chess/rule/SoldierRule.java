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
        // 判断是否过河：红方兵初始在行7-9，过河后行0-4；黑方卒初始在行0-2，过河后行5-9
        // 河界在行4和行5之间，所以红方过河是 fx <= 4，黑方过河是 fx >= 5
        boolean crossed = (color == Piece.Color.RED) ? (fx <= 4) : (fx >= 5);
        int forward = (color == Piece.Color.RED) ? -1 : 1;
        
        // 只能向前走（不能后退）
        if (dr * forward < 0) return false;
        
        // 未过河：只能向前走一步
        if (!crossed) {
            if (dc == 0 && dr == forward) return true;
        } else {
            // 过河后：可以向前或左右走一步
            if (dc == 0 && dr == forward) return true;  // 向前
            if (Math.abs(dc) == 1 && dr == 0) return true;  // 左右
        }
        return false;
    }
}
