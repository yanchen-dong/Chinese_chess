package com.ydc.chess.rule;

import com.ydc.chess.model.*;

/*
  简单工厂：根据 Piece 返回对应 Rule 实例（可扩展或改为单例/依赖注入）。
*/
public class RuleFactory {
    public static Rule of(Piece p) {
        if (p == null) return null;
        if (p instanceof General) return new GeneralRule();
        if (p instanceof Guard) return new GuardRule();
        if (p instanceof Bishop) return new BishopRule();
        if (p instanceof Knight) return new KnightRule();
        if (p instanceof Chariot) return new ChariotRule();
        if (p instanceof Cannon) return new CannonRule();
        if (p instanceof Soldier) return new SoldierRule();
        return null;
    }
}
