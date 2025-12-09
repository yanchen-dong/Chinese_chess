package com.ydc.chess.controller;

import com.ydc.chess.model.Board;
import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Pos;
import com.ydc.chess.ui.BoardRenderer;

public class GameBoardService {

    private final Board board;
    private final GameBoardView view;
    private Piece selectedPiece = null;

    public GameBoardService(Board board, GameBoardView view) {
        this.board = board;
        this.view = view;
    }

    // 初始化：刷新视图并写初始日志/回合
    public void init() {
        view.refresh(board);
        view.updateTurnLabel("当前回合: 红方");
        view.appendLog("对局开始，红方先行。");
    }

    // 外部可以调用强制刷新
    public void refreshBoard() {
        view.refresh(board);
    }

    // 处理点击（由控制器将鼠标坐标传入）
    public void handleClick(double mouseX, double mouseY) {
        Pos clickedPos = getLogicalPosition(mouseX, mouseY);
        if (clickedPos == null) return;

        Piece piece = board.getPiece(clickedPos);

        if (piece != null) {
            if (piece == selectedPiece) {
                piece.setpicked(false);
                selectedPiece = null;
                view.appendLog("取消选中: " + piece.getName() + " " + clickedPos);
            } else if (piece.getColor() == board.getCurrentTurn()) {
                board.clearpicked();
                piece.setpicked(true);
                selectedPiece = piece;
                view.appendLog("选中: " + piece.getName() + " " + clickedPos);
            }
        } else if (selectedPiece != null) {
            String fromStr = selectedPiece.getPosition() != null ? selectedPiece.getPosition().toString() : "unknown";
            String toStr = clickedPos.toString();

            boolean success = board.move(selectedPiece.getPosition(), clickedPos);
            if (success) {
                view.appendLog("移动棋子: " + selectedPiece.getName() + " 从 " + fromStr + " 到 " + toStr);
                selectedPiece = null;
                view.refresh(board);
            } else {
                view.appendLog("非法移动");
            }
        }
    }

    // 将像素坐标转换为逻辑坐标（保留原有容差与边界逻辑）
    private Pos getLogicalPosition(double x, double y) {
        double margin = BoardRenderer.MARGIN;
        double cellSize = BoardRenderer.CELL_SIZE;

        double offsetX = x - margin;
        double offsetY = y - margin;

        int col = (int) Math.round(offsetX / cellSize);
        int row = (int) Math.round(offsetY / cellSize);

        double clickRadius = 20.0;
        double targetX = margin + col * cellSize;
        double targetY = margin + row * cellSize;

        if (Math.abs(x - targetX) > clickRadius || Math.abs(y - targetY) > clickRadius) {
            return null;
        }

        if (col >= 0 && col < 9 && row >= 0 && row < 10) {
            return new Pos(col, row);
        }
        return null;
    }
}
