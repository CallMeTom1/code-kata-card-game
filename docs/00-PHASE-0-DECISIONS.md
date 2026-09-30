# Phase 0 — Décisions à prendre ensemble

**Format** : les 4 devant le même PC, **30 à 45 minutes maximum**, avant
d'écrire le moindre prompt de code. Pour chaque point : on lit la
recommandation, on vote, on écrit la décision dans la case `Décision :`.
Timebox : **3 minutes par point**. Si ça bloque, on prend la recommandation
et on avance ; on pourra revenir dessus plus tard.

Une fois le document rempli : on commite (`Record phase 0 decisions`), et on
reporte dans `DESIGN.md` / `CLAUDE.md` ce qui les concerne.

---

## A. Pré-requis techniques (5 min, à vérifier sur le PC)

- [ ] `java -version` → version : ______ (21 ou plus recommandé)
- [ ] `mvn -v` fonctionne
- [ ] `git push` fonctionne. En cas d'erreur `SSL certificate problem:
      self-signed certificate` : `git config --global http.sslBackend schannel`
- [ ] Claude Code installé, clé API de la journée configurée
- [ ] Le repo est cloné et `CLAUDE.md` est bien lu par l'agent (test : lui
      demander « quelles sont les règles de ce projet ? »)

---

## B. Organisation du mob (un seul PC)

### B1. Les rôles
| Rôle | Qui fait quoi |
|---|---|
| **Driver** | Tape les prompts dans l'agent. Il ne décide pas seul : il écrit ce que le groupe dit. |
| **Navigator** | Décide du prochain prompt et découpe la tâche en petits pas TDD. |
| **Relecteur de diff** | Lit chaque diff avant d'accepter : lisibilité, SOLID, nommage. |
| **Gardien du process** | Vérifie que le test est écrit **avant** le code, que `mvn test` a vraiment été lancé, que `CLAUDE.md` est respecté ; note chaque écart de l'agent (utile pour le débrief). |

Recommandation : les 4 rôles tournent **ensemble** d'un cran.
Décision : ______

### B2. Durée d'une rotation
Options : 10 min / 15 min / 25 min (pomodoro).
Recommandation : **15 min**. Assez long pour finir un cycle TDD, assez
court pour que tout le monde pilote souvent. Un minuteur visible.
Décision : ______

### B3. Désaccord
Recommandation : 2 minutes de discussion, puis vote à main levée ; en cas
d'égalité, le navigator tranche. On note la question dans « Questions
ouvertes » si elle mérite d'y revenir.
Décision : ______

### B4. Pauses et points d'étape
Recommandation : une pause de 10 min toutes les 1 h 30 ; un point de
5 min à chaque jalon (M1, M2, M3) pour relancer `Main` et relire le backlog.
Décision : ______

---

## C. Git

### C1. Branches
- **Option 1** : tout sur `main`, un commit par cycle TDD vert. C'est simple,
  et il n'y a aucun conflit possible puisqu'il n'y a qu'un seul PC.
- **Option 2** : une branche courte par tâche (`feature/<sujet>`), mergée
  dans `main` en local dès que les tests sont verts. L'historique est plus
  lisible, mais ça prend un peu plus de temps.

Recommandation : **option 1**. Vous pouvez aussi faire une branche uniquement
pour une expérience risquée (une idée d'équilibrage, un refactor).
Décision : ______

### C2. Messages de commit et auteurs
L'historique git est évalué. Recommandation :
- impératif, en anglais : `Add fatigue damage when deck is empty` ;
- un commit par cycle **red → green → refactor** ;
- ajouter les 4 membres en co-auteurs, pour que le travail en mob soit visible :
  ```
  Co-authored-by: Prénom Nom <email>
  ```
  (on donne la liste à l'agent une fois, il l'ajoute à chaque commit)
Décision : ______
Liste des co-auteurs : ______

### C3. Fréquence de push
Recommandation : push à chaque jalon **et** au moins toutes les 30 minutes.
Décision : ______

---

## D. Conventions de code et de test

### D1. Nommage des tests Given/When/Then
- **Option 1** : `given_empty_deck_when_draw_then_fatigue_1()` (déjà dans `CLAUDE.md`)
- **Option 2** : `@DisplayName("Given an empty deck, when drawing, then fatigue deals 1")`
  avec un nom de méthode en camelCase

Recommandation : **option 1**. Lisible directement dans le rapport de tests,
et sans double saisie.
Décision : ______

### D2. Bibliothèques de test
Recommandation : JUnit 5 + AssertJ, **pas de Mockito** (des faux écrits à la main).
Décision : ______

### D3. Langue du code, des tests et des logs
Recommandation : code, tests, commits et logs en **anglais** ; documents
d'équipe en français.
Décision : ______

### D4. Style Java
Recommandation : `record` pour les données immuables (cartes, événements),
`final` par défaut, pas de `null` retourné (utiliser `Optional` ou une
liste vide), pas de getters/setters générés en masse.
Décision : ______

---

## E. Contrats d'architecture (voir `docs/plans/00-sprint-0-mob.md`)

### E1. Comment un bot joue
- **Option 1** : `Action nextAction(GameView)`, appelé en boucle jusqu'à
  `EndTurn`. Le moteur valide chaque action.
- **Option 2** : `List<Card> choosePlays(GameView)`, le bot décide de tout le tour d'un coup.

Recommandation : **option 1**. Le bot voit l'effet de chaque carte (pioche,
mana) avant de choisir la suivante, et le moteur reste le seul à appliquer
les règles.
Décision : ______

### E2. Les événements
Maintenant qu'on est sur un seul PC, il n'y a plus de risque de conflit :
`GameEvent` peut devenir une `sealed interface`, ce qui permet un `switch`
exhaustif dans le `ConsoleRenderer` (le compilateur signale un événement
oublié).
- **Option 1** : `sealed interface` + `switch` exhaustif
- **Option 2** : interface ouverte, les listeners ignorent ce qu'ils ne connaissent pas

Recommandation : **option 1** en mob.
Décision : ______

### E3. Représentation des cartes
- **Option 1** : une carte = un `record` avec une composition d'`Effect` réutilisables
- **Option 2** : une classe Java par carte

Recommandation : **option 1** (Open/Closed : une nouvelle carte ne demande
aucun nouveau code si les effets existent).
Décision : ______

### E4. Packages
Recommandation : l'arborescence de `docs/plans/00-sprint-0-mob.md`
(package par fonctionnalité).
Décision : ______

---

## F. Règles du jeu à confirmer (`DESIGN.md`)

| # | Règle | Proposition actuelle | Décision |
|---|---|---|---|
| F1 | Un « tour » | 1 manche = les deux joueurs jouent ; limite de 50 manches | ______ |
| F2 | Avantage du 1er joueur | Le 2e reçoit 4 cartes + The Coin | ______ |
| F3 | Deck vide | Fatigue 1, 2, 3… | ______ |
| F4 | Départage après 50 tours | PV, puis dégâts infligés, puis match nul | ______ |
| F5 | Ordre du resolve phase | Ordre de jeu (comme Hearthstone), puis les minions | ✅ décidé |
| F6 | Minions | Dans le périmètre, mais après le MVP ; plateau de 7 | ______ |
| F7 | Classes pour le MVP | Mage + Tank d'abord | ______ |
| F8 | Seuil du bot Defensive | 15 PV | ______ |

---

## G. Logging

### G1. Format console
Chaque ligne commence par des **étiquettes entre crochets** : le tour, le
joueur, puis le type d'événement, aligné sur une largeur fixe. On peut
filtrer avec `grep` (par exemple `grep "\[DAMAGE"` ou `grep "\[Bob"`), et
l'œil trouve vite l'information. Une seule ligne par événement : on n'écrit
que ce qui s'est passé, jamais ce qui ne s'est pas passé.

Exemple proposé :
```
[MATCH  ] Alice [Aggressive:Mage] vs Bob [Defensive:Tank] | seed 42
[SETUP  ][Alice] [Aggressive] plays [Mage] (imposed) | deck: preset
[POWER  ][Alice] Fireblast (2) — deal 1 damage
[DECK   ][Alice] [ATTACK   10] Quick Jab x2, Frostbolt x2, Fireball x2, Raise Skeletons x2, Pyroblast x2
[DECK   ][Alice] [DEFENSE   4] Wooden Shield x2, Ice Barrier x2
[DECK   ][Alice] [RESOURCE  2] Mana Crystal x2
[DECK   ][Alice] [UTILITY   4] Insight x2, Arcane Intellect x2
[CURVE  ][Alice] [1: 8] [2: 2] [3: 6] [4: 2] [5+: 2] | avg cost 2.7
[SETUP  ][Bob  ] ...
[START  ] Alice goes first (coin flip) | Bob gets The Coin
[SWAP   ][Alice] puts back Pyroblast → draws Strike
...
[T03] ======================================================================
[T03][Alice][TURN   ] [HP 28/30] [ARMOR 0] [MANA 3/3] [HAND 4] [DECK 13]
[T03][Alice][DRAW   ] Fireball
[T03][Alice][PLAY   ] Frostbolt (2) → [MANA 1/3]
[T03][Alice][DAMAGE ] Frostbolt → Bob : 3 dmg [ABSORBED 0] [HP 27 → 24]
[T03][Alice][STATUS ] Bob [FROZEN] (-1 mana next turn)
[T03][Alice][MINION ] Skeleton [1/1] → Bob : 1 dmg [ABSORBED 1] [HP 24 → 24]
[T03][Bob  ][TURN   ] [HP 24/30] [ARMOR 0] [MANA 2/3] [HAND 5] [DECK 12]
[T03][Bob  ][PLAY   ] Healing Potion (2) → [MANA 0/3]
[T03][Bob  ][HEAL   ] Healing Potion → Bob : +5 [HP 24 → 29]
[T04] ======================================================================
[T04][Alice][PLAY   ] Raise Skeletons (3) → [MANA 1/4]
[T04][Alice][SUMMON ] Skeleton [1/1] → [BOARD 2/7]
[T04][Alice][SUMMON ] Skeleton [1/1] → [BOARD 3/7]
[T04][Bob  ][POWER  ] Armor Up (2) → [ARMOR 2 (3 turns)] [MANA 2/4]
...
[T09][Alice][DEATH  ] Skeleton [1/1] dies attacking Iron Golem [2/6] [TAUNT]
...
[T17][Bob  ][FATIGUE] 3 dmg [HP 4 → 1]
...
[RESULT ] Alice WINS | reason: HP 0 | turns: 18 | damage Alice 34 / Bob 21
```
Étiquettes (implémentées) : `MATCH`, `SETUP`, `POWER`, `DECK`, `CURVE`, `START`, `SWAP` (mulligan), `HAND`, `TURN`, `MANA`, `DRAW`, `BURN`, `FATIGUE`, `PLAY`, `ILLEGAL`, `POISON`, `EVADE`,
`DAMAGE`, `HEAL`, `ARMOR`, `STATUS`, `SUMMON`, `MINION`, `DEATH`, `RESULT`.
Les noms de joueur sont complétés par des espaces pour que les colonnes restent alignées.
Décision : ______

### G2. Quels matchs logger
Recommandation : `--log` affiche le **premier** match en détail ; les
autres ne servent qu'aux statistiques.
Décision : ______

### G3. Ouverture vers un front
Recommandation : on ne fait que l'architecture (événements + listeners) ;
l'export JSON est un bonus en fin de journée.
Décision : ______

---

## H. Travail avec l'agent

### H1. Mode de permission
Recommandation : mode **par défaut** (on valide chaque modification),
**pas** d'acceptation automatique : la règle « montre-moi le diff » n'a de
sens que si on relit.
Décision : ______

### H2. Prompt type
Recommandation :
> Lis `docs/plans/<fichier>.md`. Fais la tâche <X.n> en TDD : montre d'abord
> le test Given/When/Then qui échoue et le résultat de `mvn test`, puis le
> code minimal, puis `mvn test` vert.

Décision : ______

### H3. Quand l'agent triche
(code avant le test, « c'est fait » sans `mvn test`, fichier hors sujet
modifié). Recommandation : on rejette le diff, on re-prompte en citant la
règle de `CLAUDE.md`, et le gardien du process note l'écart.
Décision : ______

---

## I. Périmètre et ordre de travail

En mob, les 4 plans deviennent **un seul backlog, traité dans l'ordre**.
Recommandation :

| Ordre | Tâches | Jalon |
|---|---|---|
| 1 | Sprint 0 : squelette et contrats (`00-sprint-0-mob.md`) | — |
| 2 | Moteur : mise en place, phases, boucle (A.1 → A.3) | |
| 3 | Effets de base + cartes neutres (B.1, B.2) | |
| 4 | `ConsoleRenderer`, `RandomBot`, CLI (D.1 → D.3) | **M1 : un match visible en console** |
| 5 | Statistiques + `MatchRunner` (D.4) | **Livrables 1 à 3 de l'énoncé** |
| 6 | Armure, Parry, statuts, départage (A.4 → A.6) | |
| 7 | Mage, Tank, pouvoirs (B.3 → B.5) + bots Aggressive/Defensive (D.5) + préparation du match, decks construits par les bots (D.6) | **M2** |
| 8 | Minions, Taunt, zone (C.1 → C.4) | |
| 9 | Épéiste, Assassin, Clerc (C.5 → C.7), équilibrage, JSON | **M3 (bonus)** |
| 10 | Bots LLM (`DESIGN.md` → « Later: LLM bots ») | après les livrables |

Décision : ______

---

## Questions ouvertes
_(à remplir pendant la journée)_

-
