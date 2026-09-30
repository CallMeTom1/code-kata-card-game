# Lot C — Minions, plateau et 3 classes

> **Statut : implémenté** (voir l'historique git). Les noms de tests réels peuvent différer
> légèrement ; les chiffres finaux des cartes sont dans `DESIGN.md` (section Balance).

**Mode** : mob, un seul PC — ce fichier est une partie du backlog commun (ordre : section I de `docs/00-PHASE-0-DECISIONS.md`)
**Possède** : `engine.board`, `engine.classes` (Épéiste, Assassin, Clerc)

**Dépendances** : le plateau ne dépend que des contrats du sprint 0. Les
classes Épéiste, Assassin et Clerc réutilisent les effets du lot B : en
attendant, commencez par le plateau (M1 et M2), puis les classes (M3).

## Jalon M1 — plateau et minions

### C.1 Minion et plateau (`engine.board`)
- [x] `Minion` : nom, attaque, PV, Taunt, `summonedOnTurn`, capacité passive optionnelle.
- [x] `given_empty_board_when_minion_summoned_then_board_has_1_minion` (+ `MinionSummoned`)
- [x] `given_board_of_7_when_minion_summoned_then_summon_fizzles` (+ `SummonFizzled`)
- [x] `given_minion_at_1_hp_when_it_takes_2_damage_then_it_dies_and_leaves_board` (+ `MinionDied`)
- [x] Effet `Summon(minionTemplate, count)` dans `engine.effects` (fichier propre à C).
- [x] Ajouter `myBoard()` / `opponentBoard()` à `GameView` → **commit de contrat séparé, validé par le groupe**.

### C.2 Attaque des minions (phase ajoutée au resolve phase — coordonner avec A)
- [x] `given_minion_summoned_this_turn_when_resolve_phase_then_it_does_not_attack` (mal d'invocation)
- [x] `given_minion_from_last_turn_when_resolve_phase_then_it_hits_enemy_champion` (+ `MinionAttacked`)
- [x] `given_two_minions_when_resolve_phase_then_oldest_attacks_first`
- [x] Les coups de minion passent par `DamageResolver` (armure et Parry s'appliquent).

## Jalon M2 — Taunt et effets de zone

### C.3 Taunt (`TauntDamageResolver`, un décorateur de `DamageResolver` : Open/Closed)
- [x] `given_enemy_taunt_when_attack_card_played_then_champion_is_hit_anyway` (comme dans Hearthstone, Taunt ne bloque que les attaques de minions)
- [x] `given_enemy_taunt_when_minion_attacks_then_both_minions_trade_damage`
- [x] `given_no_taunt_when_attack_then_champion_is_hit`
- [x] Les dégâts sur minions ne comptent **pas** dans les dégâts infligés (statistiques).

### C.4 Effets de zone et minions spéciaux
- [x] `DamageAllEnemyMinions(n)` et `DamageChampionAndAllEnemyMinions(n)`
- [x] Wild Wolf et Shieldbearer (neutres, dans un fichier `NeutralMinionCards`)
- [x] Capacité « fin de tour » (Spirit Healer) et « on hit » (Venom Spider)
- [x] Fournir l'effet `Summon` au lot B pour Raise Skeletons et Iron Golem.

## Jalon M3 — classes

### C.5 Épéiste (`SwordsmanCards` + `Swordsman`)
- [x] Twin Blades (2 coups séparés), Whirlwind Slash (zone), Riposte, Focus Training, Battle Cry
- [x] Pouvoir *Sharpen* ; deck de 20 cartes ; ajout dans `HeroClasses`

### C.6 Assassin (`AssassinCards` + `Assassin`)
- [x] Backstab, Eviscerate (Combo), Deadly Poison, Venom Spider, Evasion, Preparation
- [x] Pouvoir *Poisoned Dagger* ; deck ; `HeroClasses`

### C.7 Clerc (`ClericCards` + `Cleric`)
- [x] Smite, Holy Nova (zone + soin), Power Word: Shield, Divine Blessing, Greater Heal, Spirit Healer
- [x] Pouvoir *Lesser Heal* ; deck ; `HeroClasses`
