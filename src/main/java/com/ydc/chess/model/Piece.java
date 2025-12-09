// java
package com.ydc.chess.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;

/**
 * 棋子基类
 */
public class Piece {

    public enum Color { RED, BLACK }
    private final String name;
    private final Color color;
    private Pos position;
    private BooleanProperty ispickedProperty;
    private ChangeListener<Boolean> pickListener;

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

    public void setpicked(boolean picked) {
        ispickedProperty().set(picked);
    }

    public ChangeListener<Boolean> getPickListener() {
        return pickListener;
    }

    public void setPickListener(ChangeListener<Boolean> l) {
        this.pickListener = l;
    }

    /**
     * 原有移动方法，保留以兼容现有调用
     */
    public void moveTo(Pos newPos) {
        this.position = newPos;
    }

    /**
     * 新增标准 setter，供外部直接设置位置。
     */
    public void setPosition(Pos pos) {
        this.position = pos;
    }

    /**
     * 便捷的 int 参数版本
     */
    public void setPosition(int x, int y) {
        moveTo(new Pos(x, y));
    }

    public void capture() { this.position = new Pos(-1,-1);}
}
