# Guideline d'équipe — Skirmish Arena

Ce document dit **comment on travaille à 4** sur ce projet. Les règles du jeu
sont dans `DESIGN.md`, les consignes pour l'agent dans `CLAUDE.md`, et le
détail de chaque lot dans `docs/plans/`.

## 1. Organisation de la journée

| Étape | Qui | Durée | Contenu |
|---|---|---|---|
| Sprint 0 | Les 4, en mob | ~45 min | Squelette Maven + contrats partagés (`docs/plans/00-sprint-0-mob.md`) |
| Jalon M1 | 4 lots en parallèle | ~2 h | Un match complet Random vs Random avec les cartes neutres, affiché en console |
| Jalon M2 | 4 lots en parallèle | ~2 h | Mage et Tank, les minions, les bots Aggressive/Defensive, les statistiques |
| Jalon M3 | selon l'avancement | reste | Autres classes, équilibrage avec le skill `run-simulation`, log d'exemple |

À chaque jalon : **point de synchro de 10 minutes**, tout le monde merge
`main` dans sa branche, et on lance ensemble `Main` pour voir le résultat.

### Les 4 lots

| Lot | Responsable | Contenu | Plan |
|---|---|---|---|
| A | Personne 1 | Moteur : boucle de tour, phases, mana, pioche/fatigue, armure/Parry, fin de match, départage | `lot-A-engine.md` |
| B | Personne 2 | Effets réutilisables, cartes neutres, Mage, Tank, pouvoirs héroïques | `lot-B-cards-effects.md` |
| C | Personne 3 | Plateau et minions, Taunt, effets de zone, Épéiste, Assassin, Clerc | `lot-C-minions-classes.md` |
| D | Personne 4 | Bots, `Main` (CLI), `MatchRunner`, log console, statistiques | `lot-D-bots-cli-logging.md` |

### Binômes (l'énoncé impose pair ou mob programming)
- **Paire 1 = A + B** : le moteur et les cartes sont très liés.
- **Paire 2 = C + D** : les minions et les bots/le log se croisent beaucoup.

Dans chaque paire, les deux font leur lot sur leur propre branche, **mais** :
- on travaille côte à côte : l'un pilote l'agent pendant que l'autre relit le
  diff, et on échange les rôles toutes les 25 minutes ;
- le binôme est le **relecteur obligatoire** des PR de l'autre.

## 2. Workflow git

- Branche de base : `main`. On ne commite jamais directement dessus (sauf le sprint 0).
- Une branche par sujet : `feature/<lot>-<sujet>`, par exemple
  `feature/A-turn-loop`, `feature/B-neutral-cards`, `feature/D-console-renderer`.
- Des branches **courtes** : une PR toutes les 1 à 2 heures maximum. Une grosse
  PR en fin de journée = des conflits garantis.
- Avant d'ouvrir une PR :
  1. `git fetch origin && git merge origin/main`
  2. `mvn test` est vert en local
  3. le plan du lot est mis à jour (cases cochées)
- PR : titre `[Lot X] <sujet>`, description = ce qui a été fait + la ligne de
  résumé de `mvn test`. **Une relecture par le binôme** avant le merge.
- Messages de commit : impératif, en anglais, par exemple
  `Add fatigue damage when deck is empty`. Un commit par cycle TDD vert,
  c'est l'idéal : **l'historique git est évalué**.
- Après chaque merge sur `main`, prévenez l'équipe pour que chacun merge `main`.

### Éviter les conflits
Les fichiers « chauds » sont connus. On les découpe pour que chacun ajoute
des fichiers au lieu de modifier les mêmes :
- **Événements** : un `record` par fichier dans `engine.events`. On n'édite
  jamais l'événement d'un autre lot : on en crée un nouveau.
- **Cartes** : un fichier par groupe (`NeutralCards`, `MageCards`, `TankCards`…).
- **Classes** : un fichier par classe ; le registre `HeroClasses` n'a qu'une
  ligne à ajouter par classe.
- **DESIGN.md** : chaque lot ne modifie que ses propres sections.
- **Contrats du sprint 0** (`Card`, `Effect`, `Champion`, `Bot`, `GameView`,
  `Action`, `GameEvent`, `DamageResolver`) : toute modification se décide
  à 4, en 2 minutes, puis passe par une petite PR dédiée, mergée tout de suite.

## 3. TDD en Given / When / Then

Le cycle est toujours le même, et l'agent doit le montrer :
1. **Red** : un seul test, qui échoue pour la bonne raison (on voit l'échec).
2. **Green** : le code minimal pour le faire passer.
3. **Refactor** : on nettoie, les tests restent verts.
4. **Commit**.

Format imposé :

```java
@Test
void given_opponent_with_3_armor_when_strike_is_played_then_armor_absorbs_first() {
    // Given
    Champion opponent = aChampion().withHp(30).withArmor(3, 2).build();
    // When
    strike.effect().apply(contextAgainst(opponent));
    // Then
    assertThat(opponent.hp()).isEqualTo(29);
    assertThat(opponent.armor()).isZero();
}
```

- Un comportement par test. Les noms de tests servent de spécification :
  quelqu'un doit comprendre la règle sans lire le code.
- Pas de Mockito : de petits faux écrits à la main dans `src/test` (un deck
  fixe, un `Random` avec une graine, un `ScriptedBot` qui joue une liste
  d'actions, un `RecordingListener` qui capture les événements).
- Les builders de test partagés (`aChampion()`…) vivent dans
  `src/test/java/com/arena/testing/` et sont créés au sprint 0.
- **Piège à surveiller** : l'agent qui annonce « c'est fait » sans avoir
  lancé `mvn test`, ou qui écrit le code avant le test. Rejetez le diff et
  re-promptez. C'est le cœur de l'exercice.

## 4. SOLID dans ce projet

| Principe | Application concrète |
|---|---|
| **S**ingle responsibility | `Match` fait tourner les tours, `DamageResolver` calcule les dégâts, `ConsoleRenderer` affiche, `StatsCollector` compte. Aucune classe ne fait deux de ces choses. |
| **O**pen/closed | Une nouvelle carte = une composition d'`Effect` existants. Une nouvelle classe, un nouveau bot ou un nouvel événement = une nouvelle classe, sans toucher au moteur. |
| **L**iskov | Tout `Bot` doit pouvoir affronter n'importe quel autre `Bot` ; tout `GameEventListener` doit accepter n'importe quel événement (et ignorer ceux qu'il ne connaît pas). |
| **I**nterface segregation | Les bots reçoivent un `GameView` en lecture seule, pas le `GameState`. Les effets reçoivent un `EffectContext`, pas le `Match`. |
| **D**ependency inversion | Le moteur dépend d'interfaces (`Bot`, `GameEventListener`, `DamageResolver`, `Random`). Tout est assemblé dans `Main`. |

## 5. Logging et ouverture vers un front

La simulation doit être **entièrement visible dans le terminal**, sans
enfermer le projet dans la console.

```
 Match ──publie──▶ GameEvent ──▶ GameEventListener
                                   ├── ConsoleRenderer    (aujourd'hui : texte lisible)
                                   ├── StatsCollector     (aujourd'hui : statistiques)
                                   └── JsonEventExporter  (plus tard : fichier rejouable en HTML/JS)
```

- Le moteur n'appelle **jamais** `System.out`. Il publie des événements :
  `TurnStarted`, `CardPlayed`, `DamageDealt`, `MinionSummoned`…
- Un événement est un `record` immuable qui contient toutes les données
  utiles à l'affichage (noms, valeurs, PV restants). Un front n'aura besoin
  de rien d'autre.
- Toute action du moteur qui change l'état **doit** publier un événement,
  sinon elle est invisible. C'est vérifié par des tests (un
  `RecordingListener` capture les événements).
- `--log` affiche le détail du premier match ; les autres matchs ne
  nourrissent que les statistiques (1000 matchs détaillés seraient illisibles).
- Pour le front plus tard : il suffira d'ajouter `JsonEventExporter` (un
  listener) et une page HTML/JS qui rejoue le fichier. Le moteur ne change pas.

## 6. Travailler avec l'agent (vibecoding)

- **Aucune ligne tapée à la main.** On lit le diff, on accepte, on revert ou on re-prompte.
- Prompt de départ conseillé :
  > Lis `docs/plans/lot-X-....md`. Fais la tâche X.n en TDD : montre-moi
  > d'abord le test qui échoue, puis le code, puis le résultat de `mvn test`.
- Une tâche du plan = un prompt = un ou quelques commits.
- Si l'agent veut modifier un contrat partagé ou le lot d'un autre : **stop**, on en parle.
- Si `CLAUDE.md` n'est pas respecté, notez-le (quoi, quand) : c'est
  intéressant pour le débrief de la journée.

## 7. Definition of Done (pour chaque PR)

- [ ] Chaque comportement ajouté a un test Given/When/Then écrit **avant** le code
- [ ] `mvn test` vert, ligne de résumé collée dans la PR
- [ ] Toute action qui change l'état publie un `GameEvent`
- [ ] Aucun `System.out` hors de `Main` et `ConsoleRenderer`
- [ ] `DESIGN.md` à jour si une règle ou un chiffre a été décidé
- [ ] Les cases du plan du lot sont cochées
- [ ] Relu et approuvé par le binôme
