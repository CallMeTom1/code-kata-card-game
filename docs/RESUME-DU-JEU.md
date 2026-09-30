# Skirmish Arena — Comment se joue une partie

Résumé pour l'équipe. La source de vérité reste [`DESIGN.md`](../DESIGN.md) : en cas d'écart, c'est lui qui a raison.

## En bref

Deux bots s'affrontent dans un duel de cartes inspiré de Hearthstone : chaque champion a 30 PV, et le premier à tomber à 0 PV perd. Chaque bot choisit une classe (5 au choix), qui lui donne un pouvoir héroïque et des cartes propres, mélangées à des cartes neutres dans un deck de 20 cartes.

Personne ne joue à la main : le moteur Java simule N parties, affiche le log tour par tour et des statistiques. Tout le hasard vient d'une seed, donc une même commande rejoue exactement la même partie.

## Mise en place

Avant le tour 1, chaque joueur a 30 PV, un deck mélangé de 20 cartes et une main de 3 ou 4 cartes.

1. **Deck** : 20 cartes exactement, 2 exemplaires maximum par carte, au moins 6 cartes de classe. Le deck est construit par le bot, ou pris dans les decks prédéfinis avec `--preset-decks`.
2. **Pile ou face** (seedé) : il désigne qui commence.
3. **Main de départ** : 3 cartes pour le premier joueur, 4 pour le second.
4. **Mulligan** : une seule fois, chaque bot peut rendre des cartes de sa main. Elles sont remélangées dans le deck, puis il repioche le même nombre (il peut donc repiocher une carte qu'il vient de rendre).
5. **The Coin** : le second joueur la reçoit après le mulligan. Elle coûte 0 et donne +1 mana pour ce tour seulement, pour compenser le fait de jouer en second.

## Déroulement d'un tour

Un « tour » = une manche complète : le premier joueur fait ses 5 phases, puis le second fait les siennes.

1. **Pioche** : le joueur pioche 1 carte. Main limitée à 10 : une carte de trop est brûlée. Deck vide = dégâts de fatigue (1, puis 2, puis 3…).
2. **Mana** : le mana max augmente de 1 (1 au tour 1, plafond 10), puis la réserve se remplit. Le mana non dépensé est perdu.
3. **Jeu** : le bot joue des cartes et, s'il le veut, son pouvoir héroïque (2 mana, une fois par tour). Les cartes Ressource et de pioche s'appliquent tout de suite, car elles changent ce qu'on peut encore jouer.
4. **Résolution** : tous les autres effets s'appliquent **dans l'ordre où ils ont été joués**, puis les serviteurs attaquent. Les dégâts touchent d'abord l'armure, puis les PV.
5. **Fin** : effets de fin de tour (par exemple le soin du Spirit Healer), puis c'est à l'adversaire.

En début de tour, le poison inflige ses dégâts et les armures arrivées à expiration disparaissent.

## Fin de partie

La partie s'arrête **immédiatement** quand un champion tombe à 0 PV : le reste de la file d'effets est annulé. Si les deux tombent à 0 en même temps, c'est un match nul.

Après 50 tours sans vainqueur, on départage :

1. Le plus de PV restants gagne.
2. À PV égaux, le plus de dégâts infligés au total gagne.
3. Sinon, match nul (compté à part dans les stats).

## Classes, héros et pouvoirs héroïques

Il y a 5 classes. Chacune a un style, un pouvoir héroïque (2 mana, une fois par tour) et 5 ou 6 cartes propres. N'importe quel bot peut jouer n'importe quelle classe.

| Classe (ligne de commande) | Style | Pouvoir héroïque | Effet du pouvoir |
| --- | --- | --- | --- |
| Mage (`Mage`) | sorts de dégâts à gros impact, squelettes | Fireblast | 1 dégât |
| Tank (`Tank`) | armure et endurance | Armor Up | Armure 2 (3 tours) |
| Épéiste (`Swordsman`) | coups multiples, bonus d'attaque, nettoyage de serviteurs | Sharpen | prochaine carte Attaque +2 dégâts |
| Assassin (`Assassin`) | cartes pas chères, combos, poison | Poisoned Dagger | Poison 1 (2 tours) |
| Clerc (`Cleric`) | soins et usure | Lesser Heal | soigne 2 PV |

Les contres sont voulus : Twin Blades use vite l'armure mais souffre contre Parry ; Eviscerate et le poison traversent l'armure du Tank ; Whirlwind Slash et Holy Nova balaient les petits serviteurs.

## Les cartes

Le jeu compte 39 cartes : 10 neutres (jouables par toutes les classes) et 29 cartes de classe. Chaque carte a un coût en mana et une catégorie : **Attaque** (A), **Défense** (D), **Ressource** (R) ou **Utilitaire** (U). Les cartes ne ciblent jamais un serviteur : elles touchent le champion adverse, sauf les effets de zone.

### Mots-clés

| Mot-clé | Effet |
| --- | --- |
| Armure X (N tours) | absorbe X dégâts, disparaît au début du N-ième tour suivant ; les armures s'additionnent |
| Parry X (N tours) | réduit chaque coup de X, après l'armure |
| Poison X (N tours) | l'adversaire perd X PV au début de chacun de ses N prochains tours, armure ignorée |
| Freeze | l'adversaire a 1 mana de moins au prochain tour |
| Combo | bonus si une autre carte a déjà été jouée ce tour (The Coin compte, pas le pouvoir) |
| Evasion | annule les prochains dégâts ennemis sur votre champion (pas le poison ni la fatigue) |
| Next Attack +X | +X à chaque coup de la prochaine carte Attaque qui inflige des dégâts |
| Taunt (Provocation) | les serviteurs ennemis doivent attaquer ce serviteur d'abord |

### Cartes neutres (10)

| Carte | Cat. | Coût | Effet |
| --- | --- | --- | --- |
| Quick Jab | A | 1 | 2 dégâts |
| Strike | A | 2 | 4 dégâts |
| Wild Wolf | A | 2 | invoque un Loup 2/2 |
| Crushing Blow | A | 5 | 8 dégâts |
| Wooden Shield | D | 1 | Armure 2 (2 tours) |
| Shieldbearer | D | 1 | invoque un 0/3 avec Taunt |
| Iron Wall | D | 3 | Armure 5 (2 tours) |
| Mana Crystal | R | 1 | +1 mana max permanent |
| Insight | U | 1 | pioche 1 carte |
| Healing Potion | U | 2 | soigne 4 PV (max 30) |

### Cartes de classe (29)

| Classe | Carte | Cat. | Coût | Effet |
| --- | --- | --- | --- | --- |
| Mage | Frostbolt | A | 2 | 3 dégâts, Freeze |
| Mage | Raise Skeletons | A | 3 | invoque deux Squelettes 1/1 |
| Mage | Fireball | A | 4 | 6 dégâts |
| Mage | Pyroblast | A | 8 | 10 dégâts |
| Mage | Ice Barrier | D | 3 | Armure 6 (2 tours) |
| Mage | Arcane Intellect | U | 3 | pioche 2 cartes |
| Tank | Shield Slam | A | 1 | dégâts = votre armure actuelle |
| Tank | Shield Block | D | 3 | Armure 4 (2 tours), pioche 1 carte |
| Tank | Iron Golem | D | 4 | invoque un Golem 0/6 avec Taunt |
| Tank | Fortress | D | 5 | Armure 8 (2 tours) |
| Tank | War Chest | R | 2 | +1 mana max, Armure 2 (2 tours) |
| Tank | Last Stand | U | 4 | soigne 6 PV |
| Épéiste | Twin Blades | A | 3 | 2 coups de 3 dégâts |
| Épéiste | Whirlwind Slash | A | 5 | 3 dégâts au champion et à chaque serviteur ennemi |
| Épéiste | Riposte | D | 2 | Parry 2 (1 tour), 2 dégâts |
| Épéiste | Focus Training | R | 1 | +1 mana max, prochaine Attaque +1 |
| Épéiste | Battle Cry | U | 1 | prochaine carte Attaque +3 dégâts |
| Assassin | Backstab | A | 0 | 2 dégâts |
| Assassin | Deadly Poison | A | 2 | Poison 2 (3 tours) |
| Assassin | Venom Spider | A | 2 | invoque une Araignée 1/2 ; ses coups sur le champion ajoutent Poison 1 (2 tours) |
| Assassin | Eviscerate | A | 3 | 3 dégâts qui ignorent l'armure ; Combo : 5 |
| Assassin | Evasion | D | 2 | Evasion (jusqu'à 2 tours) |
| Assassin | Preparation | R | 0 | +2 mana ce tour seulement |
| Clerc | Smite | A | 1 | 2 dégâts |
| Clerc | Holy Nova | A | 5 | 2 dégâts au champion et à chaque serviteur ennemi, soigne 2 PV |
| Clerc | Power Word: Shield | D | 1 | Armure 3 (2 tours), pioche 1 carte |
| Clerc | Divine Blessing | R | 2 | +1 mana max, soigne 2 PV |
| Clerc | Greater Heal | U | 3 | soigne 6 PV |
| Clerc | Spirit Healer | U | 3 | invoque un Esprit 0/3 avec Taunt ; vous soigne 2 PV à la fin de chacun de vos tours |

## Serviteurs et plateau

Certaines cartes invoquent des serviteurs (Attaque/Vie, par exemple 2/3) sur le plateau de leur propriétaire, 7 au maximum. Une invocation sur un plateau plein ne fait rien.

- **Mal d'invocation** : un serviteur attaque pour la première fois au tour suivant de son propriétaire, puis une fois par tour, du plus ancien au plus récent.
- **Cible automatique** : il attaque un serviteur ennemi avec Taunt s'il y en a un, sinon le champion adverse. Il reste en retrait plutôt que de mourir sur un Taunt sans le tuer.
- **Combat** : deux serviteurs qui se battent s'infligent mutuellement leur Attaque. À 0 Vie, un serviteur meurt.
- Un coup de serviteur sur un champion compte comme un coup : l'armure l'absorbe, Parry le réduit, Evasion peut l'annuler.
- Seuls les autres serviteurs et les effets de zone (Whirlwind Slash, Holy Nova) peuvent tuer un serviteur.

## Les bots

Un bot = une stratégie de deck (classe + 20 cartes) et une stratégie de jeu. Avec `auto`, la classe est fixée par bot : Aggressive → Assassin, Defensive → Tank, Random → au hasard.

| Bot | Deck | Mulligan | Jeu |
| --- | --- | --- | --- |
| Aggressive | Attaques pas chères et serviteurs d'attaque d'abord, puis Ressources, aucun soin | rend les cartes de coût 4 ou plus | Ressources d'abord, puis l'Attaque la plus forte payable, en boucle ; pouvoir avec le mana restant |
| Defensive | Défense, soins et Taunt d'abord, au moins 6 Attaques | rend les cartes de coût 5 ou plus | comme Aggressive au-dessus de 15 PV ; à 15 PV ou moins, soins, Taunt et Défense d'abord |
| Random | deck légal tiré au hasard | rend un sous-ensemble au hasard | cartes payables au hasard (sert de référence) |
| Llm | Claude choisit sa classe et ses 20 cartes (deck vérifié, repli sur Aggressive) | choisi par Claude | un appel par tour qui renvoie un plan ; repli sur Aggressive en cas de coup illégal ; il parle à l'adversaire |

Le bot Llm a besoin d'une clé dans `.env.local` (`ANTHROPIC_API_KEY=...`). Ses pensées et ses répliques apparaissent dans le log (`[THINK]`, `[SAY]`) et dans le replay web (bulles et panneau Dialogue).

Sur 1000 parties (seed 42), Aggressive:Mage contre Defensive:Tank donne 46,7 % de victoires à Aggressive et dure 9,6 tours en moyenne. Les deux vrais bots battent Random environ 9 fois sur 10.

## Lancer une partie et la rejouer

Une seule commande simule les parties, puis exporte la première partie et les statistiques pour le front web :

```bash
mvn -q compile exec:java -Dexec.args="--matches 1000 --p1 Aggressive:Mage --p2 Defensive:Tank --seed 42 --names Alice,Bob --json partie.jsonl --stats-json stats.json"
```

- **En direct depuis l'interface** : `mvn -q compile exec:java -Dexec.args="--serve"`, ouvrir http://localhost:8080, puis « ⚔ Nouvelle partie ».
- `--log` affiche la première partie tour par tour dans la console.
- Ouvrir `web/index.html` (double-clic), puis glisser-déposer `partie.jsonl` (onglet Replay) et/ou `stats.json` (onglet Statistiques).
- Avec la même `--seed`, on retrouve exactement la même partie.

Écarts volontaires avec Hearthstone : decks de 20 cartes, armure qui expire, phase de résolution, aucun ciblage, et Freeze qui retire 1 mana.
