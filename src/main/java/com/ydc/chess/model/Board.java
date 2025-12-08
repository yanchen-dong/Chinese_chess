package com.ydc.chess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Board类：管理棋盘数据结构与棋子在棋盘上的位置，负责棋子布局与应用／回退走法。
 */
public class Board {

    // grid[10][9]: Piece|null - 存储棋子引用，10行9列
    private Piece[][] grid = new Piece[10][9];
    // moveHistory: List<Move> - 存储走棋历史
    private List<Move> moveHistory = new ArrayList<>();
    public Board() {
        initialize();
    }

    /**
     * initialize() - 初始化棋盘，将所有 32 颗棋子摆放在起始位置
     */
    public void initialize() {
        // 1. 清空棋盘
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                grid[r][c] = null;
            }
        }

        // 2. 放置棋子 (使用独立的棋子类)

        // 辅助方法：创建并放置棋子
        this.placePiece(new Chariot("車", Piece.Color.BLACK, new Pos(0, 0)));
        this.placePiece(new Chariot("車", Piece.Color.BLACK, new Pos(8, 0)));
        this.placePiece(new Knight("马", Piece.Color.BLACK, new Pos(1, 0)));
        this.placePiece(new Knight("马", Piece.Color.BLACK, new Pos(7, 0)));
        this.placePiece(new Bishop("象", Piece.Color.BLACK, new Pos(2, 0)));
        this.placePiece(new Bishop("象", Piece.Color.BLACK, new Pos(6, 0)));
        this.placePiece(new Guard("士", Piece.Color.BLACK, new Pos(3, 0)));
        this.placePiece(new Guard("士", Piece.Color.BLACK, new Pos(5, 0)));
        this.placePiece(new General("将", Piece.Color.BLACK, new Pos(4, 0)));

        this.placePiece(new Cannon("炮", Piece.Color.BLACK, new Pos(1, 2)));
        this.placePiece(new Cannon("炮", Piece.Color.BLACK, new Pos(7, 2)));

        for (int c = 0; c < 9; c += 2) {
            this.placePiece(new Soldier("卒", Piece.Color.BLACK, new Pos(c, 3)));
        }

        // 放置红方 (底部)
        this.placePiece(new Chariot("俥", Piece.Color.RED, new Pos(0, 9)));
        this.placePiece(new Chariot("俥", Piece.Color.RED, new Pos(8, 9)));
        this.placePiece(new Knight("傌", Piece.Color.RED, new Pos(1, 9)));
        this.placePiece(new Knight("傌", Piece.Color.RED, new Pos(7, 9)));
        this.placePiece(new Bishop("相", Piece.Color.RED, new Pos(2, 9)));
        this.placePiece(new Bishop("相", Piece.Color.RED, new Pos(6, 9)));
        this.placePiece(new Guard("仕", Piece.Color.RED, new Pos(3, 9)));
        this.placePiece(new Guard("仕", Piece.Color.RED, new Pos(5, 9)));
        this.placePiece(new General("帅", Piece.Color.RED, new Pos(4, 9)));

        this.placePiece(new Cannon("炮", Piece.Color.RED, new Pos(1, 7)));
        this.placePiece(new Cannon("炮", Piece.Color.RED, new Pos(7, 7)));

        for (int c = 0; c < 9; c += 2) {
            this.placePiece(new Soldier("兵", Piece.Color.RED, new Pos(c, 6)));
        }

        moveHistory.clear();
        System.out.println("棋盘已初始化。");
    }

    // 辅助方法：放置棋子到棋盘
    private void placePiece(Piece piece) {
        Pos pos = piece.getPosition();
        if (pos.getX() >= 0 && pos.getX() <= 8 && pos.getY() >= 0 && pos.getY() <= 9) {
            grid[pos.getY()][pos.getX()] = piece;
        }
    }


    /**
     * getPiece(pos) - 获取指定位置的棋子
     * @param pos 坐标对象
     * @return 位于该位置的棋子，如果没有则为 null
     */
    public Piece getPiece(Pos pos) {
        if (pos.getX() < 0 || pos.getX() > 8 || pos.getY() < 0 || pos.getY() > 9) {
            return null;
        }
        return grid[pos.getY()][pos.getX()];
    }

    public void clearpicked(){
        for (int c = 0; c < 10; c++) {
            for (int r = 0; r < 9; r++) {
                if (grid[c][r]!=null)
                    grid[c][r].setpicked(false);
            }
        }
    }

    /**
     * setPiece(pos, piece) - 设置指定位置的棋子
     * (保留了之前的 setPiece/applyMove/undoMove 骨架，未完整实现)
     */
    public void setPiece(Pos pos, Piece piece) {
        if (pos.getX() >= 0 && pos.getX() <= 8 && pos.getY() >= 0 && pos.getY() <= 9) {
            grid[pos.getY()][pos.getX()] = piece;
        }
    }
    // ... 其他方法保持不变 (省略以节省空间，假设您已经有 Pos 和 Move 类)

    // 省略 applyMove, undoMove, getMoveHistory
    // ...
}