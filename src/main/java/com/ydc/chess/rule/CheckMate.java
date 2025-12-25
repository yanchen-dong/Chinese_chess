package com.ydc.chess.rule;

import com.ydc.chess.model.Board;
import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Pos;

// 判定将死的规则类
public class CheckMate {

    public static boolean checkMate(Board board) {
//        Board copy = board.cloneBoard();拷贝棋盘，如果直接改动棋盘的方法出现问题了就用
        // 遍历棋盘上的所有棋子，寻找当前玩家的将军状态
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                Piece piece = board.getPiece(new Pos(i, j));
                if (piece != null && piece.getColor() == board.getCurrentTurn()) {
                    // 检查该棋子是否有合法移动可以解除将军状态
                    if (canEscapeCheck(board, piece)) {
                        return false; // 找到一个合法移动，非将死
                    }
                }
            }
        }
        return true; // 没有合法移动，判定为将死
    }

    private static boolean canEscapeCheck(Board board, Piece piece) {
        Pos fromPos = piece.getPosition();
        Piece.Color fromColor = board.getCurrentTurn();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 10; y++) {
                Pos toPos = new Pos(x, y);
                if (RuleFactory.of(piece).isValidMove(board.getGrid(), fromPos.getY(), fromPos.getX(), toPos.getY(), toPos.getX())) {
                    // 模拟移动
                    System.out.println("尝试移动 " + piece.getName() + " 从 " + fromPos + " 到 " + toPos);
                    Piece capturedPiece = board.getPiece(toPos);
                    if (board.move(fromPos, toPos)) {
                        // 撤销模拟移动（undo会自动恢复被吃的棋子）
                        System.out.println("移动后检查将军状态...");
                        board.undo();
                        return true;
                    }
                }
            }
        }
        return false; // 没有合法移动可以解除将军状态
    }
}
