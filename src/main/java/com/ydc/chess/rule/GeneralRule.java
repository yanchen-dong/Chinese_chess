package com.ydc.chess.rule;

import com.ydc.chess.model.General;
import com.ydc.chess.model.Piece;

import java.util.Objects;

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

        // 检查将帅面对面规则
        // 找到对方将的位置
        int otherGeneralRow = -1, otherGeneralCol = -1;
        for (int r = 0; r <= 9; r++) {
            for (int c = 0; c <= 8; c++) {
                Piece p = board[r][c];
                if (p != null && p instanceof General && p.getColor() != g.getColor()) {
                    otherGeneralRow = r;
                    otherGeneralCol = c;
                    break;
                }
            }
            if (otherGeneralRow != -1) break;
        }
        
        // 如果找到了对方将，检查移动后是否面对面
        // 注意：如果目标是对方将（吃将），这是允许的，不需要检查面对面
        if (otherGeneralRow != -1 && otherGeneralCol != -1) {
            // 检查移动后两个将是否在同一列
            if (otherGeneralCol == ty) {
                // 如果目标是对方将的位置（吃将），允许移动
                if (tx == otherGeneralRow && ty == otherGeneralCol) {
                    // 这是吃将，允许
                    return true;
                }
                
                // 计算两个将之间的行范围（移动后的位置）
                int minRow = Math.min(tx, otherGeneralRow);
                int maxRow = Math.max(tx, otherGeneralRow);
                
                // 检查两个将之间是否有棋子阻挡
                boolean hasBlock = false;
                for (int r = minRow + 1; r < maxRow; r++) {
                    if (board[r][ty] != null) {
                        hasBlock = true;
                        break;
                    }
                }
                
                // 如果没有阻挡，则两个将面对面，不允许移动
                if (!hasBlock) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
