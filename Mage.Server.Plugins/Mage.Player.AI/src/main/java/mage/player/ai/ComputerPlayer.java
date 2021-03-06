/*
 * Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of BetaSteward_at_googlemail.com.
 */
package mage.player.ai;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import mage.MageObject;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.ActivatedAbility;
import mage.abilities.Mode;
import mage.abilities.Modes;
import mage.abilities.SpellAbility;
import mage.abilities.TriggeredAbility;
import mage.abilities.costs.VariableCost;
import mage.abilities.costs.mana.ColoredManaCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.costs.mana.HybridManaCost;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.MonoHybridManaCost;
import mage.abilities.costs.mana.PhyrexianManaCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureSourceEffect;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.EquipAbility;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.abilities.keyword.FlashAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.mana.ManaAbility;
import mage.abilities.mana.ManaOptions;
import mage.cards.Card;
import mage.cards.Cards;
import mage.cards.decks.Deck;
import mage.cards.repository.CardCriteria;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.cards.repository.ExpansionInfo;
import mage.cards.repository.ExpansionRepository;
import mage.choices.Choice;
import mage.choices.ChoiceColor;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.ColoredManaSymbol;
import mage.constants.Outcome;
import static mage.constants.PhaseStep.DECLARE_ATTACKERS;
import static mage.constants.PhaseStep.DECLARE_BLOCKERS;
import static mage.constants.PhaseStep.DRAW;
import static mage.constants.PhaseStep.END_COMBAT;
import static mage.constants.PhaseStep.POSTCOMBAT_MAIN;
import static mage.constants.PhaseStep.PRECOMBAT_MAIN;
import static mage.constants.PhaseStep.UPKEEP;
import mage.constants.RangeOfInfluence;
import mage.constants.Rarity;
import static mage.constants.SpellAbilityType.SPLIT;
import static mage.constants.SpellAbilityType.SPLIT_FUSED;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreatureForCombatBlock;
import mage.filter.common.FilterCreatureOrPlayer;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.common.FilterLandCard;
import mage.filter.common.FilterNonlandCard;
import mage.filter.common.FilterPermanentOrPlayer;
import mage.filter.common.FilterPlaneswalkerPermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.combat.CombatGroup;
import mage.game.draft.Draft;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.match.Match;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.game.tournament.Tournament;
import mage.player.ai.simulators.CombatGroupSimulator;
import mage.player.ai.simulators.CombatSimulator;
import mage.player.ai.simulators.CreatureSimulator;
import mage.player.ai.utils.RateCard;
import mage.players.Player;
import mage.players.PlayerImpl;
import mage.players.net.UserData;
import mage.players.net.UserGroup;
import mage.target.Target;
import mage.target.TargetAmount;
import mage.target.TargetCard;
import mage.target.TargetPermanent;
import mage.target.TargetPlayer;
import mage.target.TargetSource;
import mage.target.TargetSpell;
import mage.target.common.TargetCardInASingleGraveyard;
import mage.target.common.TargetCardInExile;
import mage.target.common.TargetCardInGraveyard;
import mage.target.common.TargetCardInHand;
import mage.target.common.TargetCardInLibrary;
import mage.target.common.TargetCardInOpponentsGraveyard;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.common.TargetControlledPermanent;
import mage.target.common.TargetCreatureOrPlayer;
import mage.target.common.TargetCreatureOrPlayerAmount;
import mage.target.common.TargetDefender;
import mage.target.common.TargetDiscard;
import mage.target.common.TargetPermanentOrPlayer;
import mage.target.common.TargetSpellOrPermanent;
import mage.util.Copier;
import mage.util.TreeNode;
import org.apache.log4j.Logger;

/**
 *
 * suitable for two player games and some multiplayer games
 *
 * @author BetaSteward_at_googlemail.com
 */
public class ComputerPlayer extends PlayerImpl implements Player {

    private transient final static Logger log = Logger.getLogger(ComputerPlayer.class);

    protected int PASSIVITY_PENALTY = 5; // Penalty value for doing nothing if some actions are availble
    protected boolean ALLOW_INTERRUPT = false; // change this for test / debugging purposes to false to switch off interrupts while debugging

    private transient Map<Mana, Card> unplayable = new TreeMap<>();
    private transient List<Card> playableNonInstant = new ArrayList<>();
    private transient List<Card> playableInstant = new ArrayList<>();
    private transient List<ActivatedAbility> playableAbilities = new ArrayList<>();
    private transient List<PickedCard> pickedCards;
    private transient List<ColoredManaSymbol> chosenColors;

    private transient ManaCost currentUnpaidMana;

    public ComputerPlayer(String name, RangeOfInfluence range) {
        super(name, range);
        human = false;
        userData = UserData.getDefaultUserDataView();
        userData.setAvatarId(64);
        userData.setGroupId(UserGroup.COMPUTER.getGroupId());
        userData.setFlagName("computer.png");
        pickedCards = new ArrayList<>();
    }

    protected ComputerPlayer(UUID id) {
        super(id);
        pickedCards = new ArrayList<>();
    }

    public ComputerPlayer(final ComputerPlayer player) {
        super(player);
    }

    @Override
    public boolean chooseMulligan(Game game) {
        log.debug("chooseMulligan");
        if (hand.size() < 6 || isTestMode()) {
            return false;
        }
        Set<Card> lands = hand.getCards(new FilterLandCard(), game);
        return lands.size() < 2 || lands.size() > hand.size() - 2;
    }

    @Override
    public boolean choose(Outcome outcome, Target target, UUID sourceId, Game game) {
        return choose(outcome, target, sourceId, game, null);
    }

    @Override
    public boolean choose(Outcome outcome, Target target, UUID sourceId, Game game, Map<String, Serializable> options) {
        if (log.isDebugEnabled()) {
            log.debug("chooseTarget: " + outcome.toString() + ":" + target.toString());
        }
        // sometimes a target selection can be made from a player that does not control the ability
        UUID abilityControllerId = playerId;
        if (target.getTargetController() != null && target.getAbilityController() != null) {
            abilityControllerId = target.getAbilityController();
        }
        UUID opponentId = game.getOpponents(abilityControllerId).iterator().next();
        if (target instanceof TargetPlayer) {
            if (outcome.isGood()) {
                if (target.canTarget(abilityControllerId, game)) {
                    target.add(abilityControllerId, game);
                    return true;
                }
                if (target.isRequired(sourceId, game)) {
                    if (target.canTarget(opponentId, game)) {
                        target.add(opponentId, game);
                        return true;
                    }
                }
            } else {
                if (target.canTarget(opponentId, game)) {
                    target.add(opponentId, game);
                    return true;
                }
                if (target.isRequired(sourceId, game)) {
                    if (target.canTarget(abilityControllerId, game)) {
                        target.add(abilityControllerId, game);
                        return true;
                    }
                }
            }
            return false;
        }
        if (target instanceof TargetDiscard) {
            findPlayables(game);
            if (unplayable.size() > 0) {
                for (int i = unplayable.size() - 1; i >= 0; i--) {
                    if (target.canTarget(unplayable.values().toArray(new Card[0])[i].getId(), game)) {
                        target.add(unplayable.values().toArray(new Card[0])[i].getId(), game);
                        if (target.isChosen()) {
                            return true;
                        }
                    }
                }
            }
            if (hand.size() > 0) {
                for (int i = 0; i < hand.size(); i++) {
                    if (target.canTarget(hand.toArray(new UUID[0])[i], game)) {
                        target.add(hand.toArray(new UUID[0])[i], game);
                        if (target.isChosen()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        if (target instanceof TargetControlledPermanent) {
            List<Permanent> targets;
            targets = threats(abilityControllerId, sourceId, ((TargetControlledPermanent) target).getFilter(), game, target.getTargets());
            if (!outcome.isGood()) {
                Collections.reverse(targets);
            }
            for (Permanent permanent : targets) {
                if (((TargetControlledPermanent) target).canTarget(abilityControllerId, permanent.getId(), sourceId, game, false) && !target.getTargets().contains(permanent.getId())) {
                    target.add(permanent.getId(), game);
                    return true;
                }
            }
            return false;
        }
        if (target instanceof TargetPermanent) {
            List<Permanent> targets;
            if (outcome.isCanTargetAll()) {
                targets = threats(null, sourceId, ((TargetPermanent) target).getFilter(), game, target.getTargets());
            } else {
                if (outcome.isGood()) {
                    targets = threats(abilityControllerId, sourceId, ((TargetPermanent) target).getFilter(), game, target.getTargets());
                } else {
                    targets = threats(opponentId, sourceId, ((TargetPermanent) target).getFilter(), game, target.getTargets());
                }
                if (targets.isEmpty() && target.isRequired()) {
                    if (!outcome.isGood()) {
                        targets = threats(abilityControllerId, sourceId, ((TargetPermanent) target).getFilter(), game, target.getTargets());
                    } else {
                        targets = threats(opponentId, sourceId, ((TargetPermanent) target).getFilter(), game, target.getTargets());
                    }
                }
            }
            for (Permanent permanent : targets) {
                if (((TargetPermanent) target).canTarget(abilityControllerId, permanent.getId(), null, game) && !target.getTargets().contains(permanent.getId())) {
                    // stop to add targets if not needed and outcome is no advantage for AI player
                    if (target.getNumberOfTargets() == target.getTargets().size()) {
                        if (outcome.isGood() && hasOpponent(permanent.getControllerId(), game)) {
                            return true;
                        }
                        if (!outcome.isGood() && !hasOpponent(permanent.getControllerId(), game)) {
                            return true;
                        }
                    }
                    // add the target
                    target.add(permanent.getId(), game);
                    if (target.doneChosing()) {
                        return true;
                    }
                }
            }
            return target.isChosen();
        }

        if (target instanceof TargetCardInHand) {
            List<Card> cards = new ArrayList<>();
            for (UUID cardId : ((TargetCardInHand) target).possibleTargets(sourceId, this.getId(), game)) {
                Card card = game.getCard(cardId);
                if (card != null) {
                    cards.add(card);
                }
            }
            while ((outcome.isGood() ? target.getTargets().size() < target.getMaxNumberOfTargets() : !target.isChosen())
                    && !cards.isEmpty()) {
                Card pick = pickTarget(cards, outcome, target, null, game);
                if (pick != null) {
                    target.addTarget(pick.getId(), null, game);
                    cards.remove(pick);
                }
            }
            return target.isChosen();
        }
        if (target instanceof TargetCreatureOrPlayer) {
            List<Permanent> targets;
            TargetCreatureOrPlayer t = ((TargetCreatureOrPlayer) target);
            if (outcome.isGood()) {
                targets = threats(abilityControllerId, sourceId, ((FilterCreatureOrPlayer) t.getFilter()).getCreatureFilter(), game, target.getTargets());
            } else {
                targets = threats(opponentId, sourceId, ((FilterCreatureOrPlayer) t.getFilter()).getCreatureFilter(), game, target.getTargets());
            }
            for (Permanent permanent : targets) {
                List<UUID> alreadyTargetted = target.getTargets();
                if (t.canTarget(abilityControllerId, permanent.getId(), null, game)) {
                    if (alreadyTargetted != null && !alreadyTargetted.contains(permanent.getId())) {
                        target.add(permanent.getId(), game);
                        return true;
                    }
                }
            }
            if (outcome.isGood()) {
                if (target.canTarget(abilityControllerId, null, game)) {
                    target.add(abilityControllerId, game);
                    return true;
                }
            } else {
                if (target.canTarget(opponentId, null, game)) {
                    target.add(opponentId, game);
                    return true;
                }
            }
            if (!target.isRequired(sourceId, game)) {
                return false;
            }
        }

        if (target instanceof TargetPermanentOrPlayer) {
            List<Permanent> targets;
            TargetPermanentOrPlayer t = ((TargetPermanentOrPlayer) target);
            List<Permanent> ownedTargets = threats(abilityControllerId, sourceId, ((FilterPermanentOrPlayer) t.getFilter()).getPermanentFilter(), game, target.getTargets());;
            List<Permanent> opponentTargets = threats(opponentId, sourceId, ((FilterPermanentOrPlayer) t.getFilter()).getPermanentFilter(), game, target.getTargets());
            if (outcome.isGood()) {
                targets = ownedTargets;
            } else {
                targets = opponentTargets;
            }
            for (Permanent permanent : targets) {
                List<UUID> alreadyTargetted = target.getTargets();
                if (t.canTarget(permanent.getId(), game)) {
                    if (alreadyTargetted != null && !alreadyTargetted.contains(permanent.getId())) {
                        target.add(permanent.getId(), game);
                        return true;
                    }
                }
            }
            if (outcome.isGood()) {
                if (target.canTarget(abilityControllerId, null, game)) {
                    target.add(abilityControllerId, game);
                    return true;
                }
            } else {
                if (target.canTarget(opponentId, null, game)) {
                    target.add(opponentId, game);
                    return true;
                }
            }
            if (!target.isRequired(sourceId, game) || target.getNumberOfTargets() == 0) {
                return false;
            }
            if (target.canTarget(opponentId, null, game)) {
                target.add(opponentId, game);
                return true;
            }
            if (target.canTarget(abilityControllerId, null, game)) {
                target.add(abilityControllerId, game);
                return true;
            }
            if (outcome.isGood()) { // no other valid targets so use a permanent
                targets = opponentTargets;
            } else {
                targets = ownedTargets;
            }
            for (Permanent permanent : targets) {
                List<UUID> alreadyTargetted = target.getTargets();
                if (t.canTarget(permanent.getId(), game)) {
                    if (alreadyTargetted != null && !alreadyTargetted.contains(permanent.getId())) {
                        target.add(permanent.getId(), game);
                        return true;
                    }
                }
            }
            return false;
        }
        if (target instanceof TargetCardInGraveyard) {
            List<Card> cards = new ArrayList<>();
            for (Player player : game.getPlayers().values()) {
                for (Card card : player.getGraveyard().getCards(game)) {
                    if (target.canTarget(card.getId(), game)) {
                        cards.add(card);
                    }
                }
            }
            for (Card card : cards) {
                target.add(card.getId(), game);
                if (target.isChosen()) {
                    return true;
                }
            }
            return target.isChosen();
        }

        if (target instanceof TargetCardInYourGraveyard) {
            List<UUID> alreadyTargetted = target.getTargets();
            List<Card> cards = new ArrayList<>(game.getPlayer(abilityControllerId).getGraveyard().getCards(game));
            while (!cards.isEmpty()) {
                Card card = pickTarget(cards, outcome, target, null, game);
                if (card != null && alreadyTargetted != null && !alreadyTargetted.contains(card.getId())) {
                    target.add(card.getId(), game);
                    return true;
                }
            }
            return false;
        }
        if (target instanceof TargetSource) {
            Set<UUID> targets;
            TargetSource t = ((TargetSource) target);
            targets = t.possibleTargets(sourceId, abilityControllerId, game);
            for (UUID targetId : targets) {
                MageObject targetObject = game.getObject(targetId);
                if (targetObject != null) {
                    List<UUID> alreadyTargetted = target.getTargets();
                    if (t.canTarget(targetObject.getId(), game)) {
                        if (alreadyTargetted != null && !alreadyTargetted.contains(targetObject.getId())) {
                            target.add(targetObject.getId(), game);
                            return true;
                        }
                    }
                }
            }
            if (!target.isRequired(sourceId, game)) {
                return false;
            }
            throw new IllegalStateException("TargetSource wasn't handled. class:" + target.getClass().toString());
        }

        throw new IllegalStateException("Target wasn't handled. class:" + target.getClass().toString());
    }

    @Override
    public boolean chooseTarget(Outcome outcome, Target target, Ability source, Game game) {
        if (log.isDebugEnabled()) {
            log.debug("chooseTarget: " + outcome.toString() + ":" + target.toString());
        }
        // sometimes a target selection can be made from a player that does not control the ability
        UUID abilityControllerId = playerId;
        if (target.getAbilityController() != null) {
            abilityControllerId = target.getAbilityController();
        }
        UUID opponentId = game.getOpponents(abilityControllerId).iterator().next();
        if (target instanceof TargetPlayer) {
            if (outcome.isGood()) {
                if (target.canTarget(abilityControllerId, abilityControllerId, source, game)) {
                    target.addTarget(playerId, source, game);
                    return true;
                }
                if (target.isRequired(source)) {
                    if (target.canTarget(abilityControllerId, opponentId, source, game)) {
                        target.addTarget(opponentId, source, game);
                        return true;
                    }
                }
            } else {
                if (target.canTarget(abilityControllerId, opponentId, source, game)) {
                    target.addTarget(opponentId, source, game);
                    return true;
                }
                if (target.isRequired(source)) {
                    if (target.canTarget(abilityControllerId, abilityControllerId, source, game)) {
                        target.addTarget(abilityControllerId, source, game);
                        return true;
                    }
                }
            }
            return false;
        }
        if (target instanceof TargetDiscard || target instanceof TargetCardInHand) {
            if (outcome.isGood()) {
                ArrayList<Card> cardsInHand = new ArrayList<>(hand.getCards(game));
                while (!target.isChosen() && !cardsInHand.isEmpty() && target.getMaxNumberOfTargets() > target.getTargets().size()) {
                    Card card = pickBestCard(cardsInHand, null, target, source, game);
                    if (card != null) {
                        if (target.canTarget(getId(), card.getId(), source, game)) {
                            target.addTarget(card.getId(), source, game);
                            cardsInHand.remove(card);
                            if (target.isChosen()) {
                                return true;
                            }
                        }
                    }
                }
            } else {
                findPlayables(game);
                if (unplayable.size() > 0) {
                    for (int i = unplayable.size() - 1; i >= 0; i--) {
                        if (target.canTarget(getId(), unplayable.values().toArray(new Card[0])[i].getId(), source, game)) {
                            target.addTarget(unplayable.values().toArray(new Card[0])[i].getId(), source, game);
                            if (target.isChosen()) {
                                return true;
                            }
                        }
                    }
                }
                if (hand.size() > 0) {
                    for (int i = 0; i < hand.size(); i++) {
                        if (target.canTarget(getId(), hand.toArray(new UUID[0])[i], source, game)) {
                            target.addTarget(hand.toArray(new UUID[0])[i], source, game);
                            if (target.isChosen()) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        if (target instanceof TargetControlledPermanent) {
            List<Permanent> targets;
            targets = threats(abilityControllerId, source.getSourceId(), ((TargetControlledPermanent) target).getFilter(), game, target.getTargets());
            if (!outcome.isGood()) {
                Collections.reverse(targets);
            }
            for (Permanent permanent : targets) {
                if (((TargetControlledPermanent) target).canTarget(abilityControllerId, permanent.getId(), source, game)) {
                    target.addTarget(permanent.getId(), source, game);
                    if (target.getNumberOfTargets() <= target.getTargets().size() && (!outcome.isGood() || target.getMaxNumberOfTargets() <= target.getTargets().size())) {
                        return true;
                    }
                }
            }
            return target.isChosen();

        }
        if (target instanceof TargetPermanent) {
            List<Permanent> targets;
            boolean outcomeTargets = true;
            if (outcome.isGood()) {
                targets = threats(abilityControllerId, source == null ? null : source.getSourceId(), ((TargetPermanent) target).getFilter(), game, target.getTargets());
            } else {
                targets = threats(opponentId, source == null ? null : source.getSourceId(), ((TargetPermanent) target).getFilter(), game, target.getTargets());
            }
            if (targets.isEmpty() && target.isRequired(source)) {
                targets = threats(null, source == null ? null : source.getSourceId(), ((TargetPermanent) target).getFilter(), game, target.getTargets());
                Collections.reverse(targets);
                outcomeTargets = false;
                //targets = game.getBattlefield().getActivePermanents(((TargetPermanent)target).getFilter(), playerId, game);
            }
            for (Permanent permanent : targets) {
                if (((TargetPermanent) target).canTarget(abilityControllerId, permanent.getId(), source, game)) {
                    target.addTarget(permanent.getId(), source, game);
                    if (!outcomeTargets || target.getMaxNumberOfTargets() <= target.getTargets().size()) {
                        return true;
                    }
                }
            }
            return target.isChosen();
        }
        if (target instanceof TargetCreatureOrPlayer) {
            List<Permanent> targets;
            TargetCreatureOrPlayer t = ((TargetCreatureOrPlayer) target);
            if (outcome.isGood()) {
                targets = threats(abilityControllerId, source.getSourceId(), ((FilterCreatureOrPlayer) t.getFilter()).getCreatureFilter(), game, target.getTargets());
            } else {
                targets = threats(opponentId, source.getSourceId(), ((FilterCreatureOrPlayer) t.getFilter()).getCreatureFilter(), game, target.getTargets());
            }

            if (targets.isEmpty()) {
                if (outcome.isGood()) {
                    if (target.canTarget(getId(), abilityControllerId, source, game)) {
                        target.addTarget(abilityControllerId, source, game);
                        return true;
                    }
                } else {
                    if (target.canTarget(getId(), opponentId, source, game)) {
                        target.addTarget(opponentId, source, game);
                        return true;
                    }
                }
            }

            if (targets.isEmpty() && target.isRequired(source)) {
                targets = game.getBattlefield().getActivePermanents(((FilterCreatureOrPlayer) t.getFilter()).getCreatureFilter(), playerId, game);
            }
            for (Permanent permanent : targets) {
                List<UUID> alreadyTargetted = target.getTargets();
                if (t.canTarget(abilityControllerId, permanent.getId(), source, game)) {
                    if (alreadyTargetted != null && !alreadyTargetted.contains(permanent.getId())) {
                        target.addTarget(permanent.getId(), source, game);
                        return true;
                    }
                }
            }

            if (outcome.isGood()) {
                if (target.canTarget(getId(), abilityControllerId, source, game)) {
                    target.addTarget(abilityControllerId, source, game);
                    return true;
                }
            } else {
                if (target.canTarget(getId(), opponentId, source, game)) {
                    target.addTarget(opponentId, source, game);
                    return true;
                }
            }

            //if (!target.isRequired())
            return false;
        }
        if (target instanceof TargetCardInGraveyard) {
            List<Card> cards = new ArrayList<>();
            for (Player player : game.getPlayers().values()) {
                cards.addAll(player.getGraveyard().getCards(game));
            }
            Card card = pickTarget(cards, outcome, target, source, game);
            if (card != null) {
                target.addTarget(card.getId(), source, game);
                return true;
            }
            //if (!target.isRequired())
            return false;
        }
        if (target instanceof TargetCardInLibrary) {
            List<Card> cards = new ArrayList<>(game.getPlayer(abilityControllerId).getLibrary().getCards(game));
            Card card = pickTarget(cards, outcome, target, source, game);
            if (card != null) {
                target.addTarget(card.getId(), source, game);
                return true;
            }
            return false;
        }
        if (target instanceof TargetCardInYourGraveyard) {
            List<Card> cards = new ArrayList<>(game.getPlayer(abilityControllerId).getGraveyard().getCards(game));
            while (!target.isChosen() && !cards.isEmpty()) {
                Card card = pickTarget(cards, outcome, target, source, game);
                if (card != null) {
                    target.addTarget(card.getId(), source, game);
                }
            }
            return target.isChosen();
        }
        if (target instanceof TargetCardInHand) {
            List<Card> cards = new ArrayList<>();
            cards.addAll(this.hand.getCards(game));
            while (!target.isChosen() && !cards.isEmpty()) {
                Card pick = pickTarget(cards, outcome, target, source, game);
                if (pick != null) {
                    target.addTarget(pick.getId(), source, game);
                }
            }
            return target.isChosen();
        }
        if (target instanceof TargetSpell) {
            if (game.getStack().size() > 0) {
                Iterator<StackObject> it = game.getStack().iterator();
                while (it.hasNext()) {
                    StackObject o = it.next();
                    if (o instanceof Spell && !source.getId().equals(o.getStackAbility().getId())) {
                        target.addTarget(o.getId(), source, game);
                        return true;
                    }
                }
            }
            return false;
        }
        if (target instanceof TargetSpellOrPermanent) {
            List<Permanent> targets;
            boolean outcomeTargets = true;
            if (outcome.isGood()) {
                targets = threats(abilityControllerId, source == null ? null : source.getSourceId(), ((TargetSpellOrPermanent) target).getPermanentFilter(), game, target.getTargets());
            } else {
                targets = threats(opponentId, source == null ? null : source.getSourceId(), ((TargetSpellOrPermanent) target).getPermanentFilter(), game, target.getTargets());
            }
            if (targets.isEmpty() && target.isRequired(source)) {
                targets = threats(null, source == null ? null : source.getSourceId(), ((TargetSpellOrPermanent) target).getPermanentFilter(), game, target.getTargets());
                Collections.reverse(targets);
                outcomeTargets = false;
                //targets = game.getBattlefield().getActivePermanents(((TargetPermanent)target).getFilter(), playerId, game);
            }
            for (Permanent permanent : targets) {
                if (((TargetPermanent) target).canTarget(abilityControllerId, permanent.getId(), source, game)) {
                    target.addTarget(permanent.getId(), source, game);
                    if (!outcomeTargets || target.getMaxNumberOfTargets() <= target.getTargets().size()) {
                        return true;
                    }
                }
            }
            if (game.getStack().size() > 0) {
                Iterator<StackObject> it = game.getStack().iterator();
                while (it.hasNext()) {
                    StackObject stackObject = it.next();
                    if (stackObject instanceof Spell && source != null && !source.getId().equals(stackObject.getStackAbility().getId())) {
                        if (((TargetSpellOrPermanent) target).getFilter().match(stackObject, game)) {
                            target.addTarget(stackObject.getId(), source, game);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        if (target instanceof TargetCardInOpponentsGraveyard) {
            List<Card> cards = new ArrayList<>();
            for (UUID uuid : game.getOpponents(abilityControllerId)) {
                Player player = game.getPlayer(uuid);
                if (player != null) {
                    cards.addAll(player.getGraveyard().getCards(game));
                }
            }
            Card card = pickTarget(cards, outcome, target, source, game);
            if (card != null) {
                target.addTarget(card.getId(), source, game);
                return true;
            }
            //if (!target.isRequired())
            return false;
        }
        if (target instanceof TargetDefender) {
            // TODO: Improve, now planeswalker is always chosen if it exits
            List<Permanent> targets;
            targets = game.getBattlefield().getActivePermanents(new FilterPlaneswalkerPermanent(), opponentId, game);
            if (targets != null && !targets.isEmpty()) {
                for (Permanent planeswalker : targets) {
                    if (target.canTarget(getId(), planeswalker.getId(), source, game)) {
                        target.addTarget(planeswalker.getId(), source, game);
                    }
                    if (target.isChosen()) {
                        return true;
                    }
                }
            }
            if (!target.isChosen()) {
                if (target.canTarget(getId(), opponentId, source, game)) {
                    target.addTarget(opponentId, source, game);
                }
            }
            return target.isChosen();
        }

        if (target instanceof TargetCardInASingleGraveyard) {
            List<Card> cards = new ArrayList<>();
            for (Player player : game.getPlayers().values()) {
                cards.addAll(player.getGraveyard().getCards(game));
            }
            while (!target.isChosen() && !cards.isEmpty()) {
                Card pick = pickTarget(cards, outcome, target, source, game);
                if (pick != null) {
                    target.addTarget(pick.getId(), source, game);
                }
            }
            return target.isChosen();
        }

        if (target instanceof TargetCardInExile) {
            List<Card> cards = new ArrayList<>();
            for (UUID uuid : ((TargetCardInExile) target).possibleTargets(source.getSourceId(), source.getControllerId(), game)) {
                Card card = game.getCard(uuid);
                if (card != null) {
                    cards.add(card);
                }
            }
            while (!target.isChosen() && !cards.isEmpty()) {
                Card pick = pickTarget(cards, outcome, target, source, game);
                if (pick != null) {
                    target.addTarget(pick.getId(), source, game);
                }
            }
            return target.isChosen();
        }

        throw new IllegalStateException("Target wasn't handled. class:" + target.getClass().toString());
    }

    protected Card pickTarget(List<Card> cards, Outcome outcome, Target target, Ability source, Game game) {
        Card card;
        while (!cards.isEmpty()) {
            if (outcome.isGood()) {
                card = pickBestCard(cards, null, target, source, game);
            } else {
                card = pickWorstCard(cards, null, target, source, game);
            }
            if (source != null) {
                if (target.canTarget(getId(), card.getId(), source, game)) {
                    return card;
                }
            } else {
                return card;
            }
            cards.remove(card);
        }
        return null;
    }

    @Override
    public boolean chooseTargetAmount(Outcome outcome, TargetAmount target, Ability source, Game game) {
        if (log.isDebugEnabled()) {
            log.debug("chooseTarget: " + outcome.toString() + ":" + target.toString());
        }
        UUID opponentId = game.getOpponents(playerId).iterator().next();
        if (target instanceof TargetCreatureOrPlayerAmount) {
            if (outcome.equals(Outcome.Damage) && game.getPlayer(opponentId).getLife() <= target.getAmountRemaining()) {
                target.addTarget(opponentId, target.getAmountRemaining(), source, game);
                return true;
            }
            List<Permanent> targets;
            if (outcome.isGood()) {
                targets = threats(playerId, source.getSourceId(), new FilterCreaturePermanent(), game, target.getTargets());
            } else {
                targets = threats(opponentId, source.getSourceId(), new FilterCreaturePermanent(), game, target.getTargets());
            }
            for (Permanent permanent : targets) {
                if (target.canTarget(getId(), permanent.getId(), source, game)) {
                    if (permanent.getToughness().getValue() <= target.getAmountRemaining()) {
                        target.addTarget(permanent.getId(), permanent.getToughness().getValue(), source, game);
                        return true;
                    }
                }
            }
            if (outcome.isGood() && target.canTarget(getId(), getId(), source, game)) {
                target.addTarget(opponentId, target.getAmountRemaining(), source, game);
                return true;
            } else if (target.canTarget(getId(), opponentId, source, game)) {
                // no permanent target so take opponent
                target.addTarget(opponentId, target.getAmountRemaining(), source, game);
                return true;
            } else if (target.canTarget(getId(), playerId, source, game)) {
                target.addTarget(opponentId, target.getAmountRemaining(), source, game);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean priority(Game game) {
        game.resumeTimer(getTurnControlledBy());
        log.debug("priority");
        boolean result = priorityPlay(game);
        game.pauseTimer(getTurnControlledBy());
        return result;
    }

    private boolean priorityPlay(Game game) {
        UUID opponentId = game.getOpponents(playerId).iterator().next();
        if (game.getActivePlayerId().equals(playerId)) {
            if (game.isMainPhase() && game.getStack().isEmpty()) {
                playLand(game);
            }
            switch (game.getTurn().getStepType()) {
                case UPKEEP:
                    findPlayables(game);
                    break;
                case DRAW:
                    logState(game);
                    break;
                case PRECOMBAT_MAIN:
                    findPlayables(game);
                    if (playableAbilities.size() > 0) {
                        for (ActivatedAbility ability : playableAbilities) {
                            if (ability.canActivate(playerId, game)) {
                                if (ability.getEffects().hasOutcome(Outcome.PutLandInPlay)) {
                                    if (this.activateAbility(ability, game)) {
                                        return true;
                                    }
                                }
                                if (ability.getEffects().hasOutcome(Outcome.PutCreatureInPlay)) {
                                    if (getOpponentBlockers(opponentId, game).size() <= 1) {
                                        if (this.activateAbility(ability, game)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case DECLARE_BLOCKERS:
                    findPlayables(game);
                    playRemoval(game.getCombat().getBlockers(), game);
                    playDamage(game.getCombat().getBlockers(), game);
                    break;
                case END_COMBAT:
                    findPlayables(game);
                    playDamage(game.getCombat().getBlockers(), game);
                    break;
                case POSTCOMBAT_MAIN:
                    findPlayables(game);
                    if (game.getStack().isEmpty()) {
                        if (playableNonInstant.size() > 0) {
                            for (Card card : playableNonInstant) {
                                if (card.getSpellAbility().canActivate(playerId, game)) {
                                    if (this.activateAbility(card.getSpellAbility(), game)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        if (playableAbilities.size() > 0) {
                            for (ActivatedAbility ability : playableAbilities) {
                                if (ability.canActivate(playerId, game)) {
                                    if (!(ability.getEffects().get(0) instanceof BecomesCreatureSourceEffect)) {
                                        if (this.activateAbility(ability, game)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
        } else {
            //respond to opponent events
            switch (game.getTurn().getStepType()) {
                case UPKEEP:
                    findPlayables(game);
                    break;
                case DECLARE_ATTACKERS:
                    findPlayables(game);
                    playRemoval(game.getCombat().getAttackers(), game);
                    playDamage(game.getCombat().getAttackers(), game);
                    break;
                case END_COMBAT:
                    findPlayables(game);
                    playDamage(game.getCombat().getAttackers(), game);
                    break;
            }
        }
        pass(game);
        return true;
    }

    @Override
    public boolean activateAbility(ActivatedAbility ability, Game game) {
        if (!isTestMode()) { // Test player already sends target event as he selects the target
            for (Target target : ability.getModes().getMode().getTargets()) {
                for (UUID targetId : target.getTargets()) {
                    game.fireEvent(GameEvent.getEvent(EventType.TARGETED, targetId, ability.getId(), ability.getControllerId()));
                }
            }
        }
        return super.activateAbility(ability, game);
    }

    protected void playLand(Game game) {
        log.debug("playLand");
        Set<Card> lands = new LinkedHashSet<>();
        for (Card landCard : hand.getCards(new FilterLandCard(), game)) {
            // remove lands that can not be played
            if (game.getContinuousEffects().preventedByRuleModification(GameEvent.getEvent(GameEvent.EventType.PLAY_LAND, landCard.getId(), landCard.getId(), playerId), null, game, true)) {
                break;
            }
            lands.add(landCard);
        }
        while (lands.size() > 0 && this.canPlayLand()) {
            if (lands.size() == 1) {
                this.playLand(lands.iterator().next(), game);
            } else {
                playALand(lands, game);
            }
        }
    }

    protected void playALand(Set<Card> lands, Game game) {
        log.debug("playALand");
        //play a land that will allow us to play an unplayable
        for (Mana mana : unplayable.keySet()) {
            for (Card card : lands) {
                for (ManaAbility ability : card.getAbilities().getManaAbilities(Zone.BATTLEFIELD)) {
                    for (Mana netMana : ability.getNetMana(game)) {
                        if (netMana.enough(mana)) {
                            this.playLand(card, game);
                            lands.remove(card);
                            return;
                        }
                    }
                }
            }
        }
        //play a land that will get us closer to playing an unplayable
        for (Mana mana : unplayable.keySet()) {
            for (Card card : lands) {
                for (ManaAbility ability : card.getAbilities().getManaAbilities(Zone.BATTLEFIELD)) {
                    for (Mana netMana : ability.getNetMana(game)) {
                        if (mana.contains(netMana)) {
                            this.playLand(card, game);
                            lands.remove(card);
                            return;
                        }
                    }
                }
            }
        }
        //play first available land
        this.playLand(lands.iterator().next(), game);
        lands.remove(lands.iterator().next());
    }

    protected void findPlayables(Game game) {
        playableInstant.clear();
        playableNonInstant.clear();
        unplayable.clear();
        playableAbilities.clear();
        Set<Card> nonLands = hand.getCards(new FilterNonlandCard(), game);
        ManaOptions available = getManaAvailable(game);
        available.addMana(manaPool.getMana());

        for (Card card : nonLands) {
            ManaOptions options = card.getManaCost().getOptions();
            if (card.getManaCost().getVariableCosts().size() > 0) {
                //don't use variable mana costs unless there is at least 3 extra mana for X
                for (Mana option : options) {
                    option.add(Mana.ColorlessMana(3));
                }
            }
            for (Mana mana : options) {
                for (Mana avail : available) {
                    if (mana.enough(avail)) {
                        SpellAbility ability = card.getSpellAbility();
                        if (ability != null && ability.canActivate(playerId, game)
                                && game.getContinuousEffects().preventedByRuleModification(GameEvent.getEvent(GameEvent.EventType.CAST_SPELL, ability.getSourceId(), ability.getSourceId(), playerId), ability, game, true)) {
                            if (card.getCardType().contains(CardType.INSTANT)
                                    || card.hasAbility(FlashAbility.getInstance().getId(), game)) {
                                playableInstant.add(card);
                            } else {
                                playableNonInstant.add(card);
                            }
                        }
                    } else {
                        if (!playableInstant.contains(card) && !playableNonInstant.contains(card)) {
                            unplayable.put(mana.needed(avail), card);
                        }
                    }
                }
            }
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(playerId)) {
            for (ActivatedAbility ability : permanent.getAbilities().getActivatedAbilities(Zone.BATTLEFIELD)) {
                if (!(ability instanceof ManaAbility) && ability.canActivate(playerId, game)) {
                    if (ability instanceof EquipAbility && permanent.getAttachedTo() != null) {
                        continue;
                    }
                    ManaOptions abilityOptions = ability.getManaCosts().getOptions();
                    if (ability.getManaCosts().getVariableCosts().size() > 0) {
                        //don't use variable mana costs unless there is at least 3 extra mana for X
                        for (Mana option : abilityOptions) {
                            option.add(Mana.ColorlessMana(3));
                        }
                    }
                    if (abilityOptions.size() == 0) {
                        playableAbilities.add(ability);
                    } else {
                        for (Mana mana : abilityOptions) {
                            for (Mana avail : available) {
                                if (mana.enough(avail)) {
                                    playableAbilities.add(ability);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Card card : graveyard.getCards(game)) {
            for (ActivatedAbility ability : card.getAbilities().getActivatedAbilities(Zone.GRAVEYARD)) {
                if (ability.canActivate(playerId, game)) {
                    ManaOptions abilityOptions = ability.getManaCosts().getOptions();
                    if (abilityOptions.size() == 0) {
                        playableAbilities.add(ability);
                    } else {
                        for (Mana mana : abilityOptions) {
                            for (Mana avail : available) {
                                if (mana.enough(avail)) {
                                    playableAbilities.add(ability);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("findPlayables: " + playableInstant.toString() + "---" + playableNonInstant.toString() + "---" + playableAbilities.toString());
        }
    }

    @Override
    public boolean playMana(Ability ability, ManaCost unpaid, String promptText, Game game) {
        payManaMode = true;
        currentUnpaidMana = unpaid;
        boolean result = playManaHandling(ability, unpaid, game);
        currentUnpaidMana = null;
        payManaMode = false;
        return result;
    }

    protected boolean playManaHandling(Ability ability, ManaCost unpaid, Game game) {
//        log.info("paying for " + unpaid.getText());
        boolean spendAnyMana = game.getContinuousEffects().asThough(ability.getSourceId(), AsThoughEffectType.SPEND_ANY_MANA, ability, ability.getControllerId(), game);
        ManaCost cost;
        List<Permanent> producers;
        if (unpaid instanceof ManaCosts) {
            ManaCosts<ManaCost> manaCosts = (ManaCosts<ManaCost>) unpaid;
            cost = manaCosts.get(manaCosts.size() - 1);
            producers = getSortedProducers((ManaCosts) unpaid, game);
        } else {
            cost = unpaid;
            producers = this.getAvailableManaProducers(game);
            producers.addAll(this.getAvailableManaProducersWithCost(game));
        }
        for (Permanent perm : producers) {
            // use color producing mana abilities with costs first that produce all color manas that are needed to pay
            // otherwise the computer may not be able to pay the cost for that source
            ManaAbility:
            for (ManaAbility manaAbility : perm.getAbilities().getAvailableManaAbilities(Zone.BATTLEFIELD, game)) {
                int colored = 0;
                for (Mana mana : manaAbility.getNetMana(game)) {
                    if (!unpaid.getMana().includesMana(mana)) {
                        continue ManaAbility;
                    }
                    colored += mana.countColored();
                }
                if (colored > 1 && (cost instanceof ColoredManaCost)) {

                    for (Mana netMana : manaAbility.getNetMana(game)) {
                        if (cost.testPay(netMana)) {
                            if (activateAbility(manaAbility, game)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        for (Permanent perm : producers) {
            // pay all colored costs first
            for (ManaAbility manaAbility : perm.getAbilities().getAvailableManaAbilities(Zone.BATTLEFIELD, game)) {
                if (cost instanceof ColoredManaCost) {
                    for (Mana netMana : manaAbility.getNetMana(game)) {
                        if (cost.testPay(netMana) || spendAnyMana) {
                            if (activateAbility(manaAbility, game)) {
                                return true;
                            }
                        }
                    }
                }
            }
            // then pay hybrid
            for (ManaAbility manaAbility : perm.getAbilities().getAvailableManaAbilities(Zone.BATTLEFIELD, game)) {
                if (cost instanceof HybridManaCost) {
                    for (Mana netMana : manaAbility.getNetMana(game)) {
                        if (cost.testPay(netMana) || spendAnyMana) {
                            if (activateAbility(manaAbility, game)) {
                                return true;
                            }
                        }
                    }
                }
            }
            // then pay mono hybrid
            for (ManaAbility manaAbility : perm.getAbilities().getAvailableManaAbilities(Zone.BATTLEFIELD, game)) {
                if (cost instanceof MonoHybridManaCost) {
                    for (Mana netMana : manaAbility.getNetMana(game)) {
                        if (cost.testPay(netMana) || spendAnyMana) {
                            if (activateAbility(manaAbility, game)) {
                                return true;
                            }
                        }
                    }
                }
            }
            // finally pay generic
            for (ManaAbility manaAbility : perm.getAbilities().getAvailableManaAbilities(Zone.BATTLEFIELD, game)) {
                if (cost instanceof GenericManaCost) {
                    for (Mana netMana : manaAbility.getNetMana(game)) {
                        if (cost.testPay(netMana) || spendAnyMana) {
                            if (activateAbility(manaAbility, game)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        // pay phyrexian life costs
        if (cost instanceof PhyrexianManaCost) {
            if (cost.pay(null, game, null, playerId, false) || spendAnyMana) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * returns a list of Permanents that produce mana sorted by the number of
     * mana the Permanent produces that match the unpaid costs in ascending
     * order
     *
     * the idea is that we should pay costs first from mana producers that
     * produce only one type of mana and save the multi-mana producers for those
     * costs that can't be paid by any other producers
     *
     * @param unpaid - the amount of unpaid mana costs
     * @param game
     * @return List<Permanent>
     */
    private List<Permanent> getSortedProducers(ManaCosts<ManaCost> unpaid, Game game) {
        List<Permanent> unsorted = this.getAvailableManaProducers(game);
        unsorted.addAll(this.getAvailableManaProducersWithCost(game));
        Map<Permanent, Integer> scored = new HashMap<>();
        for (Permanent permanent : unsorted) {
            int score = 0;
            for (ManaCost cost : unpaid) {
                Abilities:
                for (ManaAbility ability : permanent.getAbilities().getAvailableManaAbilities(Zone.BATTLEFIELD, game)) {
                    for (Mana netMana : ability.getNetMana(game)) {
                        if (cost.testPay(netMana)) {
                            score++;
                            break Abilities;
                        }
                    }
                }
            }
            if (score > 0) { // score mana producers that produce other mana types and have other uses higher
                score += permanent.getAbilities().getAvailableManaAbilities(Zone.BATTLEFIELD, game).size();
                score += permanent.getAbilities().getActivatedAbilities(Zone.BATTLEFIELD).size();
                if (!permanent.getCardType().contains(CardType.LAND)) {
                    score += 2;
                } else if (permanent.getCardType().contains(CardType.CREATURE)) {
                    score += 2;
                }
            }
            scored.put(permanent, score);
        }
        return sortByValue(scored);
    }

    private List<Permanent> sortByValue(Map<Permanent, Integer> map) {
        List<Entry<Permanent, Integer>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<Permanent, Integer>>() {
            @Override
            public int compare(Entry<Permanent, Integer> o1, Entry<Permanent, Integer> o2) {
                return (o1.getValue().compareTo(o2.getValue()));
            }
        });
        List<Permanent> result = new ArrayList<>();
        for (Entry<Permanent, Integer> entry : list) {
            result.add(entry.getKey());
        }
        return result;
    }

    @Override
    public int announceXMana(int min, int max, String message, Game game, Ability ability) {
        log.debug("announceXMana");
        //TODO: improve this
        int numAvailable = getAvailableManaProducers(game).size() - ability.getManaCosts().convertedManaCost();
        if (numAvailable < 0) {
            numAvailable = 0;
        }
        return numAvailable;
    }

    @Override
    public int announceXCost(int min, int max, String message, Game game, Ability ability, VariableCost variablCost) {
        log.debug("announceXMana");
        //TODO: improve this
        int value = new Random().nextInt(max + 1);
        if (value < max) {
            value++;
        }
        return value;
    }

    @Override
    public void abort() {
        abort = true;
    }

    @Override
    public void skip() {
    }

    @Override
    public boolean chooseUse(Outcome outcome, String message, Ability source, Game game) {
        log.debug("chooseUse: " + outcome.isGood());
        // Be proactive! Always use abilities, the evaluation function will decide if it's good or not
        // Otherwise some abilities won't be used by AI like LoseTargetEffect that has "bad" outcome
        // but still is good when targets opponent
        return !outcome.equals(Outcome.AIDontUseIt); // Added for Desecration Demon sacrifice ability
    }

    @Override
    public boolean choose(Outcome outcome, Choice choice, Game game) {
        log.debug("choose 3");
        //TODO: improve this
        if (choice.getMessage() != null && choice.getMessage().equals("Choose creature type")) {
            chooseCreatureType(outcome, choice, game);
        }
        // choose the correct color to pay a spell
        if (outcome.equals(Outcome.PutManaInPool) && choice instanceof ChoiceColor && currentUnpaidMana != null) {
            if (currentUnpaidMana.containsColor(ColoredManaSymbol.W) && choice.getChoices().contains("White")) {
                choice.setChoice("White");
                return true;
            }
            if (currentUnpaidMana.containsColor(ColoredManaSymbol.R) && choice.getChoices().contains("Red")) {
                choice.setChoice("Red");
                return true;
            }
            if (currentUnpaidMana.containsColor(ColoredManaSymbol.G) && choice.getChoices().contains("Green")) {
                choice.setChoice("Green");
                return true;
            }
            if (currentUnpaidMana.containsColor(ColoredManaSymbol.U) && choice.getChoices().contains("Blue")) {
                choice.setChoice("Blue");
                return true;
            }
            if (currentUnpaidMana.containsColor(ColoredManaSymbol.B) && choice.getChoices().contains("Black")) {
                choice.setChoice("Black");
                return true;
            }
        }
        // choose by random
        if (!choice.isChosen()) {
            int choiceIdx = (int) (Math.random() * choice.getChoices().size() + 1);
            for (String next : choice.getChoices()) {
                if (--choiceIdx > 0) {
                    continue;
                }
                if (!next.isEmpty()) {
                    choice.setChoice(next);
                    break;
                }
            }
        }
        return true;
    }

    protected boolean chooseCreatureType(Outcome outcome, Choice choice, Game game) {
        if (outcome.equals(Outcome.Detriment)) {
            // choose a creature type of opponent on battlefield or graveyard
            for (Permanent permanent : game.getBattlefield().getActivePermanents(this.getId(), game)) {
                if (game.getOpponents(this.getId()).contains(permanent.getControllerId())
                        && permanent.getCardType().contains(CardType.CREATURE)
                        && permanent.getSubtype().size() > 0) {
                    if (choice.getChoices().contains(permanent.getSubtype().get(0))) {
                        choice.setChoice(permanent.getSubtype().get(0));
                        break;
                    }
                }
            }
            // or in opponent graveyard
            if (!choice.isChosen()) {
                for (UUID opponentId : game.getOpponents(this.getId())) {
                    Player opponent = game.getPlayer(opponentId);
                    for (Card card : opponent.getGraveyard().getCards(game)) {
                        if (card != null && card.getCardType().contains(CardType.CREATURE) && card.getSubtype().size() > 0) {
                            if (choice.getChoices().contains(card.getSubtype().get(0))) {
                                choice.setChoice(card.getSubtype().get(0));
                                break;
                            }
                        }
                    }
                    if (choice.isChosen()) {
                        break;
                    }
                }
            }
        } else {
            // choose a creature type of hand or library
            for (UUID cardId : this.getHand()) {
                Card card = game.getCard(cardId);
                if (card != null && card.getCardType().contains(CardType.CREATURE) && card.getSubtype().size() > 0) {
                    if (choice.getChoices().contains(card.getSubtype().get(0))) {
                        choice.setChoice(card.getSubtype().get(0));
                        break;
                    }
                }
            }
            if (!choice.isChosen()) {
                for (UUID cardId : this.getLibrary().getCardList()) {
                    Card card = game.getCard(cardId);
                    if (card != null && card.getCardType().contains(CardType.CREATURE) && card.getSubtype().size() > 0) {
                        if (choice.getChoices().contains(card.getSubtype().get(0))) {
                            choice.setChoice(card.getSubtype().get(0));
                            break;
                        }
                    }
                }
            }
        }
        return choice.isChosen();
    }

    @Override
    public boolean chooseTarget(Outcome outcome, Cards cards, TargetCard target, Ability source, Game game) {
        log.debug("chooseTarget");
        if (cards == null || cards.isEmpty()) {
            return target.isRequired(source);
        }

        ArrayList<Card> cardChoices = new ArrayList<>(cards.getCards(target.getFilter(), game));
        while (!target.doneChosing()) {
            Card card = pickTarget(cardChoices, outcome, target, source, game);
            if (card != null) {
                target.addTarget(card.getId(), source, game);
                cardChoices.remove(card);
            } else {
                // We don't have any valid target to choose so stop choosing
                return target.getTargets().size() < target.getNumberOfTargets();
            }
            if (outcome.equals(Outcome.Neutral) && target.getTargets().size() > target.getNumberOfTargets() + (target.getMaxNumberOfTargets() - target.getNumberOfTargets()) / 2) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean choose(Outcome outcome, Cards cards, TargetCard target, Game game) {
        log.debug("choose 2");
        if (cards == null || cards.isEmpty()) {
            return true;
        }

        ArrayList<Card> cardChoices = new ArrayList<>(cards.getCards(target.getFilter(), game));
        while (!target.doneChosing()) {
            Card card = pickTarget(cardChoices, outcome, target, null, game);
            if (card != null) {
                target.add(card.getId(), game);
                cardChoices.remove(card);
            } else {
                // We don't have any valid target to choose so stop choosing
                break;
            }
            if (outcome.equals(Outcome.Neutral) && target.getTargets().size() > target.getNumberOfTargets() + (target.getMaxNumberOfTargets() - target.getNumberOfTargets()) / 2) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean choosePile(Outcome outcome, String message, List<? extends Card> pile1, List<? extends Card> pile2, Game game) {
        //TODO: improve this
        return true;
    }

    @Override
    public void selectAttackers(Game game, UUID attackingPlayerId) {
        log.debug("selectAttackers");
        UUID opponentId = game.getCombat().getDefenders().iterator().next();
        Attackers attackers = getPotentialAttackers(game);
        List<Permanent> blockers = getOpponentBlockers(opponentId, game);
        List<Permanent> actualAttackers = new ArrayList<>();
        if (blockers.isEmpty()) {
            actualAttackers = attackers.getAttackers();
        } else if (attackers.size() - blockers.size() >= game.getPlayer(opponentId).getLife()) {
            actualAttackers = attackers.getAttackers();
        } else {
            CombatSimulator combat = simulateAttack(attackers, blockers, opponentId, game);
            if (combat.rating > 2) {
                for (CombatGroupSimulator group : combat.groups) {
                    this.declareAttacker(group.attackers.get(0).id, group.defenderId, game, false);
                }
            }
        }
        for (Permanent attacker : actualAttackers) {
            this.declareAttacker(attacker.getId(), opponentId, game, false);
        }
    }

    @Override
    public void selectBlockers(Game game, UUID defendingPlayerId) {
        log.debug("selectBlockers");

        List<Permanent> blockers = getAvailableBlockers(game);

        CombatSimulator sim = simulateBlock(CombatSimulator.load(game), blockers, game);

        List<CombatGroup> groups = game.getCombat().getGroups();
        for (int i = 0; i < groups.size(); i++) {
            for (CreatureSimulator creature : sim.groups.get(i).blockers) {
                groups.get(i).addBlocker(creature.id, playerId, game);
            }
        }
    }

    @Override
    public int chooseReplacementEffect(Map<String, String> rEffects, Game game) {
        log.debug("chooseReplacementEffect");
        //TODO: implement this
        return 0;
    }

    @Override
    public SpellAbility chooseSpellAbilityForCast(SpellAbility ability, Game game, boolean noMana) {
        switch (ability.getSpellAbilityType()) {
            case SPLIT:
            case SPLIT_FUSED:
                MageObject object = game.getObject(ability.getSourceId());
                if (object != null) {
                    LinkedHashMap<UUID, ActivatedAbility> useableAbilities = getSpellAbilities(object, game.getState().getZone(object.getId()), game);
                    if (useableAbilities != null && useableAbilities.size() > 0) {
                        game.fireGetChoiceEvent(playerId, name, object, new ArrayList<>(useableAbilities.values()));
                        // TODO: Improve this
                        return (SpellAbility) useableAbilities.values().iterator().next();
                    }
                }
                return null;
            default:
                return ability;
        }
    }

    @Override
    public Mode chooseMode(Modes modes, Ability source, Game game) {
        log.debug("chooseMode");
        if (modes.getMode() != null && modes.getMaxModes() == modes.getSelectedModes().size()) {
            // mode was already set by the AI
            return modes.getMode();
        }
        //TODO: improve this;
        for (Mode mode : modes.getAvailableModes(source, game)) {
            if (!modes.getSelectedModes().contains(mode.getId()) // select only modes not already selected
                    && mode.getTargets().canChoose(source.getSourceId(), source.getControllerId(), game)) { // and where targets are available
                return mode;
            }
        }
        return null;
    }

    @Override
    public TriggeredAbility chooseTriggeredAbility(List<TriggeredAbility> abilities, Game game) {
        log.debug("chooseTriggeredAbility: " + abilities.toString());
        //TODO: improve this
        if (abilities.size() > 0) {
            return abilities.get(0);
        }
        return null;
    }

    @Override
    public void assignDamage(int damage, List<UUID> targets, String singleTargetName, UUID sourceId, Game game) {
        log.debug("assignDamage");
        //TODO: improve this
        game.getPermanent(targets.get(0)).damage(damage, sourceId, game, false, true);
    }

    @Override
    public int getAmount(int min, int max, String message, Game game) {
        log.debug("getAmount");
        if (message.startsWith("Assign damage to ")) {
            return min;
        }
        //TODO: improve this
        if (min < max && min == 0) {
            return new Random().nextInt(max + 1);
        }
        return min;
    }

    @Override
    public UUID chooseAttackerOrder(List<Permanent> attackers, Game game) {
        //TODO: improve this
        return attackers.iterator().next().getId();
    }

    @Override
    public UUID chooseBlockerOrder(List<Permanent> blockers, CombatGroup combatGroup, List<UUID> blockerOrder, Game game) {
        //TODO: improve this
        return blockers.iterator().next().getId();
    }

    @Override
    public List<Permanent> getAvailableManaProducers(Game game) {
        return super.getAvailableManaProducers(game);
    }

    @Override
    public void sideboard(Match match, Deck deck) {
        //TODO: improve this
        match.submitDeck(playerId, deck);
    }

    private static void addBasicLands(Deck deck, String landName, int number) {
        Random random = new Random();
        Set<String> landSets = new HashSet<>();

        // decide from which sets basic lands are taken from
        for (String setCode : deck.getExpansionSetCodes()) {
            ExpansionInfo expansionInfo = ExpansionRepository.instance.getSetByCode(setCode);
            if (expansionInfo.hasBasicLands()) {
                landSets.add(expansionInfo.getCode());
            }
        }

        // if sets have no basic land, take land from block
        if (landSets.isEmpty()) {
            for (String setCode : deck.getExpansionSetCodes()) {
                ExpansionInfo expansionInfo = ExpansionRepository.instance.getSetByCode(setCode);
                List<ExpansionInfo> blockSets = ExpansionRepository.instance.getSetsFromBlock(expansionInfo.getBlockName());
                for (ExpansionInfo blockSet : blockSets) {
                    if (blockSet.hasBasicLands()) {
                        landSets.add(blockSet.getCode());
                    }
                }
            }
        }
        // if still no set with lands found, take one by random
        if (landSets.isEmpty()) {
            // if sets have no basic lands and also it has no parent or parent has no lands get last set with lands
            // select a set with basic lands by random
            Random generator = new Random();
            List<ExpansionInfo> basicLandSets = ExpansionRepository.instance.getSetsWithBasicLandsByReleaseDate();
            if (basicLandSets.size() > 0) {
                landSets.add(basicLandSets.get(generator.nextInt(basicLandSets.size())).getCode());
            }
        }

        if (landSets.isEmpty()) {
            throw new IllegalArgumentException("No set with basic land was found");
        }

        CardCriteria criteria = new CardCriteria();
        if (!landSets.isEmpty()) {
            criteria.setCodes(landSets.toArray(new String[landSets.size()]));
        }
        criteria.rarities(Rarity.LAND).name(landName);
        List<CardInfo> cards = CardRepository.instance.findCards(criteria);

        if (cards.isEmpty()) {
            criteria = new CardCriteria();
            criteria.rarities(Rarity.LAND).name(landName);
            criteria.setCodes("M15");
            cards = CardRepository.instance.findCards(criteria);
        }

        for (int i = 0; i < number; i++) {
            Card land = cards.get(random.nextInt(cards.size())).getCard();
            deck.getCards().add(land);
        }
    }

    public static Deck buildDeck(List<Card> cardPool, final List<ColoredManaSymbol> colors) {
        Deck deck = new Deck();
        List<Card> sortedCards = new ArrayList<>(cardPool);
        Collections.sort(sortedCards, new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                Integer score1 = RateCard.rateCard(o1, colors);
                Integer score2 = RateCard.rateCard(o2, colors);
                return score2.compareTo(score1);
            }
        });
        int cardNum = 0;
        while (deck.getCards().size() < 23 && sortedCards.size() > cardNum) {
            Card card = sortedCards.get(cardNum);
            if (!card.getSupertype().contains("Basic")) {
                deck.getCards().add(card);
                deck.getSideboard().remove(card);
            }
            cardNum++;
        }
        // add basic lands
        // TODO:  compensate for non basic lands
        Mana mana = new Mana();
        for (Card card : deck.getCards()) {
            mana.add(card.getManaCost().getMana());
        }
        double total = mana.getBlack() + mana.getBlue() + mana.getGreen() + mana.getRed() + mana.getWhite();
        int mostLand = 0;
        String mostLandName = "Forest";
        if (mana.getGreen() > 0) {
            int number = (int) Math.round(mana.getGreen() / total * 17);
            addBasicLands(deck, "Forest", number);
            mostLand = number;
        }
        if (mana.getBlack() > 0) {
            int number = (int) Math.round(mana.getBlack() / total * 17);
            addBasicLands(deck, "Swamp", number);
            if (number > mostLand) {
                mostLand = number;
                mostLandName = "Swamp";
            }
        }
        if (mana.getBlue() > 0) {
            int number = (int) Math.round(mana.getBlue() / total * 17);
            addBasicLands(deck, "Island", number);
            if (number > mostLand) {
                mostLand = number;
                mostLandName = "Island";
            }
        }
        if (mana.getWhite() > 0) {
            int number = (int) Math.round(mana.getWhite() / total * 17);
            addBasicLands(deck, "Plains", number);
            if (number > mostLand) {
                mostLand = number;
                mostLandName = "Plains";
            }
        }
        if (mana.getRed() > 0) {
            int number = (int) Math.round(mana.getRed() / total * 17);
            addBasicLands(deck, "Mountain", number);
            if (number > mostLand) {
                mostLandName = "Plains";
            }
        }

        addBasicLands(deck, mostLandName, 40 - deck.getCards().size());

        return deck;
    }

    @Override
    public void construct(Tournament tournament, Deck deck) {
        if (deck != null && deck.getCards().size() < 40 && deck.getSideboard().size() > 0) {
            //pick the top 23 cards
            if (chosenColors == null) {
                for (Card card : deck.getSideboard()) {
                    rememberPick(card, RateCard.rateCard(card, null));
                }
                chosenColors = chooseDeckColorsIfPossible();
            }
            deck = buildDeck(new ArrayList<>(deck.getSideboard()), chosenColors);
        }
        tournament.submitDeck(playerId, deck);
    }

    public Card pickBestCard(List<Card> cards, List<ColoredManaSymbol> chosenColors) {
        if (cards.isEmpty()) {
            return null;
        }
        Card bestCard = null;
        int maxScore = 0;
        for (Card card : cards) {
            int score = RateCard.rateCard(card, chosenColors);
            if (bestCard == null || score > maxScore) {
                maxScore = score;
                bestCard = card;
            }
        }
        return bestCard;
    }

    public Card pickBestCard(List<Card> cards, List<ColoredManaSymbol> chosenColors, Target target, Ability source, Game game) {
        if (cards.isEmpty()) {
            return null;
        }
        Card bestCard = null;
        int maxScore = 0;
        for (Card card : cards) {
            int score = RateCard.rateCard(card, chosenColors);
            boolean betterCard = false;
            if (bestCard == null) { // we need any card to prevent NPE in callers
                betterCard = true;
            } else if (score > maxScore) { // we need better card
                if (target != null && source != null && game != null) {
                    // but also check it can be targeted
                    betterCard = target.canTarget(getId(), card.getId(), source, game);
                } else {
                    // target object wasn't provided, so acceptings it anyway
                    betterCard = true;
                }
            }
            // is it better than previous one?
            if (betterCard) {
                maxScore = score;
                bestCard = card;
            }
        }
        return bestCard;
    }

    public Card pickWorstCard(List<Card> cards, List<ColoredManaSymbol> chosenColors, Target target, Ability source, Game game) {
        if (cards.isEmpty()) {
            return null;
        }
        Card worstCard = null;
        int minScore = Integer.MAX_VALUE;
        for (Card card : cards) {
            int score = RateCard.rateCard(card, chosenColors);
            boolean worseCard = false;
            if (worstCard == null) { // we need any card to prevent NPE in callers
                worseCard = true;
            } else if (score < minScore) { // we need worse card
                if (target != null && source != null && game != null) {
                    // but also check it can be targeted
                    worseCard = target.canTarget(getId(), card.getId(), source, game);
                } else {
                    // target object wasn't provided, so accepting it anyway
                    worseCard = true;
                }
            }
            // is it worse than previous one?
            if (worseCard) {
                minScore = score;
                worstCard = card;
            }
        }
        return worstCard;
    }

    public Card pickWorstCard(List<Card> cards, List<ColoredManaSymbol> chosenColors) {
        if (cards.isEmpty()) {
            return null;
        }
        Card worstCard = null;
        int minScore = Integer.MAX_VALUE;
        for (Card card : cards) {
            int score = RateCard.rateCard(card, chosenColors);
            if (worstCard == null || score < minScore) {
                minScore = score;
                worstCard = card;
            }
        }
        return worstCard;
    }

    @Override
    public void pickCard(List<Card> cards, Deck deck, Draft draft) {
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("No cards to pick from.");
        }
        try {
            Card bestCard = pickBestCard(cards, chosenColors);
            int maxScore = RateCard.rateCard(bestCard, chosenColors);
            int pickedCardRate = RateCard.getCardRating(bestCard);

            if (pickedCardRate <= 3) {
                // if card is bad
                // try to counter pick without any color restriction
                Card counterPick = pickBestCard(cards, null);
                int counterPickScore = RateCard.getCardRating(counterPick);
                // card is really good
                // take it!
                if (counterPickScore >= 8) {
                    bestCard = counterPick;
                    maxScore = RateCard.rateCard(bestCard, chosenColors);
                }
            }

            String colors = "not chosen yet";
            // remember card if colors are not chosen yet
            if (chosenColors == null) {
                rememberPick(bestCard, maxScore);
                chosenColors = chooseDeckColorsIfPossible();
            }
            if (chosenColors != null) {
                colors = "";
                for (ColoredManaSymbol symbol : chosenColors) {
                    colors += symbol.toString();
                }
            }
            log.debug("[DEBUG] AI picked: " + bestCard.getName() + ", score=" + maxScore + ", deck colors=" + colors);
            draft.addPick(playerId, bestCard.getId(), null);
        } catch (Exception e) {
            log.debug("Exception during AI pick card for draft playerId= " + getId());
            draft.addPick(playerId, cards.get(0).getId(), null);
        }
    }

    /**
     * Remember picked card with its score.
     *
     * @param card
     * @param score
     */
    protected void rememberPick(Card card, int score) {
        pickedCards.add(new PickedCard(card, score));
    }

    /**
     * Choose 2 deck colors for draft: 1. there should be at least 3 cards in
     * card pool 2. at least 2 cards should have different colors 3. get card
     * colors as chosen starting from most rated card
     *
     * @return
     */
    protected List<ColoredManaSymbol> chooseDeckColorsIfPossible() {
        if (pickedCards.size() > 2) {
            // sort by score and color mana symbol count in descending order
            Collections.sort(pickedCards, new Comparator<PickedCard>() {
                @Override
                public int compare(PickedCard o1, PickedCard o2) {
                    if (o1.score.equals(o2.score)) {
                        Integer i1 = RateCard.getColorManaCount(o1.card);
                        Integer i2 = RateCard.getColorManaCount(o2.card);
                        return i2.compareTo(i1);
                    }
                    return o2.score.compareTo(o1.score);
                }
            });
            Set<String> chosenSymbols = new HashSet<>();
            for (PickedCard picked : pickedCards) {
                int differentColorsInCost = RateCard.getDifferentColorManaCount(picked.card);
                // choose only color card, but only if they are not too gold
                if (differentColorsInCost > 0 && differentColorsInCost < 3) {
                    // if some colors were already chosen, total amount shouldn't be more than 3
                    if (chosenSymbols.size() + differentColorsInCost < 4) {
                        for (String symbol : picked.card.getManaCost().getSymbols()) {
                            symbol = symbol.replace("{", "").replace("}", "");
                            if (RateCard.isColoredMana(symbol)) {
                                chosenSymbols.add(symbol);
                            }
                        }
                    }
                }
                // only two or three color decks are allowed
                if (chosenSymbols.size() > 1 && chosenSymbols.size() < 4) {
                    List<ColoredManaSymbol> colorsChosen = new ArrayList<>();
                    for (String symbol : chosenSymbols) {
                        ColoredManaSymbol manaSymbol = ColoredManaSymbol.lookup(symbol.charAt(0));
                        if (manaSymbol != null) {
                            colorsChosen.add(manaSymbol);
                        }
                    }
                    if (colorsChosen.size() > 1) {
                        // no need to remember picks anymore
                        pickedCards = null;
                        return colorsChosen;
                    }
                }
            }
        }
        return null;
    }

    private class PickedCard {

        public Card card;
        public Integer score;

        public PickedCard(Card card, int score) {
            this.card = card;
            this.score = score;
        }
    }

    protected Attackers getPotentialAttackers(Game game) {
        log.debug("getAvailableAttackers");
        Attackers attackers = new Attackers();
        List<Permanent> creatures = super.getAvailableAttackers(game);
        for (Permanent creature : creatures) {
            int potential = combatPotential(creature, game);
            if (potential > 0 && creature.getPower().getValue() > 0) {
                List<Permanent> l = attackers.get(potential);
                if (l == null) {
                    attackers.put(potential, l = new ArrayList<>());
                }
                l.add(creature);
            }
        }
        return attackers;
    }

    protected int combatPotential(Permanent creature, Game game) {
        log.debug("combatPotential");
        if (!creature.canAttack(game)) {
            return 0;
        }
        int potential = creature.getPower().getValue();
        potential += creature.getAbilities().getEvasionAbilities().size();
        potential += creature.getAbilities().getProtectionAbilities().size();
        potential += creature.getAbilities().containsKey(FirstStrikeAbility.getInstance().getId()) ? 1 : 0;
        potential += creature.getAbilities().containsKey(DoubleStrikeAbility.getInstance().getId()) ? 2 : 0;
        potential += creature.getAbilities().containsKey(TrampleAbility.getInstance().getId()) ? 1 : 0;
        return potential;
    }

    protected List<Permanent> getOpponentBlockers(UUID opponentId, Game game) {
        FilterCreatureForCombatBlock blockFilter = new FilterCreatureForCombatBlock();
        List<Permanent> blockers = game.getBattlefield().getAllActivePermanents(blockFilter, opponentId, game);
        return blockers;
    }

    protected CombatSimulator simulateAttack(Attackers attackers, List<Permanent> blockers, UUID opponentId, Game game) {
        log.debug("simulateAttack");
        List<Permanent> attackersList = attackers.getAttackers();
        CombatSimulator best = new CombatSimulator();
        int bestResult = 0;
        //use binary digits to calculate powerset of attackers
        int powerElements = (int) Math.pow(2, attackersList.size());
        for (int i = 1; i < powerElements; i++) {
            String binary = Integer.toBinaryString(i);
            while (binary.length() < attackersList.size()) {
                binary = "0" + binary;
            }
            List<Permanent> trialAttackers = new ArrayList<>();
            for (int j = 0; j < attackersList.size(); j++) {
                if (binary.charAt(j) == '1') {
                    trialAttackers.add(attackersList.get(j));
                }
            }
            CombatSimulator combat = new CombatSimulator();
            for (Permanent permanent : trialAttackers) {
                combat.groups.add(new CombatGroupSimulator(opponentId, Arrays.asList(permanent.getId()), new ArrayList<UUID>(), game));
            }
            CombatSimulator test = simulateBlock(combat, blockers, game);
            if (test.evaluate() > bestResult) {
                best = test;
                bestResult = test.evaluate();
            }
        }

        return best;
    }

    protected CombatSimulator simulateBlock(CombatSimulator combat, List<Permanent> blockers, Game game) {
        log.debug("simulateBlock");

        TreeNode<CombatSimulator> simulations;

        simulations = new TreeNode<>(combat);
        addBlockSimulations(blockers, simulations, game);
        combat.simulate();

        return getWorstSimulation(simulations);

    }

    protected void addBlockSimulations(List<Permanent> blockers, TreeNode<CombatSimulator> node, Game game) {
        int numGroups = node.getData().groups.size();
        Copier<CombatSimulator> copier = new Copier<>();
        for (Permanent blocker : blockers) {
            List<Permanent> subList = remove(blockers, blocker);
            for (int i = 0; i < numGroups; i++) {
                if (node.getData().groups.get(i).canBlock(blocker, game)) {
                    CombatSimulator combat = copier.copy(node.getData());
                    combat.groups.get(i).blockers.add(new CreatureSimulator(blocker));
                    TreeNode<CombatSimulator> child = new TreeNode<>(combat);
                    node.addChild(child);
                    addBlockSimulations(subList, child, game);
                    combat.simulate();
                }
            }
        }
    }

    protected List<Permanent> remove(List<Permanent> source, Permanent element) {
        List<Permanent> newList = new ArrayList<>();
        for (Permanent permanent : source) {
            if (!permanent.equals(element)) {
                newList.add(permanent);
            }
        }
        return newList;
    }

    protected CombatSimulator getBestSimulation(TreeNode<CombatSimulator> simulations) {
        CombatSimulator best = simulations.getData();
        int bestResult = best.evaluate();
        for (TreeNode<CombatSimulator> node : simulations.getChildren()) {
            CombatSimulator bestSub = getBestSimulation(node);
            if (bestSub.evaluate() > bestResult) {
                best = node.getData();
                bestResult = best.evaluate();
            }
        }
        return best;
    }

    protected CombatSimulator getWorstSimulation(TreeNode<CombatSimulator> simulations) {
        CombatSimulator worst = simulations.getData();
        int worstResult = worst.evaluate();
        for (TreeNode<CombatSimulator> node : simulations.getChildren()) {
            CombatSimulator worstSub = getWorstSimulation(node);
            if (worstSub.evaluate() < worstResult) {
                worst = node.getData();
                worstResult = worst.evaluate();
            }
        }
        return worst;
    }

    protected List<Permanent> threats(UUID playerId, UUID sourceId, FilterPermanent filter, Game game, List<UUID> targets) {
        List<Permanent> threats;
        if (playerId == null) {
            threats = game.getBattlefield().getActivePermanents(filter, this.getId(), sourceId, game); // all permanents within the range of the player
        } else {
            FilterPermanent filterCopy = filter.copy();
            filterCopy.add(new ControllerIdPredicate(playerId));
            threats = game.getBattlefield().getActivePermanents(filter, this.getId(), sourceId, game);
        }
        Iterator<Permanent> it = threats.iterator();
        while (it.hasNext()) { // remove permanents already targeted
            Permanent test = it.next();
            if (targets.contains(test.getId()) || (playerId != null && !test.getControllerId().equals(playerId))) {
                it.remove();
            }
        }
        Collections.sort(threats, new PermanentComparator(game));
        Collections.reverse(threats);
        return threats;
    }

    protected void logState(Game game) {
        if (log.isTraceEnabled()) {
            logList("Computer player " + name + " hand: ", new ArrayList<MageObject>(hand.getCards(game)));
        }
    }

    protected void logList(String message, List<MageObject> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(": ");
        for (MageObject object : list) {
            sb.append(object.getName()).append(",");
        }
        log.info(sb.toString());
    }

    protected void logAbilityList(String message, List<Ability> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(": ");
        for (Ability ability : list) {
            sb.append(ability.getRule()).append(",");
        }
        log.debug(sb.toString());
    }

    private void playRemoval(List<UUID> creatures, Game game) {
        for (UUID creatureId : creatures) {
            for (Card card : this.playableInstant) {
                if (card.getSpellAbility().canActivate(playerId, game)) {
                    for (Effect effect : card.getSpellAbility().getEffects()) {
                        if (effect.getOutcome().equals(Outcome.DestroyPermanent) || effect.getOutcome().equals(Outcome.ReturnToHand)) {
                            if (card.getSpellAbility().getTargets().get(0).canTarget(creatureId, card.getSpellAbility(), game)) {
                                if (this.activateAbility(card.getSpellAbility(), game)) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void playDamage(List<UUID> creatures, Game game) {
        for (UUID creatureId : creatures) {
            Permanent creature = game.getPermanent(creatureId);
            for (Card card : this.playableInstant) {
                if (card.getSpellAbility().canActivate(playerId, game)) {
                    for (Effect effect : card.getSpellAbility().getEffects()) {
                        if (effect instanceof DamageTargetEffect) {
                            if (card.getSpellAbility().getTargets().get(0).canTarget(creatureId, card.getSpellAbility(), game)) {
                                if (((DamageTargetEffect) effect).getAmount() > (creature.getPower().getValue() - creature.getDamage())) {
                                    if (this.activateAbility(card.getSpellAbility(), game)) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        unplayable = new TreeMap<>();
        playableNonInstant = new ArrayList<>();
        playableInstant = new ArrayList<>();
        playableAbilities = new ArrayList<>();
    }

    @Override
    public void cleanUpOnMatchEnd() {
        super.cleanUpOnMatchEnd(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ComputerPlayer copy() {
        return new ComputerPlayer(this);
    }

}
