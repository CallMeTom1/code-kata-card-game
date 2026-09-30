# Guideline d'équipe — Skirmish Arena

Ce document dit **comment on travaille à 4, en mob, sur un seul PC**. Les
règles du jeu sont dans `DESIGN.md`, les consignes pour l'agent dans
`CLAUDE.md`, les décisions de départ dans `docs/00-PHASE-0-DECISIONS.md`, et
le backlog dans `docs/plans/`.

## 1. Organisation de la journée (mob programming)

- On commence par la **phase 0** : remplir `docs/00-PHASE-0-DECISIONS.md` ensemble.
- Ensuite, **un seul PC, un seul agent**. Les rôles (driver, navigator,
  relecteur de diff, gardien du process) tournent toutes les 15 minutes,
  ou à la durée décidée en phase 0.
- Les 4 fichiers `docs/plans/lot-*.md` ne sont plus attribués à des
  personnes : ils forment **un seul backlog**, traité dans l'ordre de la
  section I de la phase 0.

| Jalon | Contenu |
|---|---|
| Sprint 0 | Squelette Maven + contrats partagés (`docs/plans/00-sprint-0-mob.md`) |
| M1 | Un match complet Random vs Random avec les cartes neutres, affiché en console |
| M2 | Statistiques, Mage et Tank, bots Aggressive/Defensive |
| M3 | Minions, autres classes, équilibrage avec le skill `run-simulation`, log d'exemple |

À chaque jalon : 5 minutes de pause, on lance `Main` ensemble, on relit le
backlog et on pousse.

## 2. Workflow git

- Selon la décision C1 de la phase 0 : par défaut, on travaille **sur `main`**,
  avec un commit par cycle TDD vert. On crée une branche seulement pour une
  expérience risquée.
- Messages de commit : impératif, en anglais, par exemple
  `Add fatigue damage when deck is empty`. On ajoute les 4 membres en
  `Co-authored-by:` : **l'historique git est évalué**.
- Avant chaque commit : `mvn test` vert et case du plan cochée.
- Push à chaque jalon et au moins toutes les 30 minutes.
- **Contrats du sprint 0** (`Card`, `Effect`, `Champion`, `Bot`, `GameView`,
  `Action`, `GameEvent`, `DamageResolver`) : on peut les faire évoluer, mais
  dans un commit à part, avec un message clair.

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
- Si l'agent veut modifier un contrat partagé ou sortir de la tâche en cours : **stop**, on en parle.
- Si `CLAUDE.md` n'est pas respecté, notez-le (quoi, quand) : c'est
  intéressant pour le débrief de la journée.

## 7. Definition of Done (pour chaque commit)

- [ ] Chaque comportement ajouté a un test Given/When/Then écrit **avant** le code
- [ ] `mvn test` vert, lancé pour de vrai par l'agent
- [ ] Toute action qui change l'état publie un `GameEvent`
- [ ] Aucun `System.out` hors de `Main` et `ConsoleRenderer`
- [ ] `DESIGN.md` à jour si une règle ou un chiffre a été décidé
- [ ] La case du backlog (`docs/plans/`) est cochée
- [ ] Diff relu par le relecteur du moment avant d'être accepté
