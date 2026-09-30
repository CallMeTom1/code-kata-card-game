# Lot B — Effets, cartes neutres, Mage et Tank

**Mode** : mob, un seul PC — ce fichier est une partie du backlog commun (ordre : section I de `docs/00-PHASE-0-DECISIONS.md`)
**Possède** : `engine.effects`, `engine.cards`, `engine.classes` (Mage, Tank)

**Principe** : une carte n'a **pas** de code propre. C'est un nom, un coût,
une catégorie et une composition d'`Effect` réutilisables (Open/Closed).
Les effets sont testés un par un ; les cartes sont testées par un test
de « fiche » : coût, catégorie, effet de bout en bout.

## Jalon M1 — effets de base et cartes neutres

### B.1 Effets (un fichier par effet dans `engine.effects`)
- [ ] `DealDamage(n)` : `given_opponent_30_hp_when_deal_damage_4_then_opponent_has_26_hp` (passe par `ctx.damage()`)
- [ ] `Heal(n)` : `given_20_hp_when_heal_5_then_25_hp` ; `given_28_hp_when_heal_5_then_capped_at_30` (+ `Healed`)
- [ ] `DrawCards(n)` : passe par la pioche du champion (fatigue gérée par le lot A)
- [ ] `GainMaxMana(n)` : plafonné à 10 (+ `ManaGained`)
- [ ] `GainTempMana(n)` : seulement pour ce tour (The Coin, Preparation)
- [ ] `GainArmor(amount, turns)` : délègue au champion (structure d'armure du lot A ; en attendant, champ simple)
- [ ] `andThen` : `given_heal_and_draw_composed_when_applied_then_both_happen_in_order`

### B.2 Cartes neutres (`NeutralCards`)
- [ ] Les 8 cartes neutres sans minion de `DESIGN.md` (Quick Jab, Strike, Crushing
      Blow, Wooden Shield, Iron Wall, Mana Crystal, Insight, Healing Potion).
      Wild Wolf et Shieldbearer sont pour le lot C.
- [ ] The Coin (0 mana, `GainTempMana(1)`, immediate).
- [ ] Test paramétré : chaque carte a le coût, la catégorie et le flag
      `immediate` de `DESIGN.md`.
- [ ] Un `DeckBuilder` : `given_deck_list_when_built_then_it_has_20_cards` ;
      refuse un deck qui n'a pas 20 cartes.
- [ ] **Intégration M1** : un deck « neutre » de 20 cartes pour le premier match Random vs Random.

## Jalon M2 — Mage et Tank

### B.3 Effets supplémentaires
- [ ] `DealDamageEqualToArmor` (Shield Slam)
- [ ] `ApplyFreeze`, `BuffNextAttack(n)`, `DealDamageIgnoringArmor(n)` (s'appuient sur les statuts du lot A — se coordonner)

### B.4 Mage (`MageCards` + `Mage`)
- [ ] Frostbolt, Fireball, Pyroblast, Ice Barrier, Arcane Intellect
- [ ] Raise Skeletons : **attendre le lot C** (effet `Summon`), ou l'ajouter une fois C mergé
- [ ] Pouvoir *Fireblast* : `given_mage_with_2_mana_when_hero_power_then_opponent_takes_1_damage`
- [ ] Deck Mage de 20 cartes conforme à `DESIGN.md` ; ajout dans `HeroClasses`

### B.5 Tank (`TankCards` + `Tank`)
- [ ] Shield Slam, Shield Block, Fortress, War Chest, Last Stand
- [ ] Iron Golem : après le lot C (effet `Summon` avec Taunt)
- [ ] Pouvoir *Armor Up* : Armor 2 pendant 3 tours
- [ ] Deck Tank de 20 cartes ; ajout dans `HeroClasses`

## Jalon M3
- [ ] Ajouter Raise Skeletons et Iron Golem une fois le lot C mergé.
- [ ] Équilibrage avec le skill `run-simulation` : proposer des changements de chiffres, les reporter dans `DESIGN.md`.
