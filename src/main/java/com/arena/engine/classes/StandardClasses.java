package com.arena.engine.classes;

import java.util.List;

/** The 5 classes of DESIGN.md; the one line to change when a class is added. */
public final class StandardClasses {

    private StandardClasses() {
    }

    /** All classes, in the order of DESIGN.md. */
    public static HeroClasses all() {
        return new HeroClasses(List.of(Mage.definition(), Tank.definition(), Swordsman.definition(),
                Assassin.definition(), Cleric.definition()));
    }
}
