# Lot C — Minions, plateau et 3 classes

**Mode** : mob, un seul PC — ce fichier est une partie du backlog commun (ordre : section I de `docs/00-PHASE-0-DECISIONS.md`)
**Possède** : `engine.board`, `engine.classes` (Épéiste, Assassin, Clerc)

**Dépendances** : le plateau ne dépend que des contrats du sprint 0. Les
classes Épéiste, Assassin et Clerc réutilisent les effets du lot B : en
attendant, commencez par le plateau (M1 et M2), puis les classes (M3).

## Jalon M1 — plateau et minions

### C.1 Minion et plateau (`engine.board`)
- [ ] `Minion` : nom, attaque, PV, Taunt, `summonedOnTurn`, capacité passive optionnelle.
- [ ] `given_empty_board_when_minion_summoned_then_board_has_1_minion` (+ `MinionSummoned`)
- [ ] `given_board_of_5_when_minion_summoned_then_summon_fizzles` (+ `SummonFizzled`)
- [ ] `given_minion_at_1_hp_when_it_takes_2_damage_then_it_dies_and_leaves_board` (+ `MinionDied`)
- [ ] Effet `Summon(minionTemplate, count)` dans `engine.effects` (fichier propre à C).
- [ ] Ajouter `myBoard()` / `opponentBoard()` à `GameView` → **commit de contrat séparé, validé par le groupe**.

### C.2 Attaque des minions (phase ajoutée au resolve phase — coordonner avec A)
- [ ] `given_minion_summoned_this_turn_when_resolve_phase_then_it_does_not_attack` (mal d'invocation)
- [ ] `given_minion_from_last_turn_when_resolve_phase_then_it_hits_enemy_champion` (+ `MinionAttacked`)
- [ ] `given_two_minions_when_resolve_phase_then_oldest_attacks_first`
- [ ] Les coups de minion passent par `DamageResolver` (armure et Parry s'appliquent).

## Jalon M2 — Taunt et effets de zone

### C.3 Taunt (`TauntDamageResolver`, un décorateur de `DamageResolver` : Open/Closed)
- [ ] `given_enemy_taunt_when_attack_card_played_then_taunt_is_hit_instead_of_champion`
- [ ] `given_enemy_taunt_when_minion_attacks_then_both_minions_trade_damage`
- [ ] `given_no_taunt_when_attack_then_champion_is_hit`
- [ ] Les dégâts sur minions ne comptent **pas** dans les dégâts infligés (statistiques).

### C.4 Effets de zone et minions spéciaux
- [ ] `DamageAllEnemyMinions(n)` et `DamageChampionAndAllEnemyMinions(n)`
- [ ] Wild Wolf et Shieldbearer (neutres, dans un fichier `NeutralMinionCards`)
- [ ] Capacité « fin de tour » (Spirit Healer) et « on hit » (Venom Spider)
- [ ] Fournir l'effet `Summon` au lot B pour Raise Skeletons et Iron Golem.

## Jalon M3 — classes

### C.5 Épéiste (`SwordsmanCards` + `Swordsman`)
- [ ] Twin Blades (2 coups séparés), Whirlwind Slash (zone), Riposte, Focus Training, Battle Cry
- [ ] Pouvoir *Sharpen* ; deck de 20 cartes ; ajout dans `HeroClasses`

### C.6 Assassin (`AssassinCards` + `Assassin`)
- [ ] Backstab, Eviscerate (Combo), Deadly Poison, Venom Spider, Evasion, Preparation
- [ ] Pouvoir *Poisoned Dagger* ; deck ; `HeroClasses`

### C.7 Clerc (`ClericCards` + `Cleric`)
- [ ] Smite, Holy Nova (zone + soin), Power Word: Shield, Divine Blessing, Greater Heal, Spirit Healer
- [ ] Pouvoir *Lesser Heal* ; deck ; `HeroClasses`
