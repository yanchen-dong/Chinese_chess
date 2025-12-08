package com.ydc.chess.ui;

import com.ydc.chess.model.Board;
import com.ydc.chess.model.Piece;
import com.ydc.chess.model.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color; // 这是 JavaFX 的颜色，用于绘图
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;

/**
 * 负责在界面上绘制中国象棋的棋盘线条和棋子。
 */
public class BoardRenderer {

    // 棋盘参数配置
    public static final int ROWS = 10;
    public static final int COLS = 9;
    public static final double CELL_SIZE = 60.0;
    public static final double MARGIN = 40.0;
    public static final double PIECE_RADIUS = CELL_SIZE / 2 - 5;

    /**
     * 在指定的面板上绘制棋盘和棋子
     */
    public static void drawBoard(Pane boardPane, Board board) {
        boardPane.getChildren().clear();
        drawGridLines(boardPane);
        if (board != null) {
            drawPieces(boardPane, board);
        }
    }

    // 绘制网格线、楚河汉界和九宫格
    private static void drawGridLines(Pane boardPane) {
        // 绘制横线 (10条)
        for (int i = 0; i < ROWS; i++) {
            double y = MARGIN + i * CELL_SIZE;
            drawLine(boardPane, MARGIN, y, MARGIN + (COLS - 1) * CELL_SIZE, y);
        }

        // 绘制竖线 (中间断开)
        for (int i = 0; i < COLS; i++) {
            double x = MARGIN + i * CELL_SIZE;
            if (i == 0 || i == COLS - 1) {
                drawLine(boardPane, x, MARGIN, x, MARGIN + (ROWS - 1) * CELL_SIZE);
            } else {
                drawLine(boardPane, x, MARGIN, x, MARGIN + 4 * CELL_SIZE);
                drawLine(boardPane, x, MARGIN + 5 * CELL_SIZE, x, MARGIN + (ROWS - 1) * CELL_SIZE);
            }
        }

        // 绘制九宫格的斜线
        drawX(boardPane, 3, 0);
        drawX(boardPane, 3, 7);

        // 绘制“楚河 汉界”文字
        drawRiverText(boardPane);
    }

    private static void drawLine(Pane pane, double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(2.0);
        pane.getChildren().add(line);
    }

    private static void drawX(Pane pane, int startCol, int startRow) {
        double x1 = MARGIN + startCol * CELL_SIZE;
        double y1 = MARGIN + startRow * CELL_SIZE;
        double x2 = MARGIN + (startCol + 2) * CELL_SIZE;
        double y2 = MARGIN + (startRow + 2) * CELL_SIZE;
        drawLine(pane, x1, y1, x2, y2);
        drawLine(pane, x2, y1, x1, y2);
    }

    private static void drawRiverText(Pane pane) {
        double riverY = MARGIN + 4 * CELL_SIZE + (CELL_SIZE / 2) - 20;
        Label chu = new Label("楚 河");
        chu.setFont(new Font("KaiTi", 30));
        chu.setLayoutX(MARGIN + 2 * CELL_SIZE);
        chu.setLayoutY(riverY);

        Label han = new Label("汉 界");
        han.setFont(new Font("KaiTi", 30));
        han.setLayoutX(MARGIN + 5 * CELL_SIZE);
        han.setLayoutY(riverY);
        pane.getChildren().addAll(chu, han);
    }

    private static void drawPieces(Pane boardPane, Board board) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Pos pos = new Pos(c, r);
                Piece piece = board.getPiece(pos);
                if (piece != null) {
                    drawSinglePiece(boardPane, piece);
                }
            }
        }
    }

    private static void drawSinglePiece(Pane pane, Piece piece) {
        Pos pos = piece.getPosition();
        double centerX = MARGIN + pos.getX() * CELL_SIZE;
        double centerY = MARGIN + pos.getY() * CELL_SIZE;

        // 1. 绘制棋子圆盘
        Circle circle = new Circle(centerX, centerY, PIECE_RADIUS);
        circle.setFill(Color.web("#FFEBCD"));
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2.0);

        // 2. 绘制棋子文字
        Label label = new Label(piece.getName());
        label.setFont(Font.font("SimHei", FontWeight.BOLD, PIECE_RADIUS));

        Color textColor = (piece.getColor() == Piece.Color.RED) ? Color.RED : Color.BLACK;
        label.setTextFill(textColor);

        // 3. 居中对齐
        label.setLayoutX(centerX - PIECE_RADIUS);
        label.setLayoutY(centerY - PIECE_RADIUS * 0.65);
        label.setPrefWidth(PIECE_RADIUS * 2);
        label.setAlignment(javafx.geometry.Pos.CENTER);

        // 4. 交互属性
        circle.setUserData(piece);
        label.setUserData(piece);
        label.setMouseTransparent(true);

        // 添加标识，方便查找
        circle.setId("piece_circle_" + pos.getX() + "_" + pos.getY());
        label.setId("piece_label_" + pos.getX() + "_" + pos.getY());

        // 添加监听器
        piece.ispickedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // 选中状态：放大
                ScaleTransition circleTransition = new ScaleTransition(Duration.millis(200), circle);
                circleTransition.setToX(1.2);
                circleTransition.setToY(1.2);
                circleTransition.play();

                ScaleTransition labelTransition = new ScaleTransition(Duration.millis(200), label);
                labelTransition.setToX(1.2);
                labelTransition.setToY(1.2);
                labelTransition.play();

                circle.setStroke(Color.GOLD);
                circle.setStrokeWidth(3.0);

                circle.toFront();
                label.toFront();
            } else {
                // 取消选中：恢复
                ScaleTransition circleTransition = new ScaleTransition(Duration.millis(200), circle);
                circleTransition.setToX(1.0);
                circleTransition.setToY(1.0);
                circleTransition.play();

                ScaleTransition labelTransition = new ScaleTransition(Duration.millis(200), label);
                labelTransition.setToX(1.0);
                labelTransition.setToY(1.0);
                labelTransition.play();

                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(2.0);
            }
        });

        pane.getChildren().addAll(circle, label);
    }
}