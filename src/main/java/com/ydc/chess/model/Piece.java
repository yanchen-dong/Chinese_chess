package com.ydc.chess.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jdk.jfr.FlightRecorder;

/**
 * 棋子基类
 */
public class Piece {


    public enum Color { RED, BLACK }
    private final String name;
    private final Color color;
    private Pos position;
    private boolean ispicked;
    private BooleanProperty ispickedProperty;

    public Piece(String name, Color color, Pos position) {
        this.name = name;
        this.color = color;
        this.position = position;
        ispickedProperty = new SimpleBooleanProperty(false);
    }

    public BooleanProperty ispickedProperty() {
        return ispickedProperty;
    }

    public void setIspickedProperty(boolean ispickedProperty) {
        this.ispickedProperty.set(ispickedProperty);
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

    public boolean ispicked() { return ispickedProperty().get(); }

    public void setpicked(boolean picked) { ispickedProperty().set(picked);
        ispicked = picked;}

    public void moveTo(Pos newPos) {
        this.position = newPos;
    }

    public void capture() { /* 模拟被吃 */ }
}