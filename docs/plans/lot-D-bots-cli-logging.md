# Lot D — Bots, CLI, log console et statistiques

**Mode** : mob, un seul PC — ce fichier est une partie du backlog commun (ordre : section I de `docs/00-PHASE-0-DECISIONS.md`)
**Possède** : `bots`, `cli`, `log`, `stats`, `Main`

**Dépendances** : tout se teste sans le moteur. Le renderer et les
statistiques consomment des `GameEvent` fabriqués à la main dans les tests ;
les bots reçoivent un faux `GameView`.

## Jalon M1 — voir un match dans le terminal

### D.1 `ConsoleRenderer` (un `GameEventListener`)
- [ ] Il écrit dans un `PrintStream` injecté (pas `System.out` en dur) → testable.
- [ ] `given_turn_started_event_when_rendered_then_prints_turn_header`
      (ex. `[T03][Alice][TURN   ] [HP 28/30] [ARMOR 0] [MANA 3/3] [HAND 4] [DECK 13]`)
- [ ] `given_damage_dealt_with_absorbed_armor_when_rendered_then_shows_absorbed_and_hp_left`
      (ex. `[T03][Alice][DAMAGE ] Fireball → Bob : 6 dmg [ABSORBED 2] [HP 26 → 22]`)
- [ ] Format de référence : section G1 de `docs/00-PHASE-0-DECISIONS.md`
      (étiquettes entre crochets, largeur fixe, une ligne par événement).
- [ ] `given_unknown_event_when_rendered_then_nothing_breaks` (Liskov : un événement inconnu est ignoré)
- [ ] Un format par événement du sprint 0, puis un par événement ajouté par A et C.
- [ ] Fin de match : résumé (gagnant, raison, tours, PV restants).

### D.2 `RandomBot`
- [ ] `given_no_affordable_card_when_asked_then_ends_turn`
- [ ] `given_seeded_random_when_asked_twice_then_same_choice` (déterminisme)
- [ ] Ne renvoie jamais une carte trop chère.

### D.3 CLI et `Main`
- [ ] `CommandLineOptions` : `--matches N`, `--p1 Bot:Class`, `--p2 Bot:Class`,
      `--seed S`, `--log` ; valeurs par défaut ; message clair si un bot ou une
      classe est inconnu.
- [ ] `given_args_p1_Aggressive_Mage_when_parsed_then_bot_is_Aggressive_and_class_is_Mage`
- [ ] `BotFactory` : `byName(String)` (une ligne par bot).
- [ ] **Intégration M1** avec A et B : `Main --matches 1 --log` affiche un match Random vs Random complet.

## Jalon M2 — statistiques et vrais bots

### D.4 `StatsCollector` (un `GameEventListener`) et `MatchRunner`
- [ ] `MatchRunner` joue N matchs avec la graine `baseSeed + i` ; `--log` n'attache le `ConsoleRenderer` qu'au premier match.
- [ ] `given_3_matches_won_2_by_p1_when_aggregated_then_p1_win_rate_is_66_7_percent`
- [ ] Taux de match nul, taux de victoire du premier joueur, durée moyenne en tours,
      dégâts moyens infligés au champion adverse par camp (hors fatigue et hors minions).
- [ ] Affichage final des stats (`StatsReport` → texte).

### D.5 `AggressiveBot` et `DefensiveBot` (règles dans `DESIGN.md`)
- [ ] Aggressive : `given_resource_and_attack_in_hand_when_asked_then_plays_resource_first`
- [ ] `given_two_attacks_affordable_when_asked_then_plays_highest_damage`
- [ ] `given_battle_cry_and_no_attack_affordable_after_when_asked_then_does_not_play_it`
- [ ] `given_2_mana_left_and_nothing_playable_when_asked_then_uses_hero_power`
- [ ] Defensive : `given_hp_15_when_heal_and_attack_in_hand_then_plays_heal_first`
- [ ] `given_hp_20_when_asked_then_behaves_like_aggressive`
- [ ] Minions : un minion offensif vaut 2× son attaque (après le lot C).
- [ ] Les bots ont besoin de connaître les dégâts d'une carte : ajouter au besoin
      une méthode `estimatedDamage()` sur `Card` → **commit de contrat séparé, validé par le groupe**.

## Jalon M3 — livrables et ouverture
- [ ] Sauvegarder un log d'exemple dans `docs/sample-match.log` (livrable n°2 de l'énoncé).
- [ ] Vérifier le skill `.claude/skills/run-simulation` et l'utiliser pour l'équilibrage.
- [ ] (Bonus, ouverture front) `JsonEventExporter` : un listener qui écrit les
      événements d'un match en JSON Lines (`--json fichier`), sans bibliothèque
      externe. Le moteur ne doit pas changer d'une ligne.
