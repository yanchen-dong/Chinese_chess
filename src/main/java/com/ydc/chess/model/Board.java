
package com.ydc.chess.model;

import com.ydc.chess.rule.RuleFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Board类：管理棋盘数据结构与棋子在棋盘上的位置，负责棋子布局与应用／回退走法。
 */
public class Board {

    public enum checkStatus {
        NONE,
        BEFORE_CHECK,
        AFTER_CHECK
    }//被将军的状态
    private checkStatus checkstatus;
    // grid[10][9]: grid[row][col]
    private Piece[][] grid = new Piece[10][9];
    // moveHistory: List<Move> - 存储走棋历史
    private List<Move> moveHistory = new ArrayList<>();
    // 当前执子方（红先）
    private Piece.Color currentTurn = Piece.Color.RED;

    public Board() {
        initialize();
    }

    /**
     * 初始化棋盘，将棋子摆回起始位置
     */
    public void initialize() {
        // 清空棋盘
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                grid[r][c] = null;
            }
        }

        // 放置黑方（上方，行较小）
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

        // 放置红方（下方，行较大）
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
        checkstatus = checkStatus.NONE;
        currentTurn = Piece.Color.RED;
        System.out.println("棋盘已初始化。");
    }

    // 放置棋子到棋盘（Pos: x=列, y=行）
    private void placePiece(Piece piece) {
        Pos pos = piece.getPosition();
        if (pos == null) return;
        if (pos.getX() >= 0 && pos.getX() <= 8 && pos.getY() >= 0 && pos.getY() <= 9) {
            grid[pos.getY()][pos.getX()] = piece;
        }
    }

    public Piece getPiece(Pos pos) {
        if (pos == null) return null;
        if (pos.getX() < 0 || pos.getX() > 8 || pos.getY() < 0 || pos.getY() > 9) {
            return null;
        }
        return grid[pos.getY()][pos.getX()];
    }

    public Piece[][] getGrid() {
        return grid;
    }

    public Piece.Color getCurrentTurn() {
        return currentTurn;
    }

    public checkStatus getCheckStatus() { return checkstatus; }

    public void clearpicked(){
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] != null) {
                    grid[r][c].setpicked(false);
                }
            }
        }
    }

    public void setPiece(Pos pos, Piece piece) {
        if (pos == null) return;
        if (pos.getX() >= 0 && pos.getX() <= 8 && pos.getY() >= 0 && pos.getY() <= 9) {
            grid[pos.getY()][pos.getX()] = piece;
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r <= 9 && c >= 0 && c <= 8;
    }

    /**
     * 使用坐标移动（行,列）
     * fr: 起始行, fc: 起始列, tr: 目标行, tc: 目标列
     */
    public boolean move(int fr, int fc, int tr, int tc) {
        checkstatus = checkStatus.NONE;
        if (!inBounds(fr, fc) || !inBounds(tr, tc)) return false;
        Piece from = grid[fr][fc];
        if (from == null) return false;
        if (from.getColor() != currentTurn) return false;
        Piece to = grid[tr][tc];
        if (to != null && to.getColor() == from.getColor()) return false;
        if (isInCheck(currentTurn)) {
            checkstatus = checkStatus.BEFORE_CHECK;
        }

        // 基本走法校验
        if (!RuleFactory.of(from).isValidMove(grid,fr,fc,tr,tc)) return false;

        // 模拟走子
        Piece captured = grid[tr][tc];
        grid[tr][tc] = from;
        grid[fr][fc] = null;

        // 检查自将（走后本方是否被将军）
        if (isInCheck(currentTurn)) {
            // 恢复
            grid[fr][fc] = from;
            grid[tr][tc] = captured;
            if (checkstatus != checkStatus.BEFORE_CHECK)
              checkstatus = checkStatus.AFTER_CHECK;
            return false;
        }

        // 更新棋子对象位置（Pos: x=列, y=行）
        from.setPosition(new Pos(tc, tr));//添加setPosition方法

        // 若被吃子对象存在，可视需要将其 position 清空或保持旧值（此处保留旧值以便历史回退）
        // 记录历史（Pos 构造为 x=列, y=行）
        moveHistory.add(new Move(new Pos(fc, fr), new Pos(tc, tr), captured));
        // 判断是否将军（走子方将对方军）
        Piece.Color enemy =
                (currentTurn == Piece.Color.RED)
                        ? Piece.Color.BLACK
                        : Piece.Color.RED;

        if (isInCheck(enemy)) {
            checkedColor = enemy;
        } else {
            checkedColor = null;
        }

// 取消所有已选中状态，便于 UI 更新
        clearpicked();

// 切换回合
        currentTurn = enemy;

        return true;

    }

    public boolean move(Pos from, Pos to) {
        if (from == null || to == null) return false;
        return move(from.getY(), from.getX(), to.getY(), to.getX());
    }

    private Pos findGeneralPos(Piece.Color color) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                Piece p = grid[r][c];
                if (p instanceof General && p.getColor() == color) {
                    return new Pos(c, r);
                }
            }
        }
        return null;
    }
    // 记录当前是否有一方被将军
    private Piece.Color checkedColor = null;
    public Piece.Color getCheckedColor() {
        return checkedColor;
    }
    /**
     * 克隆棋盘（只深拷贝棋子和棋盘布局，不拷贝历史）
     */
    public Board cloneBoard() {
        Board copy = new Board();

        // 不调用 initialize，重新创建一个空棋盘
        copy.grid = new Piece[10][9];

        // 深拷贝每一个棋子
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                Piece p = this.grid[r][c];
                if (p != null) {
                    copy.grid[r][c] = Piece.clonePieceSimple(p);
                }
            }
        }

        // 拷贝状态
        copy.currentTurn = this.currentTurn;
        copy.checkstatus = this.checkstatus;

        // moveHistory 不拷贝（模拟棋盘无需）
        copy.moveHistory = new ArrayList<>();

        return copy;
    }


    /**
     * 判断 color 方是否被将（任一敌方棋子按基本走法可吃将）
     */
    private boolean isInCheck(Piece.Color color) {
        Pos gpos = findGeneralPos(color);
        if (gpos == null) {
            // 没有将视为被将（或异常），禁止该走法
            return true;
        }
        int gr = gpos.getY(), gc = gpos.getX();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor() != color) {
                    if (RuleFactory.of(p).isValidMove(grid, r, c, gr, gc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isincheck(Piece.Color color) {
        return isInCheck(color);
    }
    // 强制移动，不进行合法性判断

    public boolean undo() {
        if (moveHistory.isEmpty()) return false;
        Move last = moveHistory.remove(moveHistory.size() - 1);
        Pos from = last.getFromPos();
        Pos to = last.getToPos();
        Piece captured = last.getCapturedPiece();

        // 将移动回去（注意 Pos 为 x=列,y=行）
        Piece moved = grid[to.getY()][to.getX()];
        grid[from.getY()][from.getX()] = moved;
        grid[to.getY()][to.getX()] = captured;

        // 同步棋子对象的位置
        if (moved != null) {
            moved.setPosition(new Pos(from.getX(), from.getY()));
        }
        if (captured != null) {
            // 被吃的棋子复位到被吃位置（以便 UI/逻辑一致）
            captured.setPosition(new Pos(to.getX(), to.getY()));
        }

        // 取消所有已选中状态，便于 UI 更新
        clearpicked();
        // 切换回合
        currentTurn = (currentTurn == Piece.Color.RED) ? Piece.Color.BLACK : Piece.Color.RED;
        return true;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }
}
