# Lot A — Moteur de match

> **Statut : implémenté** (voir l'historique git). Les noms de tests réels peuvent différer
> légèrement ; les chiffres finaux des cartes sont dans `DESIGN.md` (section Balance).

**Mode** : mob, un seul PC — ce fichier est une partie du backlog commun (ordre : section I de `docs/00-PHASE-0-DECISIONS.md`)
**Possède** : `engine.match` (hors `Bot`/`GameView`/`Action`), `engine.player`, `engine.combat`

**Dépendances** : pour tester sans attendre le lot B, créez des cartes de
test avec des lambdas (`ctx -> ctx.damage().deal("Test", ctx.opponent(), 3)`).

## Jalon M1 — un match complet tourne

### A.1 Mise en place
- [x] `given_two_champions_when_match_starts_then_each_has_30_hp_and_shuffled_deck`
- [x] `given_seed_42_when_match_starts_twice_then_both_decks_are_in_same_order` (déterminisme)
- [x] `given_coin_flip_when_match_starts_then_first_player_has_3_cards_and_second_has_4_plus_the_coin`
- [x] Mulligan : `given_bot_puts_back_2_cards_when_mulligan_then_hand_size_unchanged_and_cards_shuffled_back` (+ `MulliganDone`)
- [x] Publie `MatchStarted`.

### A.2 Phases du tour
- [x] Draw : `given_non_empty_deck_when_draw_phase_then_top_card_goes_to_hand` (+ `CardDrawn`)
- [x] `given_hand_of_10_when_draw_phase_then_card_is_burned` (+ `CardBurned`)
- [x] `given_empty_deck_when_draw_phase_then_fatigue_deals_1_then_2_then_3` (+ `FatigueDamage`)
- [x] Mana : `given_turn_3_when_mana_phase_then_max_mana_is_3_and_mana_is_refilled` ; plafond à 10 (+ `ManaRefilled`)
- [x] Play : `given_bot_plays_affordable_card_when_play_phase_then_mana_is_spent` ;
      `given_bot_plays_unaffordable_card_when_play_phase_then_turn_ends` ;
      `given_bot_uses_hero_power_twice_when_play_phase_then_second_use_is_refused`
- [x] Carte `immediate` appliquée tout de suite ; les autres vont dans la file du resolve phase.
- [x] Resolve : `given_armor_then_shield_slam_played_when_resolve_phase_then_effects_apply_in_play_order`
- [x] `given_opponent_dies_mid_queue_when_resolve_phase_then_match_ends_immediately`
- [x] `given_both_champions_at_0_hp_when_checked_then_match_is_a_draw`
- [x] `given_10_mana_when_the_coin_is_played_then_mana_stays_10`
- [x] End : `given_one_champion_at_0_hp_when_end_phase_then_match_ends_with_winner` (+ `MatchEnded`)
- [x] `TurnStarted` publié à chaque tour ; un « tour » = une manche des deux joueurs.

### A.3 Boucle de match
- [x] `given_two_scripted_bots_when_match_runs_then_it_ends_before_turn_51`
- [x] `Match.play()` renvoie un `MatchResult` (gagnant, raison, nombre de tours, dégâts infligés par chaque camp).
- [x] **Intégration M1** avec le lot D : `Main` joue Random vs Random avec les cartes neutres de B.

## Jalon M2 — règles de combat

### A.4 Armure et Parry (`ArmorDamageResolver` remplace `SimpleDamageResolver`)
- [x] `given_armor_3_when_4_damage_then_hp_loses_1_and_armor_is_gone`
- [x] `given_two_armors_when_damage_then_they_stack`
- [x] `given_armor_2_turns_when_owner_starts_2nd_next_turn_then_armor_expires` (+ `ArmorExpired`)
- [x] `given_parry_2_when_hit_of_3_then_1_damage_passes` (Parry après l'armure)
- [x] `given_damage_ignoring_armor_when_dealt_then_hp_is_hit_directly` (pour Eviscerate/Poison)
- [x] `absorbed` renseigné dans `DamageDealt`.

### A.5 Statuts de tour
- [x] Poison : dégâts au début du tour de la victime, ignore l'armure (+ `PoisonTicked`)
- [x] Freeze : 1 mana de moins au prochain tour de l'adversaire
- [x] Combo : `EffectContext` indique si une carte a déjà été jouée ce tour
- [x] Evasion : la prochaine carte Attack qui touche inflige 0
- [x] Buff « prochaine attaque +X » stocké sur le champion, consommé par la prochaine carte Attack

### A.6 Fin après 50 tours (`TieBreaker`, une classe à part)
- [x] `given_turn_50_ends_when_hp_differ_then_higher_hp_wins`
- [x] `given_equal_hp_when_tie_break_then_more_damage_dealt_wins`
- [x] `given_equal_hp_and_damage_when_tie_break_then_draw`

## Jalon M3
- [x] Revue des événements : toute modification d'état publie un événement (test avec `RecordingListener` sur un match complet).
- [x] Aider C à brancher le plateau (phase d'attaque des minions dans le resolve phase).
