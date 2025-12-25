package com.ydc.chess.controller;

import com.ydc.chess.model.Board;
import com.ydc.chess.model.GameSettings;
import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Pos;
import com.ydc.chess.network.NetworkMessage;
import com.ydc.chess.network.NetworkService;
import com.ydc.chess.ui.BoardRenderer;
import com.ydc.chess.rule.CheckMate;
import java.util.Stack;
import com.ydc.chess.model.Move;

public class GameBoardService {

    private int redRegretLeft;
    private int blackRegretLeft;

    // ⭐ 新增：记录最后一步是谁走的（悔棋依据）
    private Piece.Color lastMoveSide = null;

    private final Board board;
    private final GameBoardView view;
    private final TimerService timerService;
    private Piece selectedPiece = null;
    private NetworkService networkService;
    private Piece.Color myColor; // 网络模式下，我的颜色（主机是红方，客机是黑方）

    private final Stack<Move> moveStack = new Stack<>();

    public GameBoardService(Board board, TimerService timerService, GameBoardView view) {
        this.board = board;
        this.view = view;
        this.timerService = timerService;
        
        // 从设置中读取悔棋次数
        GameSettings settings = GameSettings.getInstance();
        redRegretLeft = settings.getMaxRegretCount();
        blackRegretLeft = settings.getMaxRegretCount();
        
        // 检查是否是网络模式
        if (NetworkManager.isNetworkMode()) {
            networkService = NetworkManager.getNetworkService();
            // 主机是红方，客机是黑方
            myColor = networkService.isHost() ? Piece.Color.RED : Piece.Color.BLACK;
        }
    }

    // 初始化
    public void init() {
        view.refresh(board);
        view.updateTurnLabel("当前回合: 红方");
        view.appendLog("对局开始，红方先行。");
        view.updateRegretCount(redRegretLeft, blackRegretLeft);
        view.startTimer();
    }

    public int getCurrentSideRegretLeft() {
        if (lastMoveSide == null) return 0;
        return lastMoveSide == Piece.Color.RED ? redRegretLeft : blackRegretLeft;
    }

    // 回合标签更新
    public void ChangeLabel() {
        if (board.getCurrentTurn() == Piece.Color.RED) {
            view.updateTurnLabel("当前回合: 红方");
        } else {
            view.updateTurnLabel("当前回合: 黑方");
        }
    }

    // 鼠标点击处理（保持原逻辑）
    public void handleClick(double mouseX, double mouseY) {
        // 网络模式下，检查是否是当前玩家的回合
        if (networkService != null && networkService.isConnected()) {
            if (board.getCurrentTurn() != myColor) {
                view.appendLog("请等待对方走棋...");
                return;
            }
        }
        
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

            } else if (selectedPiece != null) {
                movePiece(clickedPos, piece, true);
            }

        } else if (selectedPiece != null) {
            movePiece(clickedPos, null, false);
        }
    }
    // ================= 核心走子逻辑 =================
    private void movePiece(Pos targetPos, Piece targetPiece, boolean isCapture) {
        String fromStr = selectedPiece.getPosition() != null ? selectedPiece.getPosition().toString() : "unknown";
        String toStr = targetPos.toString();
        Pos from = selectedPiece.getPosition();
        Piece captured = board.getPiece(targetPos);
        boolean success = board.move(from, targetPos);
        if (success) {
            lastMoveSide = selectedPiece.getColor();
            Move move = new Move(from, targetPos, captured);
            moveStack.push(move);
            
            // 网络模式下，发送移动消息
            if (networkService != null && networkService.isConnected()) {
                networkService.sendMove(from, targetPos);
            }
            
            if (isCapture)
            {
                view.appendLog("移动棋子: " + selectedPiece.getName() + " 从 " + fromStr + " 到 " + toStr + " 并吃掉 " + targetPiece.getName()); } else { view.appendLog("移动棋子: " + selectedPiece.getName() + " 从 " + fromStr + " 到 " + toStr);
            }
            selectedPiece = null;
            view.refresh(board);
            ChangeLabel();
            timerService.startNewTimer();
            // ======= 将军 / 将死 / 困毙 判断 =======
            Piece.Color current = board.getCurrentTurn();
            if (board.isincheck(current))
            {
                if (CheckMate.checkMate(board))
                {
                String winner = (current == Piece.Color.RED) ? "黑方" : "红方";
                view.appendLog("将死！" + winner + "获胜！");
                timerService.stop();
                view.showGameOverDialog(winner);
            } else {
                String side = (current == Piece.Color.RED) ? "红方" : "黑方";
                view.appendLog(side + "已经被将军！");
            }
            }
            else if (CheckMate.checkMate(board))
            { String winner = (current == Piece.Color.RED) ? "黑方" : "红方";
                view.appendLog("困毙！" + winner + "获胜！"); timerService.stop();
                view.showGameOverDialog(winner);
            }
        } else { 
            if (board.getCheckStatus() == Board.checkStatus.BEFORE_CHECK) {
                view.appendLog("你已被将军！");
            } else if (board.getCheckStatus() == Board.checkStatus.AFTER_CHECK) {
                view.appendLog("这一步之后你将被将军！");
            } else if (board.getCheckStatus() == Board.checkStatus.FACE_TO_FACE) {
                view.appendLog("将帅不能面对面！");
            } else {
                view.appendLog("非法移动");
            }
        }
    }
    
    /**
     * 处理网络消息 - 移动棋子
     */
    public void handleNetworkMove(Pos from, Pos to) {
        Piece piece = board.getPiece(from);
        if (piece == null) {
            view.appendLog("网络错误：找不到要移动的棋子");
            return;
        }
        
        Piece targetPiece = board.getPiece(to);
        boolean isCapture = (targetPiece != null);
        
        boolean success = board.move(from, to);
        if (success) {
            lastMoveSide = piece.getColor();
            Move move = new Move(from, to, targetPiece);
            moveStack.push(move);
            
            String fromStr = from.toString();
            String toStr = to.toString();
            if (isCapture) {
                view.appendLog("对方移动: " + piece.getName() + " 从 " + fromStr + " 到 " + toStr + " 并吃掉 " + targetPiece.getName());
            } else {
                view.appendLog("对方移动: " + piece.getName() + " 从 " + fromStr + " 到 " + toStr);
            }
            
            selectedPiece = null;
            view.refresh(board);
            ChangeLabel();
            timerService.startNewTimer();
            
            // 检查游戏结束
            Piece.Color current = board.getCurrentTurn();
            if (board.isincheck(current)) {
                if (CheckMate.checkMate(board)) {
                    String winner = (current == Piece.Color.RED) ? "黑方" : "红方";
                    view.appendLog("将死！" + winner + "获胜！");
                    timerService.stop();
                    view.showGameOverDialog(winner);
                } else {
                    String side = (current == Piece.Color.RED) ? "红方" : "黑方";
                    view.appendLog(side + "已经被将军！");
                }
            } else if (CheckMate.checkMate(board)) {
                String winner = (current == Piece.Color.RED) ? "黑方" : "红方";
                view.appendLog("困毙！" + winner + "获胜！");
                timerService.stop();
                view.showGameOverDialog(winner);
            }
        } else {
            view.appendLog("网络错误：对方移动失败");
        }
    }
    public void clearHistory() { moveStack.clear(); }

    // ================= 悔棋（唯一修改点） =================
    public boolean tryRegret() {
        // 网络模式下，检查是否是当前玩家的回合
        if (networkService != null && networkService.isConnected()) {
            if (lastMoveSide != myColor) {
                view.appendLog("只能悔自己的棋");
                return false;
            }
        }

        if (lastMoveSide == null) return false;

        int left =
                (lastMoveSide == Piece.Color.RED)
                        ? redRegretLeft
                        : blackRegretLeft;

        if (left <= 0) return false;

        boolean success = board.undo();
        if (!success) return false;

        if (lastMoveSide == Piece.Color.RED) {
            redRegretLeft--;
        } else {
            blackRegretLeft--;
        }

        // 网络模式下，发送悔棋消息
        if (networkService != null && networkService.isConnected()) {
            networkService.sendRegret();
        }

        view.refresh(board);
        ChangeLabel();
        view.updateRegretCount(redRegretLeft, blackRegretLeft);

        view.appendLog(
                (lastMoveSide == Piece.Color.RED ? "红方" : "黑方")
                        + "悔棋成功，剩余次数："
                        + ((lastMoveSide == Piece.Color.RED)
                        ? redRegretLeft
                        : blackRegretLeft)
        );

        // 本地版：一次悔棋只撤回一步
        lastMoveSide = null;

        return true;
    }
    
    /**
     * 处理网络消息 - 悔棋
     */
    public void handleNetworkRegret() {
        if (lastMoveSide == null) {
            view.appendLog("对方请求悔棋，但无法悔棋");
            return;
        }
        
        int left = (lastMoveSide == Piece.Color.RED) ? redRegretLeft : blackRegretLeft;
        if (left <= 0) {
            view.appendLog("对方请求悔棋，但悔棋次数已用尽");
            return;
        }
        
        boolean success = board.undo();
        if (success) {
            if (lastMoveSide == Piece.Color.RED) {
                redRegretLeft--;
            } else {
                blackRegretLeft--;
            }
            
            view.refresh(board);
            ChangeLabel();
            view.updateRegretCount(redRegretLeft, blackRegretLeft);
            view.appendLog("对方悔棋成功");
            
            lastMoveSide = null;
        }
    }


    // 坐标转换（原样保留）
    private Pos getLogicalPosition(double x, double y) {
        double margin = BoardRenderer.MARGIN;
        double cellSize = BoardRenderer.CELL_SIZE;

        int col = (int) Math.round((x - margin) / cellSize);
        int row = (int) Math.round((y - margin) / cellSize);

        if (col >= 0 && col < 9 && row >= 0 && row < 10) {
            return new Pos(col, row);
        }
        return null;
    }
}
