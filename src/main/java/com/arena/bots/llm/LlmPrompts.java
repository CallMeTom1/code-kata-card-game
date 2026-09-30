package com.arena.bots.llm;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.classes.StandardClasses;
import com.arena.engine.match.GameView;
import com.arena.engine.match.Match;
import com.arena.engine.match.MinionView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Everything the model reads: the rules and card catalog (a fixed system prompt, so it can be cached)
 * and one short text per decision. Kept apart from the bots so prompts can be tuned in one place.
 */
final class LlmPrompts {

    static final HeroClasses CLASSES = StandardClasses.all();

    private static final String RULES = """
            You are a champion in Skirmish Arena, a Hearthstone-like card duel against another AI.
            Rules:
            - Each champion has 30 HP and a 20-card deck (max 2 copies of a card, at least 6 class cards,
              only your class cards plus neutral cards). First to 0 HP loses. After 50 rounds the higher HP wins.
            - Each turn: draw 1 card, max mana +1 (cap 10) and mana refills, then you play cards and may use your
              hero power once (it costs 2). Resource and draw cards apply at once; everything else resolves at the
              end of your play phase, in the order you played it. Then your minions attack automatically.
            - Cards never target minions: they hit the enemy champion (area cards also hit every enemy minion).
              Minions attack an enemy Taunt minion first, otherwise the enemy champion.
            - Armor absorbs damage first and expires after its number of turns. Parry reduces each hit. Poison
              ignores armor. Freeze removes 1 mana from the opponent's next turn. Combo: bonus if another card
              was already played this turn (The Coin counts).
            - Max 10 cards in hand, max 7 minions on the board. Drawing from an empty deck deals fatigue damage.
            You also talk with your opponent: "thought" is your private reasoning (1-2 short sentences),
            "message" is what you say aloud to the other AI (one short, playful line: taunt, bluff, answer what
            it said). Write "thought" and "message" in French. Never reveal your hand in "message".
            """;

    private LlmPrompts() {
    }

    /** Rules, every class with its hero power and cards, and the neutral cards. */
    static String system() {
        StringBuilder text = new StringBuilder(RULES).append("\nClasses:\n");
        for (HeroClass heroClass : CLASSES.all()) {
            text.append("- ").append(heroClass.name()).append(": hero power ").append(heroClass.heroPower().name())
                    .append(" (").append(heroClass.heroPower().cost()).append(" mana, ")
                    .append(heroClass.heroPower().description()).append("). Class cards: ")
                    .append(cards(heroClass.classCards())).append('\n');
        }
        return text.append("Neutral cards: ").append(cards(NeutralCards.all())).append('\n').toString();
    }

    /** The state of the board at the start of a turn, as a player would see it. */
    static String turn(GameView view) {
        String power = CLASSES.byName(view.myClass())
                .map(c -> c.heroPower().name() + " (" + c.heroPower().cost() + " mana, " + c.heroPower().description()
                        + ")").orElse("cost " + view.heroPowerCost());
        StringBuilder text = new StringBuilder();
        text.append("Round ").append(view.turn()).append('/').append(Match.MAX_ROUNDS).append(".\n")
                .append("You (").append(view.myClass()).append("): HP ").append(view.myHp()).append(", armor ")
                .append(view.myArmor()).append(", mana ").append(view.myMana()).append('/').append(view.myMaxMana())
                .append(", deck ").append(view.myDeckSize()).append(" cards.\n")
                .append("Hero power ").append(power).append(view.heroPowerAvailable() ? ": available" : ": not available")
                .append(".\nYour hand:\n");
        List<Card> hand = view.myHand();
        for (int i = 0; i < hand.size(); i++) {
            text.append("  ").append(i).append(". ").append(card(hand.get(i))).append('\n');
        }
        text.append("Your minions: ").append(minions(view.myMinions())).append('\n')
                .append("Opponent (").append(view.opponentClass()).append("): HP ").append(view.opponentHp())
                .append(", armor ").append(view.opponentArmor()).append(", ").append(view.opponentHandSize())
                .append(" cards in hand.\nOpponent minions: ").append(minions(view.opponentMinions())).append('\n');
        if (!view.opponentLastWords().isEmpty()) {
            text.append("Your opponent just said: \"").append(view.opponentLastWords()).append("\"\n");
        }
        return text.append("""
                Plan your whole turn: list the cards to play in order ("PLAY" with the exact card name) and
                "HERO_POWER" if you use it. Total cost must fit your mana (The Coin and Resource cards add mana).
                An empty list ends the turn.""").toString();
    }

    /** The opening hand, to choose which cards go back. */
    static String mulligan(List<Card> hand) {
        StringBuilder text = new StringBuilder("Mulligan: here is your opening hand.\n");
        for (int i = 0; i < hand.size(); i++) {
            text.append("  ").append(i).append(". ").append(card(hand.get(i))).append('\n');
        }
        return text.append("""
                Give the positions of the cards to shuffle back into your deck (you draw as many new ones).
                An empty list keeps the whole hand.""").toString();
    }

    private static String cards(List<Card> cards) {
        return cards.stream().distinct().map(LlmPrompts::card).collect(Collectors.joining("; "));
    }

    private static String card(Card card) {
        return card.name() + " (" + card.cost() + ", " + card.category() + (card.text().isEmpty() ? "" : ": "
                + card.text()) + ")";
    }

    private static String minions(List<MinionView> minions) {
        if (minions.isEmpty()) {
            return "none";
        }
        return minions.stream().map(m -> m.name() + " " + m.attack() + "/" + m.health() + (m.taunt() ? " Taunt" : "")
                + (m.canAttack() ? "" : " (cannot attack yet)")).collect(Collectors.joining(", "));
    }
}
