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

    /**
     * 仅用于棋子复制：新建一个相同类型、相同属性的新棋子对象。
     */
    public static Piece clonePieceSimple(Piece p) {
        Pos newPos = new Pos(p.getPosition().getX(), p.getPosition().getY());

        if (p instanceof Chariot)
            return new Chariot(p.getName(), p.getColor(), newPos);
        if (p instanceof Knight)
            return new Knight(p.getName(), p.getColor(), newPos);
        if (p instanceof Bishop)
            return new Bishop(p.getName(), p.getColor(), newPos);
        if (p instanceof Guard)
            return new Guard(p.getName(), p.getColor(), newPos);
        if (p instanceof General)
            return new General(p.getName(), p.getColor(), newPos);
        if (p instanceof Cannon)
            return new Cannon(p.getName(), p.getColor(), newPos);
        if (p instanceof Soldier)
            return new Soldier(p.getName(), p.getColor(), newPos);

        return null; // 不会发生
    }


    public void capture() { this.position = new Pos(-1,-1);}
}
