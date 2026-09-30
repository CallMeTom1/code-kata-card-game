package com.arena.engine.classes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Registry of all playable classes, keyed by name; adding a class means adding one line here. */
public final class HeroClasses {

    private final Map<String, HeroClass> byName = new LinkedHashMap<>();

    public HeroClasses register(HeroClass heroClass) {
        byName.put(heroClass.name(), heroClass);
        return this;
    }

    public Optional<HeroClass> byName(String name) {
        return Optional.ofNullable(byName.get(name));
    }
}
