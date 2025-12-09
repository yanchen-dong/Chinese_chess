package com.ydc.chess.rule;

import com.ydc.chess.model.Bishop;
import com.ydc.chess.model.Chariot;
import com.ydc.chess.model.Cannon;
import com.ydc.chess.model.General;
import com.ydc.chess.model.Guard;
import com.ydc.chess.model.Knight;
import com.ydc.chess.model.Soldier;
import com.ydc.chess.model.Piece;

public class rule {

    /**
     * 验证从 (fx,fy) 到 (tx,ty) 的移动是否合规。
     * board: Piece[10][9]，行 0..9，列 0..8
     */
    public static boolean isValidMove(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inBounds(fx, fy) || !inBounds(tx, ty)) return false;
        Piece from = board[fx][fy];
        if (from == null) return false;
        Piece to = board[tx][ty];
        if (to != null && sameColor(from, to)) return false;

        // dispatch by piece type
        if (from instanceof General) {
            return validGeneralMove((General) from, board, fx, fy, tx, ty);
        } else if (from instanceof Guard) {
            return validGuardMove((Guard) from, board, fx, fy, tx, ty);
        } else if (from instanceof Bishop) {
            return validBishopMove((Bishop) from, board, fx, fy, tx, ty);
        } else if (from instanceof Knight) {
            return validKnightMove((Knight) from, board, fx, fy, tx, ty);
        } else if (from instanceof Chariot) {
            return validChariotMove((Chariot) from, board, fx, fy, tx, ty);
        } else if (from instanceof Cannon) {
            return validCannonMove((Cannon) from, board, fx, fy, tx, ty);
        } else if (from instanceof Soldier) {
            return validSoldierMove((Soldier) from, board, fx, fy, tx, ty);
        } else {
            // 未知棋子类型，保守返回 false
            return false;
        }
    }

    private static boolean inBounds(int r, int c) {
        return r >= 0 && r <= 9 && c >= 0 && c <= 8;
    }

    private static boolean sameColor(Piece a, Piece b) {
        return a.getColor() == b.getColor();
    }

    /* ==================== 将（帅） ==================== */
    private static boolean validGeneralMove(General g, Piece[][] board, int fx, int fy, int tx, int ty) {
        // 必须在九宫内，且只能走一步直线
        if (!inPalace(g.getColor(), tx, ty)) return false;
        int dr = Math.abs(tx - fx);
        int dc = Math.abs(ty - fy);
        if (dr + dc != 1) return false;

        // 不能与对方将相对面无子隔列相对（此处仅不允许直接走成对面相对的状态）
        // 检测目标是否会使双方将军正面相对（同列且中间无子）
        Piece other = findOppositeGeneral(board, g.getColor());
        if (other != null) {
            // 查找对方将的位置
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
                if (!block) {
                    // 如果走后与对方将同列且中间无子，则不允许（将面相对）
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean inPalace(Piece.Color color, int r, int c) {
        if (c < 3 || c > 5) return false;
        if (color == Piece.Color.RED) {
            return r >= 7 && r <= 9;
        } else {
            return r >= 0 && r <= 2;
        }
    }

    private static Piece findOppositeGeneral(Piece[][] board, Piece.Color color) {
        for (int r = 0; r <= 9; r++) {
            for (int c = 0; c <= 8; c++) {
                Piece p = board[r][c];
                if (p != null && p instanceof General && p.getColor() != color) return p;
            }
        }
        return null;
    }

    /* ==================== 士（仕） ==================== */
    private static boolean validGuardMove(Guard s, Piece[][] board, int fx, int fy, int tx, int ty) {
        if (!inPalace(s.getColor(), tx, ty)) return false;
        int dr = Math.abs(tx - fx), dc = Math.abs(ty - fy);
        return dr == 1 && dc == 1;
    }

    /* ==================== 象（相） ==================== */
    private static boolean validBishopMove(Bishop e, Piece[][] board, int fx, int fy, int tx, int ty) {
        // 象走田字，两步斜，不能越过河界（不得进入对方半场）
        int dr = tx - fx, dc = ty - fy;
        if (Math.abs(dr) != 2 || Math.abs(dc) != 2) return false;
        // 象眼
        int mr = fx + dr / 2, mc = fy + dc / 2;
        if (board[mr][mc] != null) return false;
        // 不得过河
        if (e.getColor() == Piece.Color.RED) {
            return tx >= 5; // 红方只能在行 5..9
        } else {
            return tx <= 4; // 黑方只能在行 0..4
        }
    }

    /* ==================== 马 ==================== */
    private static boolean validKnightMove(Knight m, Piece[][] board, int fx, int fy, int tx, int ty) {
        int dr = tx - fx, dc = ty - fy;
        int adr = Math.abs(dr), adc = Math.abs(dc);
        if (!((adr == 2 && adc == 1) || (adr == 1 && adc == 2))) return false;
        // 马脚阻挡检测
        if (adr == 2) {
            int br = fx + dr / 2, bc = fy;
            if (board[br][bc] != null) return false;
        } else {
            int br = fx, bc = fy + dc / 2;
            if (board[br][bc] != null) return false;
        }
        return true;
    }

    /* ==================== 车 ==================== */
    private static boolean validChariotMove(Chariot ch, Piece[][] board, int fx, int fy, int tx, int ty) {
        if (fx != tx && fy != ty) return false;
        return countBetween(board, fx, fy, tx, ty) == 0;
    }

    /* ==================== 炮 ==================== */
    private static boolean validCannonMove(Cannon can, Piece[][] board, int fx, int fy, int tx, int ty) {
        if (fx != tx && fy != ty) return false;
        int between = countBetween(board, fx, fy, tx, ty);
        Piece dest = board[tx][ty];
        if (dest == null) {
            // 不吃子时，路径不能有其他棋子
            return between == 0;
        } else {
            // 吃子时，中间必须恰有 1 个棋子作为炮架
            return between == 1 && dest.getColor() != can.getColor();
        }
    }

    /* ==================== 兵（卒） ==================== */
    private static boolean validSoldierMove(Soldier s, Piece[][] board, int fx, int fy, int tx, int ty) {
        int dr = tx - fx, dc = ty - fy;
        Piece.Color color = s.getColor();
        // 判断是否过河
        boolean crossed = (color == Piece.Color.RED) ? (fx <= 4) : (fx >= 5);
        // 向前方向
        int forward = (color == Piece.Color.RED) ? -1 : 1;
        if (dc == 0 && dr == forward) {
            // 向前一步合法
            return true;
        }
        if (crossed) {
            // 过河后可左右走一步
            if (Math.abs(dc) == 1 && dr == 0) return true;
        }
        return false;
    }

    /* ==================== 工具：统计两点之间的棋子数量（不含端点） ==================== */
    private static int countBetween(Piece[][] board, int fx, int fy, int tx, int ty) {
        if (fx == tx) {
            int c1 = Math.min(fy, ty) + 1, c2 = Math.max(fy, ty) - 0;
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
            return -1; // 不在同一行或列
        }
    }
}
