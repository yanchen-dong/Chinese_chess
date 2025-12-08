package com.ydc.chess.model;

/**
 * 棋子基类
 */
public class Piece {
    public enum Color { RED, BLACK }
    private final String name;
    private final Color color;
    private Pos position;

    public Piece(String name, Color color, Pos position) {
        this.name = name;
        this.color = color;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public Pos getPosition() {
        return position;
    }

    public void moveTo(Pos newPos) {
        this.position = newPos;
    }

    public void capture() { /* 模拟被吃 */ }
}