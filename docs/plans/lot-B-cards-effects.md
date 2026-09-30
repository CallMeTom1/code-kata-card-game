# Lot B — Effets, cartes neutres, Mage et Tank

> **Statut : implémenté** (voir l'historique git). Les noms de tests réels peuvent différer
> légèrement ; les chiffres finaux des cartes sont dans `DESIGN.md` (section Balance).

**Mode** : mob, un seul PC — ce fichier est une partie du backlog commun (ordre : section I de `docs/00-PHASE-0-DECISIONS.md`)
**Possède** : `engine.effects`, `engine.cards`, `engine.classes` (Mage, Tank)

**Principe** : une carte n'a **pas** de code propre. C'est un nom, un coût,
une catégorie et une composition d'`Effect` réutilisables (Open/Closed).
Les effets sont testés un par un ; les cartes sont testées par un test
de « fiche » : coût, catégorie, effet de bout en bout.

## Jalon M1 — effets de base et cartes neutres

### B.1 Effets (un fichier par effet dans `engine.effects`)
- [x] `DealDamage(n)` : `given_opponent_30_hp_when_deal_damage_4_then_opponent_has_26_hp` (passe par `ctx.damage()`)
- [x] `Heal(n)` : `given_20_hp_when_heal_5_then_25_hp` ; `given_28_hp_when_heal_5_then_capped_at_30` (+ `Healed`)
- [x] `DrawCards(n)` : passe par la pioche du champion (fatigue gérée par le lot A)
- [x] `GainMaxMana(n)` : plafonné à 10 (+ `ManaGained`)
- [x] `GainTempMana(n)` : seulement pour ce tour (The Coin, Preparation)
- [x] `GainArmor(amount, turns)` : délègue au champion (structure d'armure du lot A ; en attendant, champ simple)
- [x] `andThen` : `given_heal_and_draw_composed_when_applied_then_both_happen_in_order`

### B.2 Cartes neutres (`NeutralCards`)
- [x] Les 8 cartes neutres sans minion de `DESIGN.md` (Quick Jab, Strike, Crushing
      Blow, Wooden Shield, Iron Wall, Mana Crystal, Insight, Healing Potion).
      Wild Wolf et Shieldbearer sont pour le lot C.
- [x] The Coin (0 mana, `GainTempMana(1)`, immediate).
- [x] Test paramétré : chaque carte a le coût, la catégorie et le flag
      `immediate` de `DESIGN.md`.
- [x] Un `DeckBuilder` : `given_deck_list_when_built_then_it_has_20_cards` ;
      refuse un deck qui n'a pas 20 cartes.
- [x] **Intégration M1** : un deck « neutre » de 20 cartes pour le premier match Random vs Random.

## Jalon M2 — Mage et Tank

### B.3 Effets supplémentaires
- [x] `DealDamageEqualToArmor` (Shield Slam)
- [x] `ApplyFreeze`, `BuffNextAttack(n)`, `DealDamageIgnoringArmor(n)` (s'appuient sur les statuts du lot A — se coordonner)

### B.4 Mage (`MageCards` + `Mage`)
- [x] Frostbolt, Fireball, Pyroblast, Ice Barrier, Arcane Intellect
- [x] Raise Skeletons : **attendre le lot C** (effet `Summon`), ou l'ajouter une fois C mergé
- [x] Pouvoir *Fireblast* : `given_mage_with_2_mana_when_hero_power_then_opponent_takes_1_damage`
- [x] Deck Mage de 20 cartes conforme à `DESIGN.md` ; ajout dans `HeroClasses`

### B.5 Tank (`TankCards` + `Tank`)
- [x] Shield Slam, Shield Block, Fortress, War Chest, Last Stand
- [x] Iron Golem : après le lot C (effet `Summon` avec Taunt)
- [x] Pouvoir *Armor Up* : Armor 2 pendant 3 tours
- [x] Deck Tank de 20 cartes ; ajout dans `HeroClasses`

## Jalon M3
- [x] Ajouter Raise Skeletons et Iron Golem une fois le lot C mergé.
- [x] Équilibrage avec le skill `run-simulation` : proposer des changements de chiffres, les reporter dans `DESIGN.md`.
