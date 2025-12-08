package com.ydc.chess.model; // 必须是这个包名

/**
 * 代表一次走棋操作的类，用于记录历史和悔棋。
 */
public class Move { // 必须是 public class Move
    private final Pos fromPos;
    private final Pos toPos;
    private final Piece capturedPiece;

    public Move(Pos fromPos, Pos toPos, Piece capturedPiece) {
        this.fromPos = fromPos;
        this.toPos = toPos;
        this.capturedPiece = capturedPiece;
    }

    public Pos getFromPos() {
        return fromPos;
    }

    public Pos getToPos() {
        return toPos;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }
}