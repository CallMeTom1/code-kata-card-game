# Sprint 0 — Squelette et contrats partagés (les 4, en mob)

**Objectif** : en ~45 minutes, poser sur `main` tout ce dont les 4 lots ont
besoin pour travailler en parallèle sans se bloquer. On ne code **aucune
règle du jeu** ici, seulement des contrats et des implémentations triviales.

**Mode** : une seule personne pilote l'agent, les 3 autres relisent et
proposent. On change de pilote toutes les 10 minutes. On commite directement
sur `main` (voir la décision C1 de la phase 0).

## Tâches

### 0.1 Projet Maven
- [ ] `pom.xml` : `groupId com.arena`, Java 21, JUnit 5, AssertJ (scope
      test), `maven-surefire-plugin`, `exec-maven-plugin` avec
      `mainClass = com.arena.Main`.
- [ ] `Main` affiche `Skirmish Arena` ; un test d'exemple Given/When/Then passe.
- [ ] Vérifier : `mvn test`, puis
      `mvn -q compile exec:java -Dexec.args="--matches 1"`.
- [ ] Commit : `Set up Maven project skeleton`

### 0.2 Événements (package `com.arena.engine.events`)
- [ ] `interface GameEvent {}`. **Pas `sealed`** : chaque lot ajoute ses
      records dans ses propres fichiers sans modifier une liste commune.
- [ ] `interface GameEventListener { void on(GameEvent event); }`
- [ ] `class EventPublisher` : `subscribe(listener)`, `publish(event)`.
      Test : un événement publié arrive à tous les listeners, dans l'ordre.
- [ ] Premiers événements (records) : `MatchStarted(String p1, String p2,
      long seed, String firstPlayer)`, `TurnStarted(int turn, String player)`,
      `CardPlayed(String player, String card, int cost, int manaLeft)`,
      `DamageDealt(String source, String target, int amount, int absorbed,
      int targetHpLeft)`, `MatchEnded(String winner, String reason, int turns)`.
- [ ] Test helper `RecordingListener` (dans `src/test/.../testing`).

### 0.3 Cartes et effets
- [ ] `engine.cards.CardCategory { ATTACK, DEFENSE, RESOURCE, UTILITY }`
- [ ] `engine.cards.Card` (record) : `name`, `cost`, `category`, `effect`,
      `boolean immediate` (true = s'applique pendant le play phase, pour
      Resource et pioche ; false = file d'attente du resolve phase).
- [ ] `engine.effects.Effect` : interface fonctionnelle
      `void apply(EffectContext ctx)` + `default Effect andThen(Effect next)`.
- [ ] `engine.effects.EffectContext` (interface) : `Champion caster()`,
      `Champion opponent()`, `DamageResolver damage()`, `EventPublisher events()`.
- [ ] `engine.combat.DamageResolver` (interface) :
      `void deal(String source, Champion target, int amount)`.
      Implémentation triviale `SimpleDamageResolver` (retire les PV + publie
      `DamageDealt`). Le lot A la remplacera par la vraie (armure, Parry) ;
      le lot C l'étendra pour Taunt.

### 0.4 Champion
- [ ] `engine.player.Champion` : `name`, `hp` (30), `maxMana`, `mana`,
      `hand` (List<Card>), `deck` (Deque<Card>).
      Méthodes simples : `loseHp(int)`, `heal(int)` (plafonné à 30),
      `isDead()`, `spendMana(int)`, `addToHand(Card)`.
      Le lot A en devient responsable ensuite (armure, fatigue…).
- [ ] Builder de test `aChampion()` dans `src/test/.../testing`.

### 0.5 Bots (contrat seulement)
- [ ] `engine.match.Action` : `sealed interface Action permits PlayCard,
      UseHeroPower, EndTurn` avec `record PlayCard(int handIndex)`. Ici,
      `sealed` est justifié : la liste d'actions est fermée par les règles.
- [ ] `engine.match.GameView` (interface, lecture seule) : `turn()`,
      `myHp()`, `myArmor()`, `myMana()`, `myHand()`, `heroPowerAvailable()`,
      `opponentHp()`, `opponentArmor()`, `opponentHandSize()`.
      Le lot C ajoutera `myBoard()` / `opponentBoard()`.
- [ ] `engine.match.Bot` : `String name()` + `Action nextAction(GameView view)`
      + `List<Integer> mulligan(List<Card> openingHand)` (indices des cartes à remettre).
      Le moteur appelle `nextAction` en boucle jusqu'à `EndTurn` et refuse
      une action illégale (dans ce cas, il termine le tour).
- [ ] Faux `ScriptedBot` (test) qui rejoue une liste d'actions.

### 0.6 Classes (contrat seulement)
- [ ] `engine.classes.HeroPower` (record) : `name`, `cost` (2), `effect`.
- [ ] `engine.classes.HeroClass` (record) : `name`, `heroPower`, `List<Card> deck`.
- [ ] `engine.classes.HeroClasses` : registre `byName(String)`, vide pour
      l'instant (une ligne par classe sera ajoutée par B et C).

### 0.7 Fin du sprint 0
- [ ] `mvn test` vert, push sur `main`.
- [ ] On passe à la tâche A.1 du backlog.

## Arborescence visée
```
src/main/java/com/arena/
  Main.java
  engine/events/     GameEvent, GameEventListener, EventPublisher, records…
  engine/cards/      Card, CardCategory, NeutralCards, MageCards…
  engine/effects/    Effect, EffectContext, DealDamage, GainArmor…
  engine/combat/     DamageResolver, ArmorDamageResolver…
  engine/player/     Champion, Armor…
  engine/board/      Board, Minion (lot C)
  engine/classes/    HeroClass, HeroPower, HeroClasses, Mage, Tank…
  engine/match/      Match, Bot, GameView, Action, TieBreaker…
  bots/              RandomBot, AggressiveBot, DefensiveBot
  cli/               CommandLineOptions, MatchRunner
  log/               ConsoleRenderer
  stats/             StatsCollector, AggregateStats
src/test/java/com/arena/testing/   builders et faux partagés
```
